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
package com.rapidminer.gradle

import org.gradle.api.Project;

/**
 * Resource files and init-class definitions.
 * 
 * @author Nils Woehler
 *
 */
public class DependencyConfiguration {
	
	List<ExtensionDependency> extensions = []
	String rapidminer = '6.0.000'
	Boolean useAntArtifact = false
	Project project
	
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
	
	def useAntArtifact(Boolean use) {
		this.useAntArtifact = use
	}

	def project(Project projectReference){
		this.project = projectReference
	}
	
	def extension(extensionDef) {
		extensions << new ExtensionDependency(extensionDef)
	}
	
	def extension(String namespace, String version, String group = ExtensionConfiguration.DEFAULT_GROUP) {
		extensions << new ExtensionDependency(group: group, namespace: namespace, version: version)
	}
}
