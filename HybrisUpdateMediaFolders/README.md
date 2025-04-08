# Hybris Update Media Folders

This script updates the location field of media items, so it is prefixed with the name of the media folder where they belong. This is useful, for example, when copying the database and medias over from CCv2 to the local environment, since in CCv2 the cloud media storage strategy doesn't prefix the location with the folder name, and the local media strategy does.

## Features
- Takes the original location field of each media item and prefixes it with the folder name.
- Recalculates the location hash.
- Batch processing to avoid overwhelming the system.
- Outputs progress and estimated time of completion.
- Logs into the console log, so the progress can be seen even if the script takes too long and the browser times out.

## Usage

1. Clone this repository or download the script.
2. Configure the script - adjust the constants at the beginning of the script:
	- **basePath** - root of the media folder
	- **maxItems** - size of the batch to process with this script
	- **includedFolders** and **excludedFolders** - configure which media folders to specifically include and exclude from processing
	- **intervalMillis** - how often to output the progress and the ETA to the log

3. Run the script with Groovy in HAC (Hybris Administration Console).

## Requirements

Access to Hybris HAC (appropriate credentials).

## Example Output

```
[MediaLocationUpdater] ⏭️ Skipping (not in included list): email-body
[MediaLocationUpdater] ⏭️ Skipping (not in included list): couponcodes
[MediaLocationUpdater] ⏭️ Skipping (not in included list): documents
[MediaLocationUpdater] ⏭️ Skipping (not in included list): accountsummary
[MediaLocationUpdater] ⏭️ Skipping (not in included list): root
[MediaLocationUpdater] ⏭️ Skipping (not in included list): hmc
[MediaLocationUpdater] ⏭️ Skipping (not in included list): cronjob
[MediaLocationUpdater] ⏭️ Skipping (not in included list): impex
[MediaLocationUpdater] ⏭️ Skipping (not in included list): catalogsync
[MediaLocationUpdater] ⏭️ Skipping (not in included list): auditreports
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficeconfiguration
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficeuseravatars
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficeexcelimport
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficethemes
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficewidgetsconfig
[MediaLocationUpdater] ⏭️ Skipping (not in included list): backofficelogos
[MediaLocationUpdater] ⏭️ Skipping (not in included list): kie-modules
[MediaLocationUpdater] ⏭️ Skipping (not in included list): customersupportattachments
[MediaLocationUpdater] ⏭️ Skipping (not in included list): email-attachments
[MediaLocationUpdater] 📂 Processing folder: images
[MediaLocationUpdater] 📈 images: 4079/9426 (43.27%) done, ⏳ remaining: 0 min 39 sec
[MediaLocationUpdater] 📈 images: 6509/9426 (69.05%) done, ⏳ remaining: 0 min 26 sec
[MediaLocationUpdater] 📈 images: 9286/9426 (98.51%) done, ⏳ remaining: 0 min 1 sec
[MediaLocationUpdater] 📈 images: 9426/9426 (100.00%) done, ⏳ remaining: 0 min 0 sec
[MediaLocationUpdater] 📈 images: 9426/9426 (100.00%) done, ⏳ remaining: 0 min 0 sec
[MediaLocationUpdater] ⏭️ Skipping (not in included list): email-body
[MediaLocationUpdater] ⏭️ Skipping (not in included list): couponcodes
[MediaLocationUpdater] ⏭️ Skipping (not in included list): documents
[MediaLocationUpdater] ⏭️ Skipping (not in included list): accountsummary
```

## License

This script is part of the Hybris Utils repository and is licensed under the [MIT License](../LICENSE).
