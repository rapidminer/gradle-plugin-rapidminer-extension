package com.rapidminer.gradle;

/**
 * 
 * @author Nils Woehler
 *
 */
public class ExtensionDefinition {
	String name = null
	String namespace = null
	String vendor = "com.rapidminer.extension"
	String admin = ""
	String homepage = ""
	String extensionDependencies = ""
	
	String rapidminerVersion = "6.0.003"
	String rapidminerHome = "../rapidminer-studio"
	String javaTarget = "1.7"
	Boolean bundleDependencies = true
	
	// Resource files and init-class definitions
	ResourceConfiguration resources = new ResourceConfiguration()
	
	/**
	 * Delegates the provided Closure to the ResourceConfiguration.
	 */
	def resources(Closure closure){
		resources.apply(closure)
	}
}
