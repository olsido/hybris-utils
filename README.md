# Hybris Utils

A collection of utility scripts to assist with SAP Hybris project management and analysis. These tools are designed to automate common tasks, improve efficiency, and help developers working with Hybris.

## Current Tools

### 1. Hybris Circular Dependency Checker

This script helps you detect circular dependencies between extensions in your Hybris project. It recursively scans `extensioninfo.xml` files to analyze dependencies and report any circular references.

#### Usage
1. Clone this repository or download the script.
2. Run the script with Groovy, passing the path to your `bin/custom` directory as an argument:
   ```bash
   groovy HybrisCircularDependencyChecker.groovy <path-to-hybris-bin-custom>
