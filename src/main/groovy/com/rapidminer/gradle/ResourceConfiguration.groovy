/*
 * Copyright 2013-2015 RapidMiner GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rapidminer.gradle;

/**
 * Resource files and init-class definitions.
 *
 * @author Nils Woehler
 *
 */
public class ResourceConfiguration {
	
	String initClass
	String operatorDefinition
	String objectDefinition = ""
	String parseRuleDefinition = ""
	String groupProperties = ""
	String errorDescription = ""
	String userErrors = ""
	String guiDescription = ""
	String settingsDescriptor = ""
	String settingsStructureDescriptor = ""

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
	
	def settingsDescriptor(String settingsDescriptor){
		this.settingsDescriptor = settingsDescriptor
	}
	
	def settingsStructureDescriptor(String settingsStructureDescriptor){
		this.settingsStructureDescriptor = settingsStructureDescriptor
	}
	
}
