package com.rapidminer.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy


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
			apply plugin: 'java'
			apply plugin: 'eclipse'
			apply plugin: 'base'
			
			defaultTasks 'install'

			// minimize changes, at least for now (gradle uses 'build' by default)
			buildDir = "target"

			// ###################
			// Create Maven like provided configuration
			// See http://issues.gradle.org/browse/GRADLE-784
			configurations { provided }

			sourceSets {
				main.compileClasspath += configurations.provided
				test.compileClasspath += configurations.provided
				test.runtimeClasspath += configurations.provided
			}

			eclipse.classpath.plusConfigurations += configurations.provided
			// ####################
			
			// Create 'install' task, will be configured later
			// Copies extension jar created by 'jar' task to the '/lib/plugins' directory of RapidMiner
			tasks.create(name:'install', type: Copy, dependsOn: 'jar')
			
			// Configuring the properties below can only be accomplished after 
			// 'extension' has been configured
			afterEvaluate {

				sourceCompatibility = extension.javaTarget
				targetCompatibility = extension.javaTarget

				group = extension.vendor

				// add RapidMiner as dependency
				dependencies { provided "com.rapidminer.studio:rapidminer:" + project.extension.rapidminerVersion }

				// configure install task
				install {
					into extension.rapidminerHome + "/lib/plugins"
					from jar
				}
				
				// configure jar output
				jar {
					// if activated add runtime dependencies to jar package
					if(project.extension.bundleDependencies) {
						dependsOn configurations.runtime
						from ({configurations.runtime.collect {it.isDirectory() ? it : zipTree(it)}}) {
							// remove all signature files
							exclude "META-INF/*.SF"
							exclude "META-INF/*.DSA"
							exclude "META-INF/*.RSA"
						}
					}
					manifest {
						attributes(
								"Manifest-Version": 		"1.0",
								"Implementation-Vendor": 	project.extension.vendor,
								"Implementation-Title":		project.extension.longName,
								"Implementation-URL":		project.extension.homepage,
								"Implementation-Version": 	version,
								"Specification-Title": 		project.extension.longName,
								"Specification-Version":	version,
								"RapidMiner-Version":		project.extension.rapidminerVersion,
								"RapidMiner-Type":			"RapidMiner_Extension",
								"Plugin-Dependencies":		project.extension.extensionDependencies,

								// Definition of important files
								"Extension-ID":				"rmx_" + project.extension.namespace,
								"Namespace":				project.extension.namespace,
								"Initialization-Class":		initClass,
								"IOObject-Descriptor":		objectDefinition,
								"Operator-Descriptor":		operatorDefinition,
								"ParseRule-Descriptor":		parseRuleDefinition,
								"Group-Descriptor":			groupProperties,
								"Error-Descriptor":			errorDescription,
								"UserError-Descriptor":		userErrors,
								"GUI-Descriptor":			guiDescription
								)
					}
				}
			}
		}
	}

}
