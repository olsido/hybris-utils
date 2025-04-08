# Hybris Utils

A collection of utility scripts to assist with SAP Hybris project management and analysis. These tools are designed to automate common tasks, improve efficiency, and help developers working with Hybris.

## Current Tools

1. **[Hybris Circular Dependency Checker](HybrisCircularDependencyChecker)**
   - Detects circular dependencies between extensions in your Hybris project.
   - Analyzes `extensioninfo.xml` files and reports any circular references.

2. **[Hybris Obsolete Items Cleanup](HybrisObsoleteItemsCleanup)**
   - Automates the cleanup of obsolete items such as old log files, job logs, and ImpEx media.
   - Supports flexible date cutoff policies and batch deletion for optimized performance.

3. **[Hybris Update Media Folders](HybrisUpdateMediaFolders)**
   - Updates the location field of media items, so it is prefixed with the name of the media folder where they belong.
   - This is useful, for example, when copying the database and medias over from CCv2 to the local environment, since in CCv2 the cloud media storage strategy doesn't prefix the location with the folder name, and the local media strategy does.

Each script is in its own directory with specific usage instructions. Navigate to the desired tool's directory for more details.

## How to Use These Scripts

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/Hybris-Utils.git
   ```
2. Navigate to the desired script’s directory.

3. Follow the instructions provided in the README.md of that directory.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the [MIT License](LICENSE).