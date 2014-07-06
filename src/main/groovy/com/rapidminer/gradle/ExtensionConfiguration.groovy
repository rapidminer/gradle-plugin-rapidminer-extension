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
	
	public static final String DEFAULT_WRAPPER_VERSION = '1.12'

	String name
	String namespace
	String groupId = DEFAULT_GROUP

	String vendor = "RapidMiner GmbH"
	String homepage = "www.rapidminer.com"

	String rapidminerHome = "../rapidminer-studio"
	
	String wrapperVersion = DEFAULT_WRAPPER_VERSION

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

	/**
	 * Overwrite namespace getter to ensure that there always is a non empty namespace
	 */
	def getNamespace(){
		if(!namespace){
			return name.toLowerCase().replace(" ", "-");
		}
		return namespace
	}
}
