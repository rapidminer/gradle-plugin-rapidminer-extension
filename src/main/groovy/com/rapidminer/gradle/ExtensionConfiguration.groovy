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

import org.gradle.api.GradleException

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
	
	public static final String DEFAULT_WRAPPER_VERSION = '4.1'

	String name
	String namespace
	String groupId = DEFAULT_GROUP
	
	String vendor = "RapidMiner GmbH"
	String homepage = "www.rapidminer.com"

	String extensionFolder

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
			if(name){
				return name.toLowerCase().replace(" ", "_")
			} else {
				throw new GradleException("Neither an extension name nor a namspace defined!")
			}
		}
		return namespace
	}
}
