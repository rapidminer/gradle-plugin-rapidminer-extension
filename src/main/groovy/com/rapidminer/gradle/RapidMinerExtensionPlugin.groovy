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
		project.extensions.create("extension", ExtensionDefinition)

		configureProject(project);
	}

	/**
	 * Configures the project with default gradle plugins and provided configurations.
	 */
	void configureProject(Project project) {
		project.configure(project) {

			// extension project and subprojects are java projects
			apply plugin: 'rapidminer-java-basics'
			//			apply plugin: 'rapidminer-publish'
			subprojects { apply plugin: 'rapidminer-java-basics' }

			defaultTasks 'install'

			// Create 'createExtensionBundle' task that will create an extension release jar
			tasks.create(name: 'createExtensionRelease', type: org.gradle.api.tasks.bundling.Jar)

			// Create 'install' task, will be configured later
			// Copies extension jar created by 'jar' task to the '/lib/plugins' directory of RapidMiner
			tasks.create(name:'install', type: org.gradle.api.tasks.Copy, dependsOn: 'createExtensionRelease')

			// Configuring the properties below can only be accomplished after
			// the project extension 'extension' has been configured
			afterEvaluate {

				// check if extension name has been defined
				assert project.extension.name

				// create namespace from extension name if no namespace has been defined
				if(!extension.namespace) {
					extension.namespace = extension.name.toLowerCase().replace(" ", "-");
				}

				// define extension vendor as publishing group
				group = extension.vendor

				// add RapidMiner as dependency to all extension projects (parent+subprojects)
				dependencies {
					provided "com.rapidminer.studio:rapidminer:" + project.extension.rapidminerVersion
				}
				subprojects {
					dependencies {
						provided "com.rapidminer.studio:rapidminer:" + project.extension.rapidminerVersion
					}
				}


				//TODO if defined use artifactId, else use namespace
				//				// define publish repositories and publications to upload extension as library
				//				publish {
				//					releaseRepo 'libs-release-local'
				//					snapshotRepo 'libs-snapshot-local'
				//					publication 'extensionLib'
				//				}
				//
				//				// define Maven publication
				//				publishing {
				//					publications {
				//						extensionLib(MavenPublication) {
				//							from components.java
				//
				//							if(extension.artifactId){
				//								artifactId extension.artifactId
				//							}
				//						}
				//						extensionRelease(MavenPublication) {
				//							from components.java
				//
				//							if(extension.artifactId){
				//								artifactId extension.artifactId
				//							}
				//						}
				//					}
				//				}

				// add check for manifest entries to avoid generic 'null' error
				checkReleaseManifestEntries(project)

				// configure create extension release task
				createExtensionRelease {
					appendix = "uberJar"

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
								"Implementation-Vendor": 	project.extension.vendor,
								"Implementation-Title":		project.extension.name,
								"Implementation-URL":		project.extension.homepage,
								"Implementation-Version": 	project.version,
								"Specification-Title": 		project.extension.name,
								"Specification-Version":	project.version,
								"RapidMiner-Version":		project.extension.rapidminerVersion,
								"RapidMiner-Type":			"RapidMiner_Extension",
								"Plugin-Dependencies":		project.extension.extensionDependencies,

								// Definition of important files
								"Extension-ID":				"rmx_" + project.extension.namespace,
								"Namespace":				project.extension.namespace,
								"Initialization-Class":		project.extension.resources.initClass,
								"IOObject-Descriptor":		project.extension.resources.objectDefinition,
								"Operator-Descriptor":		project.extension.resources.operatorDefinition,
								"ParseRule-Descriptor":		project.extension.resources.parseRuleDefinition,
								"Group-Descriptor":			project.extension.resources.groupProperties,
								"Error-Descriptor":			project.extension.resources.errorDescription,
								"UserError-Descriptor":		project.extension.resources.userErrors,
								"GUI-Descriptor":			project.extension.resources.guiDescription
								)
					}
				}

				// configure install task
				install {
					into extension.rapidminerHome + "/lib/plugins"
					from createExtensionRelease
				}
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
		assert project.extension.name
		assert project.extension.namespace
		assert project.extension.vendor
		assert project.extension.rapidminerVersion
		assert project.extension.extensionDependencies

		// extensions must specify init class and operator definition file
		assert project.extension.resources.initClass
		assert project.extension.resources.operatorDefinition
		assert project.extension.resources.initClass
		assert project.extension.resources.objectDefinition
		assert project.extension.resources.operatorDefinition
		assert project.extension.resources.parseRuleDefinition
		assert project.extension.resources.groupProperties
		assert project.extension.resources.errorDescription
		assert project.extension.resources.userErrors
		assert project.extension.resources.guiDescription
	}

}
