/*
 * Copyright 2013-2014 RapidMiner GmbH.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.publish.maven.MavenPublication



/**
 *
 * @author Nils Woehler
 *
 */
class RapidMinerExtensionPlugin implements Plugin<Project> {

	private static final String EXTENSION_GROUP = "RapidMiner Extension"

	private static final String RMX = "rmx_"

	private static final String DEFAULT_JAVA_PATH = "src/main/java/";
	private static final String DEFAULT_RESOURCE_PATH = "src/main/resources/";
	private static final String RAPIDMINER_PACKAGE = "com/rapidminer/";
	private static final String RESOURCE_PACKAGE = RAPIDMINER_PACKAGE + "resources/"
	private static final String I18N_PATH = "i18n/"

	private static final String INIT_CLASS_PREFIX = "PluginInit"

	private static final String JAVA_EXTENSION = ".java"
	private static final String XML_EXTENSION = ".xml"
	private static final String PROPERTIES_EXTENSION = ".properties"

	@Override
	void apply(Project project) {

		// create 'extension' project extension
		project.extensions.create("extensionConfig", ExtensionConfiguration)

		configureProject(project);
	}

	/**
	 * Configures the project with default gradle plugins and provided configurations.
	 */
	void configureProject(Project project) {
		project.configure(project) {

			// extension project and subprojects are java projects
			allprojects {
				apply plugin: 'com.rapidminer.gradle.java-basics'
				apply plugin: 'com.rapidminer.gradle.code-quality'
			}

			// shadowJar is being used to create a shaded extension jar
			apply plugin: 'com.github.johnrengelman.shadow'
			apply plugin: 'com.rapidminer.gradle.release'

			defaultTasks 'installExtension'

			// Create 'install' task, will be configured later
			// Copies extension jar created by 'jar' task to the '/lib/plugins' directory of RapidMiner
			def installTask = tasks.create(name:'installExtension', type: org.gradle.api.tasks.Copy, dependsOn: 'shadowJar')
			installTask.group = EXTENSION_GROUP
			installTask.description = "Create a jar bundled with all dependencies and copies the jar"+
					" to '%rapidminerHome/lib/plugins'. %rapidminerHome can be configured by changing"+
					" 'extensionConfig { rapidminerHome '...' }'. Default is '../rapidminer-studio'."

			// configure install task
			installExtension {
				into "${->extensionConfig.rapidminerHome}/lib/plugins"
				from shadowJar
			}

			// create publish all task
			def publishLibAndRelease = tasks.create(name: 'publishExtension', dependsOn: 'publishExtensionJarPublicationToMavenRepository')
			publishLibAndRelease.group = EXTENSION_GROUP
			publishLibAndRelease.description = "Publishes extension .jar and shaded extension .jar to configured Maven repository."

			// add and configure Gradle wrapper task
			tasks.create(name: 'wrapper', type: org.gradle.api.tasks.wrapper.Wrapper)
			wrapper { gradleVersion = "${->extensionConfig.wrapperVersion}" }

			// define extension group as lazy GString
			// see http://forums.gradle.org/gradle/topics/how_do_you_delay_configuration_of_a_task_by_a_custom_plugin_using_the_extension_method
			group = "${->project.extensionConfig.groupId}"

			// define Maven publications
			publishing {
				publications {
					extensionJar(MavenPublication) {
						from components.java
						artifact shadowJar { classifier 'all' }
						artifactId "${->project.extensionConfig.namespace}"
					}
				}
			}

			// Configuring the properties below can only be accomplished after
			// the project extension 'extension' has been configured
			afterEvaluate {

				//TODO do these sanity checks only when jar is created (as import of project without an extension name does not work)
				// first sanity checks
				if(!project.extensionConfig.name){
					throw new RuntimeException("No RapidMiner Extension name defined. Define via 'extensionConfig { name $NAME }'.")
				}

				if(!project.extensionConfig.groupId) {
					throw new RuntimeException("No groupdId defined! Define via 'extensionConfig { groupdId $GROUPID }'. (default: 'com.rapidminer.extension')")
				}

				// add RapidMiner and configured extensions as dependency to all projects
				allprojects {
					dependencies {
						provided getRapidMinerDependency(project)
						extensionConfig.dependencies.extensions.each{  e ->
							provided group: e.group, name: e.namespace, version: e.version
						}
					}
				}

				// TODO create tasks that checks entries instead of doing it in the evaluation phase
				// TODO jar should depend on this new task
				// add check for manifest entries to avoid generic 'null' error
				checkReleaseManifestEntries(project)

				// configure create extension release task
				jar {
					// configure manifest
					manifest {
						attributes(
								"Manifest-Version": 		"1.0",
								"Implementation-Vendor": 	project.extensionConfig.vendor,
								"Implementation-Title":		project.extensionConfig.name,
								"Implementation-URL":		project.extensionConfig.homepage,
								"Implementation-Version": 	project.version,
								"Specification-Title": 		project.extensionConfig.name,
								"Specification-Version":	project.version,
								"RapidMiner-Version":		getRapidMinerVersion(project),
								"RapidMiner-Type":			"RapidMiner_Extension",
								"Plugin-Dependencies":		getExtensionDependencies(project),

								// Definition of important files
								"Extension-ID":					RMX + project.extensionConfig.namespace,
								"Namespace":					project.extensionConfig.namespace,
								"Initialization-Class":			project.extensionConfig.resources.initClass,
								"IOObject-Descriptor":			project.extensionConfig.resources.objectDefinition,
								"Operator-Descriptor":			project.extensionConfig.resources.operatorDefinition,
								"ParseRule-Descriptor":			project.extensionConfig.resources.parseRuleDefinition,
								"Group-Descriptor":				project.extensionConfig.resources.groupProperties,
								"Error-Descriptor":				project.extensionConfig.resources.errorDescription,
								"UserError-Descriptor":			project.extensionConfig.resources.userErrors,
								"GUI-Descriptor":				project.extensionConfig.resources.guiDescription,
								"Settings-Descriptor":			project.extensionConfig.resources.settingsDescriptor,
								"SettingsStructure-Descriptor":	project.extensionConfig.resources.settingsStructureDescriptor
								)
					}
				}

				// ensure provided dependencies are not compiled into shadowJar
				shadowJar {
					dependencies {
						exclude(dependency(configurations.provided))
					}
				}
			}
		}
	}

