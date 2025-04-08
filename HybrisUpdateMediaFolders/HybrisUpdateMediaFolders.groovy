import de.hybris.platform.core.model.media.MediaModel
import de.hybris.platform.core.model.media.MediaFolderModel
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import java.io.File
import java.io.FileInputStream
import org.slf4j.LoggerFactory

def logger = LoggerFactory.getLogger("MediaLocationUpdater")

def mediaService = spring.getBean("mediaService")
def modelService = spring.getBean("modelService")
def flexibleSearchService = spring.getBean("flexibleSearchService")

// Adjust path to your media root
def basePath = "/path/to/hybris_home/data/media/sys_master/"

// The maximum number of items to be processed - script can be run multiple times to process items chunk by chunk, this is better than having it run indefinitely and only way to stop it to be to kill Hybris
def maxItems = 10000

// Folders you want to explicitly process - if any are included, then the script will only process these folders and no others
def includedFolders = []

// Folders to skip (if not explicitly included)
def excludedFolders = ["root", "images", "cronjob", "catalogsync"]

// Time interval for the updates in the log
def intervalMillis = 30 * 1000 // 30 seconds, change this to 75000 for 1 min 15 sec, etc.

// Get all media folders
def folderQuery = "SELECT {mf:pk}, {mf:qualifier} FROM {MediaFolder AS mf} WHERE {mf:qualifier} IS NOT NULL"
def folderResults = flexibleSearchService.search(folderQuery).result

// Iterate over each folder
for (folder in folderResults) {
	def qualifier = folder.qualifier

	// Process the folder if it is in the inclusion list, or the inclusion list is empty, unless it is in the exclusion list
	if (!includedFolders.isEmpty()) {
	    if (!includedFolders.contains(qualifier)) {
	        logger.info "⏭️ Skipping (not in included list): ${qualifier}"
	        continue
	    }
	} else if (excludedFolders.contains(qualifier)) {
	    logger.info "⏭️ Skipping (in excluded list): ${qualifier}"
	    continue
	}

	logger.info "📂 Processing folder: ${qualifier}"

	// Select all the medias that belong to this media folder, and whose location doesn't start with this media folder name
	def mediaQuery = """
	  SELECT {pk}
	  FROM {
	      Media AS m
	      JOIN MediaFolder AS mf ON {m.folder} = {mf.pk}
	  }
	  WHERE {mf.qualifier} = ?qualifier AND {m.location} NOT LIKE '${qualifier}%'
	"""
	def params = ["qualifier": qualifier]
	def query = new FlexibleSearchQuery(mediaQuery, params)
	query.setCount(maxItems)  // Select maximum maxItems medias
	def mediaList = flexibleSearchService.search(query).result

	def startTime = System.currentTimeMillis()
	def lastLoggedTime = startTime
	def total = mediaList.size()
	def count = 0

	for (media in mediaList) {

		// Calculate and log ETA to completion
	    count++
	    if (System.currentTimeMillis() - lastLoggedTime >= intervalMillis || count == total) {
			def now = System.currentTimeMillis()
			def elapsed = now - startTime
			def avgPerItem = count > 0 ? (elapsed / count) : 0
			def remaining = (total - count) * avgPerItem
			def remainingSec = (long)(remaining / 1000)
			def mins = (int)(remainingSec / 60)
			def secs = (int)(remainingSec % 60)
			def pct = String.format("%.2f", (count * 100.0 / total))
			logger.info "📈 ${qualifier}: ${count}/${total} (${pct}%) done, ⏳ remaining: ${mins} min ${secs} sec"
			lastLoggedTime = now
		}

		// Update media location so it starts with the media folder
	    def oldLocation = media.getLocation()
	    if (oldLocation == null || oldLocation.startsWith(qualifier + "/")) {
	        logger.info "✔️ Already prefixed or empty location: ${oldLocation}"
	    } else {
	        def newLocation = qualifier + "/" + oldLocation
	        def mediaFile = new File(basePath + newLocation)

	        if (mediaFile.exists()) {
	            def inputStream = new FileInputStream(mediaFile)
	            media.setLocation(newLocation)
	            mediaService.setStreamForMedia(media, inputStream)
	            inputStream.close()
	            modelService.save(media)
	            modelService.detach(media)
	            //logger.info "✅ Updated media: ${media.code} → ${media.location}"
	        } else {
	            logger.info "⚠️ File not found: ${mediaFile.getAbsolutePath()}"
	        }
	    }
	}
}
// Free up memory
modelService.detachAll()
System.gc()