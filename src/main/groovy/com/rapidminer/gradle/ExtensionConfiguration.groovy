package com.rapidminer.gradle;

/**
 *
 * @author Nils Woehler
 *
 */
public class ExtensionConfiguration {

	/**
	 * The default extension groupId	
	 */
	public static final String DEFAULT_GROUP = 'com.rapidminer.extension'
	
	String name
	String namespace
	String groupId = DEFAULT_GROUP
	
	String vendor = "RapidMiner GmbH"
	String homepage = "www.rapidminer.com"

	String rapidminerHome = "../rapidminer-studio"

	// RapidMiner and Extension dependencies
	DependencyConfiguration dependencies = new DependencyConfiguration()

	// Resource files and init-class definitions
	ResourceConfiguration resources = new ResourceConfiguration()

	/**
	 * Delegates the provided Closure to the ResourceConfiguration.
	 */
	def resources(Closure closure){
		resources.apply(closure)
	}

	/**
	 * Delegates the provided Closure to the DependencyConfiguration.
	 */
	def dependencies(Closure closure){
		dependencies.apply(closure)
	}
}
