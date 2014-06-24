package com.rapidminer.gradle;

/**
 *
 * @author Nils Woehler
 *
 */
public class ExtensionConfiguration {
	String name = null
	String namespace = null
	String vendor = "com.rapidminer.extension"
	String admin = ""
	String homepage = ""

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
