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
			apply plugin: 'rapidminer-publish'
			allprojects { apply plugin: 'rapidminer-java-basics' }

			defaultTasks 'install'

			// Create 'createExtensionBundle' task that will create an extension release jar
			def createTask = tasks.create(name: 'createExtensionRelease', type: org.gradle.api.tasks.bundling.Jar)
			createTask.group = EXTENSION_GROUP
			createTask.description = "Creates an extension release .jar file."+
					" The .jar will contain all specified compile and runtime dependencies."

			// Create 'install' task, will be configured later
			// Copies extension jar created by 'jar' task to the '/lib/plugins' directory of RapidMiner
			def installTask = tasks.create(name:'install', type: org.gradle.api.tasks.Copy, dependsOn: 'createExtensionRelease')
			installTask.group = EXTENSION_GROUP
			installTask.description = "Create a jar bundled with all dependencies and copies the jar"+
					" to '%rapidminerHome/lib/plugins'. %rapidminerHome can be configured by changing"+
					" 'extensionConfig { rapidminerHome '...' }'. Default is '../rapidminer-studio'."

			// configure install task
			install {
				into "${->extensionConfig.rapidminerHome}/lib/plugins"
				from createExtensionRelease
			}

			// create publish all task
			def publishLibAndRelease = tasks.create(name: 'publishExtensionToArtifactory', dependsOn: [
				'publishExtensionLibPublicationToMavenRepository',
				'artifactoryPublish'
			])
			publishLibAndRelease.group = EXTENSION_GROUP
			publishLibAndRelease.description = "Publishes extension lib .jar and extension release .jar to Artifactory."

			// add lib appendix for extension without bundled dependencies
			jar { appendix = "lib" }

			// create sources jar task
			//task sourceJar(type:  org.gradle.api.tasks.bundling.Jar) {  from sourceSets.main.allJava  }

			// define extension group as lazy GString
			// see http://forums.gradle.org/gradle/topics/how_do_you_delay_configuration_of_a_task_by_a_custom_plugin_using_the_extension_method
			group = "${->project.extensionConfig.groupId}"

			// define extension release and snapshot repositories
			uploadConfig {
				releaseRepo 'applications-release-local'
				snapshotRepo 'applications-snapshot-local'
				contextUrl 'https://gitlab.rapid-i.com/artifactory/'
				publication 'extensionRelease'
			}

			// define Maven publications
			publishing {
				publications {
					extensionLib(MavenPublication) {
						from components.java
						artifactId "${->project.extensionConfig.namespace}"
						//artifact sourceJar { classifier "source" }
					}
					extensionRelease(MavenPublication) {
						artifact createExtensionRelease
						artifactId "${->project.extensionConfig.namespace}"
						groupId "${->project.extensionConfig.groupId}.release"
					}
				}
				repositories {
					maven {
						credentials {
							username = "${artifactory_user}"
							password = "${artifactory_password}"
						}
						if(project.version.endsWith('-SNAPSHOT')){
							url "https://gitlab.rapid-i.com/artifactory/libs-snapshot-local"
						} else {
							url "https://gitlab.rapid-i.com/artifactory/libs-release-local"
						}
					}
				}
			}

			// Configuring the properties below can only be accomplished after
			// the project extension 'extension' has been configured
			afterEvaluate {

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

				// add check for manifest entries to avoid generic 'null' error
				checkReleaseManifestEntries(project)

				// configure create extension release task
				createExtensionRelease {
					// bundle extension classes and compile dependencies
					from sourceSets.main.output
					from ({configurations.runtime.collect {it.isDirectory() ? it : zipTree(it)}}) {
						// remove all signature files
						exclude "META-INF/*.SF"
						exclude "META-INF/*.DSA"
						exclude "META-INF/*.RSA"
					}
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
								"Extension-ID":				RMX + project.extensionConfig.namespace,
								"Namespace":				project.extensionConfig.namespace,
								"Initialization-Class":		project.extensionConfig.resources.initClass,
								"IOObject-Descriptor":		project.extensionConfig.resources.objectDefinition,
								"Operator-Descriptor":		project.extensionConfig.resources.operatorDefinition,
								"ParseRule-Descriptor":		project.extensionConfig.resources.parseRuleDefinition,
								"Group-Descriptor":			project.extensionConfig.resources.groupProperties,
								"Error-Descriptor":			project.extensionConfig.resources.errorDescription,
								"UserError-Descriptor":		project.extensionConfig.resources.userErrors,
								"GUI-Descriptor":			project.extensionConfig.resources.guiDescription
								)
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
		return "com.rapidminer.studio:rapidminer:" + getRapidMinerVersion(project)
	}

	def getRapidMinerVersion(Project project) {
		assert project.extensionConfig.dependencies.rapidminer
		return  project.extensionConfig.dependencies.rapidminer
	}

}
