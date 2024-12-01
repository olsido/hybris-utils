# Hybris Circular Dependency Checker

This script helps detect circular dependencies between extensions in your Hybris project. It recursively scans `extensioninfo.xml` files to analyze dependencies and report any circular references.

## Features
- Recursive scanning of `extensioninfo.xml`.
- Detailed reporting of circular references.
- Helps maintain clean and manageable dependencies.

---

## Usage

1. Clone this repository or download the script.
2. Run the script with Groovy on your machine, passing the path to your `bin/custom` directory as an argument:
   ```bash
   groovy HybrisCircularDependencyChecker.groovy <path-to-hybris-bin-custom>
3. Review the output for any circular dependencies.

## Example Output

```
Circular dependency detected: extension-a -> extension-b -> extension-a
No issues detected for: extension-c
```

## Requirements

- Groovy installed on your system.
- Access to the Hybris `bin/custom` directory.

## License

This script is part of the Hybris Utils repository and is licensed under the [MIT License](../LICENSE).