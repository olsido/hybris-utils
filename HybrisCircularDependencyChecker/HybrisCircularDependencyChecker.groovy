import groovy.xml.XmlSlurper
import java.nio.file.*

class HybrisCircularDependencyChecker {

    // Map to store the dependencies between extensions
    static Map<String, List<String>> extensionDependencies = new HashMap<>()

    // Set to keep track of visited extensions during the search
    static Set<String> visited = new HashSet<>()
    static List<String> stack = new ArrayList<>()

    static void main(String[] args) {
        if (args.length < 1) {
            println "Usage: groovy HybrisCircularDependencyChecker.groovy <hybris-bin-custom-path>"
            System.exit(1)
        }

        def customExtensionsPath = Paths.get(args[0])

        // Recursively process each extensioninfo.xml under the bin/custom directory
        Files.walk(customExtensionsPath).filter { it.toString().endsWith("extensioninfo.xml") }.each { xmlFile ->
            analyzeExtensionXml(xmlFile)
        }

        //println "extensionDependencies = $extensionDependencies"

        // Detect circular dependencies
        extensionDependencies.keySet().each { extension ->
            if (hasCircularDependency(extension)) {
                println "Circular dependency detected involving extension: $extension"
            }
        }

        println "Circular dependency analysis completed."
    }

    // Parse extensioninfo.xml to find extension dependencies
    static void analyzeExtensionXml(Path xmlFilePath) {
        //println "Analyzing extension $xmlFilePath"

        def xml = new XmlSlurper().parse(xmlFilePath.toFile())
        def extensionName = xml.extension.@name.text()

        if (!extensionName) {
            println "No extension name found in ${xmlFilePath}"
            return
        }

        // Fetch dependencies explicitly using findAll
	def dependencies = xml.'extension'.'requires-extension'.collect { it.@name.text() }

        extensionDependencies.put(extensionName, dependencies)
        //println "Extension $extensionName depends on $dependencies"
    }

    // Detect circular dependencies in the extension dependency graph
    static boolean hasCircularDependency(String extension) {
        //println "Inside hasCircularDependency($extension)"
        //println "stack = $stack"
        //println "visited = $visited"

        if (stack.contains(extension)) {
            println "-------> Circular dependency path: ${stack.join(' -> ')} -> $extension"
            return true
        }

        if (!visited.contains(extension)) {
            visited.add(extension)
            //println "Added $extension to visited"
            if (!stack.contains(extension)) {
		stack.add(extension)
	    }
            //println "Added $extension to stack"

            def dependencies = extensionDependencies.get(extension) ?: []
            //println "dependencies = $dependencies"
            dependencies.each { dep ->
                //println "Going to analyze circular dependencies for $dep"
                if (hasCircularDependency(dep)) {
                    return true
                }
            }

            //println "Removing $extension from stack"
            stack.remove(extension)
            //println "Now stack = $stack"
        }
        return false
    }
}
