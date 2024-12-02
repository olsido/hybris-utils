import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import de.hybris.platform.servicelayer.model.ModelService
import org.apache.log4j.Logger
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Collections
import java.text.NumberFormat
import java.util.Locale
import java.math.BigDecimal

// Constants
final int BATCH_SIZE = 1000
final String TIME_ZONE = "America/New_York"  // Adjust time zone as needed

// List of target types with their queries, cutoff policies, and modes
final List<Map<String, Object>> TARGET_TYPES = [
    [
        type: 'LogFile',
        query: "SELECT {PK} FROM {LogFile} WHERE {creationtime} <= '__CUTOFF_DATE__'",
        cutoff: '2Y 5M 1H 1M 1S', // 3 years, 6 months, 10 hours
        mode: 'QUERY' // Mode for this type - can be 'QUERY' or 'DELETE'
    ],
    [
        type: 'JobLog',
        query: "SELECT {PK} FROM {JobLog} WHERE {modifiedtime} <= '__CUTOFF_DATE__'",
        cutoff: '2.75Y', // 2.75 years
        mode: 'QUERY' // Mode for this type - can be 'QUERY' or 'DELETE'
    ],
    [
        type: 'ImpExMedia',
        query: "SELECT {PK} FROM {ImpExMedia} WHERE {creationtime} <= '__CUTOFF_DATE__'",
        cutoff: '6M 12H', // 6 months, 12 hours
        mode: 'QUERY' // Mode for this type - can be 'QUERY' or 'DELETE'
    ],
    [
        type: 'PriceRow',
        query: "SELECT {PK} FROM {PriceRow} WHERE {endTime} <= '__CUTOFF_DATE__'",
        cutoff: '1Y 1M 2W', // 6 months, 12 hours
        mode: 'QUERY' // Mode for this type - can be 'QUERY' or 'DELETE'
    ],
    [
        type: 'PromotionSourceRule',
        query: "SELECT {PK} FROM {PromotionSourceRule} WHERE {endDate} <= '__CUTOFF_DATE__'",
        cutoff: '1Y 1M 2W', // 6 months, 12 hours
        mode: 'QUERY' // Mode for this type - can be 'QUERY' or 'DELETE'
    ]
]

// Helper to calculate dynamic cutoff dates
def calculateCutoffDate(String cutoffPolicy) {
    LocalDateTime now = LocalDateTime.now()
    def years = 0
    def months = 0
    def weeks = 0
    def days = 0
    def hours = 0
    def minutes = 0
    def seconds = 0

    def matchPattern = ~/(?i)(?:(\d+\.?\d*)Y)?\s*(?:(\d+\.?\d*)M)?\s*(?:(\d+\.?\d*)W)?\s*(?:(\d+\.?\d*)D)?\s*(?:(\d+\.?\d*)H)?\s*(?:(\d+\.?\d*)m)?\s*(?:(\d+\.?\d*)S)?/
    def matcher = cutoffPolicy =~ matchPattern

    if (matcher.find()) {
        def yearMatch = matcher[0][1]
        def monthMatch = matcher[0][2]
        def weekMatch = matcher[0][3]
        def dayMatch = matcher[0][4]
        def hourMatch = matcher[0][5]
        def minuteMatch = matcher[0][6]
        def secondMatch = matcher[0][7]

        if (yearMatch) {
            BigDecimal yearValue = new BigDecimal(yearMatch)
            years = yearValue.intValue()
            months += (yearValue.remainder(BigDecimal.ONE) * 12).intValue() // Convert fractional years to months
        }
        if (monthMatch) {
            BigDecimal monthValue = new BigDecimal(monthMatch)
            months += monthValue.intValue()
            days += (monthValue.remainder(BigDecimal.ONE) * 30).intValue() // Convert fractional months to days
        }
        if (weekMatch) {
            BigDecimal weekValue = new BigDecimal(weekMatch)
            weeks += weekValue.intValue()
            days += (weekValue.remainder(BigDecimal.ONE) * 7).intValue() // Convert fractional weeks to days
        }
        if (dayMatch) {
            days += new BigDecimal(dayMatch).intValue()
        }
        if (hourMatch) {
            hours += new BigDecimal(hourMatch).intValue()
        }
        if (minuteMatch) {
            minutes += new BigDecimal(minuteMatch).intValue()
        }
        if (secondMatch) {
            seconds += new BigDecimal(secondMatch).intValue()
        }
    }

    return now.minusYears(years)
              .minusMonths(months)
              .minusWeeks(weeks)
              .minusDays(days)
              .minusHours(hours)
              .minusMinutes(minutes)
              .minusSeconds(seconds)
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}

