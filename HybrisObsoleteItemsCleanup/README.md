# Hybris Obsolete Items Cleanup

This script automates the cleanup of obsolete items in SAP Hybris, such as old log files, job logs, and ImpEx media. It supports flexible date cutoff policies and batch deletion for optimized performance.

## Features
- Deletes obsolete items like `LogFile`, `JobLog`, and `ImpExMedia`.
- Supports flexible cutoff times, e.g., `2Y 6M 3D 4H` (2 years, 6 months, 3 days, 4 hours).
- Batch processing to avoid overwhelming the system.
- Outputs progress and estimated time of completion.

## Usage

1. Clone this repository or download the script.
2. Configure the script:
   - Adjust the `TARGET_TYPES` list to include the item types, cutoff policies, and mode (`DELETE` or `QUERY`):
     ```groovy
     final List<Map<String, Object>> TARGET_TYPES = [
       [
           type: 'LogFile',
           query: "SELECT {PK} FROM {LogFile} WHERE {creationtime} <= '__CUTOFF_DATE__'",
           cutoff: '2.5Y',
           mode: 'QUERY'
       ]
     ]
3. Run the script with Groovy in HAC (Hybris Administration Console).
4. Monitor logs for logger "groovy.log.cleanup".

## Requirements

Access to Hybris HAC (appropriate credentials).

## Example Output

```
Obsolete items deletion script started at: 2024-12-01T15:52:48.340665557
Generated Query for LogFile (cutoff = 2Y 6M 1H 1M 1S): SELECT {PK} FROM {LogFile} WHERE {creationtime} <= '2022-06-01 14:51:47'
Total LogFile items to delete (cutoff: 2022-06-01 14:51:47): 1,767

    Deleted 1,000 LogFile items in this batch. 
    Remaining: 767 (56.59% completed). 
    Estimated time to complete: 5 minutes (or at 02:56 PM EST).

    Deleted 767 LogFile items in this batch. 
    Remaining: 0 (100.00% completed). 
    Estimated time to complete: now.

Cleanup completed for LogFile. Total deleted: 1,767 items.
Total execution time: 0h 6m 43s
Obsolete items deletion script completed at: 2024-12-01T15:58:24.536256745
```

## License

This script is part of the Hybris Utils repository and is licensed under the [MIT License](../LICENSE).
