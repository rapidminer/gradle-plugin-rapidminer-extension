package com.rapidminer.gradle;

/**
 * Resource files and init-class definitions.
 * 
 * @author Nils Woehler
 *
 */
public class ResourceConfiguration {
	String initClass = null
	String operatorDefinition = null
	String objectDefinition = ""
	String parseRuleDefinition = ""
	String groupProperties = ""
	String errorDescription = ""
	String userErrors = ""
	String guiDescription = ""
	
	/**
	 * Applies the provided closure (e.g. to configure class fields).
	 */
	def apply(Closure closure) {
		closure.delegate = this
		closure()
	}
	
	// Setter methods. Otherwise the Closure provided to apply cannot be evaluated.
	
	def initClass(String initClass) {
		this.initClass = initClass
	}
	
	def operatorDefinition(String operatorDefinition) {
		this.operatorDefinition = operatorDefinition
	}
	
	def objectDefinition(String objectDefinition) {
		this.objectDefinition = objectDefinition
	}
	
	def parseRuleDefinition(String parseRuleDefinition) {
		this.parseRuleDefinition = parseRuleDefinition
	}
	
	def groupProperties(String groupProperties) {
		this.groupProperties = groupProperties
	}
	
	def errorDescription(String errorDescription) {
		this.errorDescription = errorDescription
	}
	
	def userErrors(String userErrors) {
		this.userErrors = userErrors
	}
	
	def guiDescription(String guiDescription) {
		this.guiDescription = guiDescription
	}
}