	def getArtifactId(Project project){
		return project.extensionConfig.namespace
	}

	/**
	 * Checks for existence of user defined resource objects. If no objects are defined by the user
	 *
	 */
	def checkReleaseManifestEntries(Project project) {
		assert project.version
		assert project.extensionConfig.vendor

		def res = project.extensionConfig.resources
		def name = project.extensionConfig.name.replace(" ", "")
		def logger = project.logger

		res.initClass = checkInitClass(project, res, name, logger)
		res.operatorDefinition = checkResourceFile("Operators", XML_EXTENSION, res.operatorDefinition, project, res, name, logger, true)
		res.objectDefinition = checkResourceFile("ioobjects", XML_EXTENSION, res.objectDefinition, project, res, name, logger)
		res.parseRuleDefinition = checkResourceFile("parserules", XML_EXTENSION, res.parseRuleDefinition, project, res, name, logger)
		res.groupProperties = checkResourceFile("groups", PROPERTIES_EXTENSION, res.groupProperties, project, res, name, logger)
		res.errorDescription = checkResourceFile("Errors", PROPERTIES_EXTENSION, res.errorDescription, project, res, name, logger, false, I18N_PATH)
		res.userErrors = checkResourceFile("UserErrorMessage", PROPERTIES_EXTENSION, res.userErrors, project, res, name, logger, false,  I18N_PATH)
		res.guiDescription = checkResourceFile("GUI", PROPERTIES_EXTENSION, res.guiDescription, project, res, name, logger,  false, I18N_PATH)
		res.settingsStructureDescriptor = checkResourceFile("settings", XML_EXTENSION, res.settingsStructureDescriptor, project, res, name, logger,  false)
		res.settingsDescriptor = checkResourceFile("Settings", PROPERTIES_EXTENSION, res.settingsDescriptor, project, res, name, logger,  false, I18N_PATH)
	}