// Helper to format numbers with commas
def formatNumber(long number) {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

// Initialize the logger for Kibana
def logger = Logger.getLogger("groovy.log.cleanup")

// Services
def flexibleSearchService = spring.getBean("flexibleSearchService")
def modelService = spring.getBean("modelService")

// Log script start time
def scriptStartTime = LocalDateTime.now()
logger.info("Obsolete items deletion script started at: ${scriptStartTime}")
println "Obsolete items deletion script started at: ${scriptStartTime}"

TARGET_TYPES.each { target ->
    def targetType = target.type
    def queryTemplate = target.query
    def cutoffPolicy = target.cutoff
    def mode = target.mode // Mode for the current type
    def cutoffDate = calculateCutoffDate(cutoffPolicy)

    // Replace placeholder with actual cutoff date
    def queryWithCutoffDate = queryTemplate.replace('__CUTOFF_DATE__', cutoffDate)
  
    // Log and print the generated query with the cutoff policy
    println "Generated Query for ${targetType} (cutoff = ${cutoffPolicy}): ${queryWithCutoffDate}"
    logger.info("Generated Query for ${targetType} (cutoff = ${cutoffPolicy}): ${queryWithCutoffDate}")

    // Query to get the total count of items to delete
    def countQuery = """
        SELECT COUNT(*) 
        FROM {${targetType}} 
        WHERE {PK} IN ({{${queryWithCutoffDate}}})
    """

    def countFlexibleSearchQuery = new FlexibleSearchQuery(countQuery)
    countFlexibleSearchQuery.setResultClassList(Collections.singletonList(Long.class))

    def countResult = flexibleSearchService.search(countFlexibleSearchQuery)
    def totalToDelete = countResult.result.get(0) ?: 0  // Handle null result gracefully

    logger.info("Total ${targetType} items to delete (cutoff: ${cutoffDate}): ${formatNumber(totalToDelete)}")
    println "Total ${targetType} items to delete (cutoff: ${cutoffDate}): ${formatNumber(totalToDelete)}"
  
    if (mode == 'QUERY') {
        println "Query mode enabled. No deletion performed for ${targetType}."
        return  // Exit the current iteration
    }

    if (totalToDelete == 0) {
        println "No ${targetType} items found to delete."
        logger.info("No ${targetType} items found to delete.")
        return
    }

    int deletedCount = 0
    long startTime = System.currentTimeMillis()

    while (true) {
        // Fetch the next batch of items
        def batchQuery = """
            ${queryWithCutoffDate}
            ORDER BY {creationtime} ASC
        """
        def query = new FlexibleSearchQuery(batchQuery)
        query.setCount(BATCH_SIZE)  // Limit the batch size
        def result = flexibleSearchService.search(query)
        def items = result.result

        if (items.isEmpty()) {
            break
        }

        try {
            // Remove all items in the batch
            modelService.removeAll(items)
            deletedCount += items.size()
        } catch (Exception e) {
            def errorMessage = "Failed to delete a batch of ${targetType} items. Error: ${e.message}"
            println errorMessage
            logger.error(errorMessage)
        }

        // Calculate remaining items and percentage
        def remaining = totalToDelete - deletedCount
        def percentageCompleted = (deletedCount / totalToDelete.toDouble()) * 100

        // Estimate time to complete
        long elapsedTime = System.currentTimeMillis() - startTime
        long estimatedTotalTime = (elapsedTime / deletedCount) * totalToDelete
        long remainingTimeMillis = estimatedTotalTime - elapsedTime
        def remainingTime = Duration.ofMillis(remainingTimeMillis)
        def etcHours = remainingTime.toHours()
        def etcMinutes = remainingTime.toMinutesPart()
        def completionTime = LocalDateTime.now().plusSeconds((remainingTimeMillis / 1000) as long)
        def formattedCompletionTime = completionTime.atZone(ZoneId.of(TIME_ZONE))
            .format(DateTimeFormatter.ofPattern("hh:mm a z"))

        // Progress message
        def timeMessage
        if (etcHours == 0 && etcMinutes == 0) {
            timeMessage = "now"
        } else {
            timeMessage = "${etcHours > 0 ? "${etcHours} hours " : ""}${etcMinutes} minutes (or at ${formattedCompletionTime})"
        }

        def progressMessage = """
            Deleted ${formatNumber(items.size())} ${targetType} items in this batch. 
            Remaining: ${formatNumber(remaining)} (${String.format('%.2f', percentageCompleted)}% completed). 
            Estimated time to complete: ${timeMessage}.
        """
        println progressMessage
        logger.info(progressMessage)
    }

    // Final message for this type
    def completionMessage = "Cleanup completed for ${targetType}. Total deleted: ${formatNumber(deletedCount)} items."
    println completionMessage
    logger.info(completionMessage)
}

// Log script end time
def scriptEndTime = LocalDateTime.now()
def duration = Duration.between(scriptStartTime, scriptEndTime)
def hours = duration.toHours()
def minutes = duration.toMinutesPart()
def seconds = duration.toSecondsPart()

logger.info("Obsolete items deletion script completed at: ${scriptEndTime}")
println "Obsolete items deletion script completed at: ${scriptEndTime}"
