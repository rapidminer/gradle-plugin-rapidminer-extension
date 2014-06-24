package com.rapidminer.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 *
 * @author Nils Woehler
 *
 */
class RapidMinerExtensionPlugin implements Plugin<Project> {

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
//			apply plugin: 'rapidminer-publish'
			allprojects { apply plugin: 'rapidminer-java-basics' }

			defaultTasks 'install'

			//FIXME add tasks groupd and description
			// Create 'createExtensionBundle' task that will create an extension release jar
			tasks.create(name: 'createExtensionRelease', type: org.gradle.api.tasks.bundling.Jar)

			// Create 'install' task, will be configured later
			// Copies extension jar created by 'jar' task to the '/lib/plugins' directory of RapidMiner
			tasks.create(name:'install', type: org.gradle.api.tasks.Copy, dependsOn: 'createExtensionRelease')

			// Configuring the properties below can only be accomplished after
			// the project extension 'extension' has been configured
			afterEvaluate {

				// check if extension name has been defined
				assert project.extensionConfig.name

				// create namespace from extension name if no namespace has been defined
				if(!extensionConfig.namespace) {
					extensionConfig.namespace = extensionConfig.name.toLowerCase().replace(" ", "-");
				}

				// define extension vendor as publishing group
				group = extensionConfig.vendor

				// add RapidMiner as dependency to all projects
				allprojects {
					dependencies {
						provided getRapidMinerDependency(project)
						extensionConfig.dependencies.extensions.each{  e ->
							provided group: e.group, name: e.namespace, version: e.version
						}
					}
				}


				// define publish repositories and publications to upload extension as library
//				uploadConfig {
//					releaseRepo 'applications-release-local'
//					snapshotRepo 'applications-snapshot-local'
//					contextUrl 'https://gitlab.rapid-i.com/artifactory/'
//					publication 'extensionRelease'
//				}

				// define Maven publications
//				publishing {
//					publications {
//						extensionLib(MavenPublication) {
//							from components.java
//							artifactId extensionConfig.namespace
//						}
//						extensionRelease(MavenPublication) {
//							from createExtensionRelease
//							artifactId extensionConfig.namespace
//							groupId project.group + ".release"
//						}
//					}
//				}

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
								"Extension-ID":				"rmx_" + project.extensionConfig.namespace,
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

				// configure install task
				install {
					into extensionConfig.rapidminerHome + "/lib/plugins"
					from createExtensionRelease
				}

				// add lib appendix for extension without bundled dependencies
				jar { appendix = "lib" }
			}
		}
	}

	/**
	 * Checks
	 * @param project
	 * @return
	 */
	def checkReleaseManifestEntries(Project project) {
		assert project.version
		assert project.extensionConfig.name
		assert project.extensionConfig.namespace
		assert project.extensionConfig.vendor

		// extensions must specify init class and operator definition file
		assert project.extensionConfig.resources.initClass
		assert project.extensionConfig.resources.operatorDefinition
		assert project.extensionConfig.resources.initClass
		assert project.extensionConfig.resources.objectDefinition != null
		assert project.extensionConfig.resources.operatorDefinition != null
		assert project.extensionConfig.resources.parseRuleDefinition != null
		assert project.extensionConfig.resources.groupProperties != null
		assert project.extensionConfig.resources.errorDescription != null
		assert project.extensionConfig.resources.userErrors != null
		assert project.extensionConfig.resources.guiDescription != null
	}

	def getExtensionDependencies(Project project) {
		String deps = ""
		project.extensionConfig.dependencies.extensions.each{  e ->
			deps += ","+e.namespace+"_["+e.version+"]"
		}
		// remove first comma (if necessary)
		if(deps.length() > 0) {
			deps = deps.substring(1)
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