	def checkInitClass(Project project, res, name, logger){
		// Check if init class is user defined
		if(!res.initClass){
			def defaultFileName = DEFAULT_JAVA_PATH + RAPIDMINER_PACKAGE + INIT_CLASS_PREFIX + name + JAVA_EXTENSION
			if(project.file(defaultFileName).exists()){
				logger.info("Found default init class: '" + defaultFileName + "'")
				return RAPIDMINER_PACKAGE.replace("/", ".") + INIT_CLASS_PREFIX + name
			} else {
				logger.info("Default init class  '" + defaultFileName + "' not found. "
						+"Searching for alternatives in src/main/java...")

				// Create a file tree with a base directory
				FileTree tree = project.fileTree(dir: DEFAULT_JAVA_PATH, include: '**/*' + JAVA_EXTENSION)

				// Iterate over the contents of a tree
				def initCandidate = null
				tree.find { File file ->
					if(file.getName().contains(INIT_CLASS_PREFIX)){
						logger.info("Found potential init class: " + file.getPath())
						def idx = file.getPath().indexOf(DEFAULT_JAVA_PATH.replace("/", File.separator))
						initCandidate = file.getPath()
								.substring(idx + DEFAULT_JAVA_PATH.length())
								.replace(JAVA_EXTENSION, "")
								.replace(File.separator, ".")
						return true // take this one
					}
					return false // not found yet
				}

				// Still not found?
				if(initCandidate == null){
					throw new RuntimeException("No init class candidate found!")
				}
				logger.info("Selected init class: '"+ initCandidate+"'")
				return initCandidate
			}
		} else {
			// check if user defined init class exists
			def initClassFile = project.file(DEFAULT_JAVA_PATH + res.initClass.replace(".", File.separator) + ".java")
			if(!initClassFile.exists()){
				throw new RuntimeException("Defined extension init class '"+ initClassFile +"' does not exist!")
			}
			return res.initClass // use the user-defined one
		}
	}


	def checkResourceFile(String resourceName, String suffix, userDefinedResource, Project project, res, name, logger, boolean mandatory = false, String subdirectory = ""){
		// Check if resource file is user defined
		if(!userDefinedResource){
			def defaultResourceFile = RESOURCE_PACKAGE + subdirectory + resourceName + name + suffix
			if(project.file(DEFAULT_RESOURCE_PATH + defaultResourceFile).exists()){
				logger.info("Found default " + resourceName + " resource file: '" + defaultResourceFile + "'")
				return defaultResourceFile
			} else {
				logger.info("Default " + resourceName + " resource file '" + defaultResourceFile + "' not found."
						+" Searching for alternatives in "+ DEFAULT_RESOURCE_PATH + "...")

				// Create a file tree with a base directry
				FileTree tree = project.fileTree(dir: DEFAULT_RESOURCE_PATH, include: '**/*' + suffix)

				// Iterate over the contents of a tree
				def resourceCandidate = null
				tree.find { File file ->
					if(file.getName().contains(resourceName)){
						logger.info("Found potential " + resourceName + " resource file: " + file.getPath())
						def idx = file.getPath().indexOf(DEFAULT_RESOURCE_PATH.replace("/", File.separator))
						resourceCandidate = file.getPath()
								.substring(idx + DEFAULT_RESOURCE_PATH.length())
								.replace(File.separator, "/")
						return true // take this one
					}
					return false // not found yet
				}

				// Still not found?
				if(resourceCandidate == null){
					if(mandatory){
						throw new RuntimeException("Mandatory " + resourceName + " resource file is missing. No candidate found!")
					} else {
						resourceCandidate = ""
					}
					logger.info("No optional " + resourceName + " resource file found. Skipping...")
				} else {
					logger.info("Selected " + resourceName + " resource file: '"+ resourceCandidate+"'")
				}
				return resourceCandidate
			}
		} else {
			def fileName = DEFAULT_RESOURCE_PATH + userDefinedResource
			def resourceFile = project.file(fileName)
			if(!resourceFile.exists()){
				throw new RuntimeException("Selected " + resourceName + " resource file '"+ resourceFile +"' does not exist!")
			}
		}
	}

	def getExtensionDependencies(Project project) {
		String deps = ""
		project.extensionConfig.dependencies.extensions.eachWithIndex{  e, i ->
			if(i != 0){
				deps += "; "
			}
			deps += RMX + e.namespace + "[" + e.version + "]"
		}
		return deps
	}

	def getRapidMinerDependency(Project project) {
		def version = getRapidMinerVersion(project)
		if(project.extensionConfig.dependencies.useAntArtifact){
			return "com.rapidminer.studio:rapidminer:" + version
		} else {
			return "com.rapidminer.studio:rapidminer-studio-core:" + version
		}
	}

	def getRapidMinerVersion(Project project) {
		assert project.extensionConfig.dependencies.rapidminer
		return  project.extensionConfig.dependencies.rapidminer
	}

}
