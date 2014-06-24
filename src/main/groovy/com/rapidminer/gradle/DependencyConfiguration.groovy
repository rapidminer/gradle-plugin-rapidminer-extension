package com.rapidminer.gradle;

/**
 * Resource files and init-class definitions.
 * 
 * @author Nils Woehler
 *
 */
public class DependencyConfiguration {
	
	List<ExtensionDependency> extensions = []
	String rapidminer = '6.0.000'
	
	/**
	 * Applies the provided closure (e.g. to configure class fields).
	 */
	def apply(Closure closure) {
		closure.delegate = this
		closure()
	}
	
	def rapidminer(String version) {
		this.rapidminer = version
	}
	
	def extension(extensionDef) {
		extensions << new ExtensionDependency(extensionDef)
	}
	
	def extension(String namespace, String version, String group = ExtensionDependency.DEFAULT_GROUP) {
		extensions << new ExtensionDependency(group: group, namespace: namespace, version: version)
	}
}
