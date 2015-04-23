## Change Log

#### 0.7.0
* Added an extension initialization task which creates a fresh extension project setup
* Change shadow plugin to latest version (1.2.1) which is compatible with Gradle 2.2+
* Changes default Gradle wrapper version to 2.3
* Applying the plugin will only affect the configuration of the project the plugin was applied to. Subprojects need to be configured separately.
* Added a task that prepares a RapidMiner Home environment for extension process tests 
* Added RapidMiner Studio core test artifact as testCompile dependency for running process tests without cloning the RapidMiner Studio Git repository
* Replaced 'rapidminerHome' extensionConfig property with 'extensionFolder' property. 
  Extensions are now installed into ~/.RapidMiner/extensions by default if the 'extensionFolder' property is not set.

#### 0.6.2
* Downgraded shadow plugin to version 1.0.3 (breaks build with Gradle 2.2)
* Do not configure remote Maven repository if credentials and URL aren't set

#### 0.6.1
* Adds shortened plugin name 'com.rapidminer.extension' to comply with plugins.gradle.org standards

#### 0.6.0
* (BREAKING CHANGE) Compile configuration will now extend from provided configuration. This ensures that newer artifact versions of compile configuration do overwrite older versions from provided configuration.
* Updates shadow plugin to version 1.1.2
* Updates code-quality plugin to version 0.3.4
* Manifest content will only be checked when creating a Jar, not every time when building the Gradle model
* Removes 'publishExtension' task as Jar publication was removed in version 0.5.4 (use Extension publication tasks from now on)

#### 0.5.4
* Changes default Gradle wrapper version to 2.1
* Updates java-basics plugin to version 3.0.0 (which removes superfluous publish targets)
* Updates shadow plugin to version 1.0.3

#### 0.5.3
* Adds Artifactory repository publication configuration 
* Changes namespace whitespace separator from '-' to '_'

#### 0.5.2
* Fixes error for extensions with a multi-project build setups
* Plugins applied by the extension plugin will only be applied to the project which applies the extension plugin  

#### 0.5.1
* Adds Gradle 2.1 compatible plugin name 'com.rapidminer.gradle.extension'

#### 0.5.0
* RapidMiner Studio dependency will by default point to new Maven artifact. Use extensionConfig { dependencies { useAntArtifact = true } } to use old Ant artifact dependency (which is available up to version 6.0.008).
* Removes lib appendix for jar created by jar task
* Updates 'rapidminer-code-quality' to version 0.2.3
* Updates 'rapidminer-release' to version 0.2.0
* Updates 'rapidminer-java-basics' to version 0.2.6
* Adds Settings-Descriptor and SettingsStructure-Descriptor Jar properties

#### 0.4.1
* Updates 'rapidminer-code-quality' to version 0.2.1
* Fixes dependencies from provided scope being compiled into 'all' jar

#### 0.4.0
* Applies 'rapidminer-release' with version 0.1.3
* Updates 'rapidminer-code-quality' to version 0.2.0

#### 0.3.1
* Updates 'rapidminer-code-quality' to version 0.1.1 which adds prependHeader tasks

#### 0.3.0
* Rename 'install' task to 'instsallExtension'
* Apply 'rapidminer-code-quality' plugin to all projects
* Updates 'rapidminer-java-basics' to version 0.2.1 which fixes the provided configuration

#### 0.2.7
* Updates 'rapidminer-java-basics' to version 0.2.0 which adds Maven publishing
* Updates 'shadowJar' plugin to version 1.0.2 to fix Gradle 2.0 compile errors

#### 0.2.6
* Updates 'rapidminer-java-basics' to latest version to fix Gradle 2.0 compile errors

#### 0.2.4
* Adds default Gradle Wrapper task called 'wrapper'
* Use 'shadowJar' Gradle plugin for building extension releases (much faster than doing it the old way)
* Removes dependency from Artifactory Gradle plugin, only use native 'maven-publish' plugin instead

#### 0.2.3
* Fixes wrong multi-extension dependency declaration in MANIFEST.MF 

#### 0.2.2
* Fixes error when guessing non default named resource files 

#### 0.2.1
* Fixes "null" namespaces when uploading artifacts with no namespace defined

#### 0.2.0
* Adds extension publishing tasks that allow to upload lib and release to artifactory

#### 0.1.1
* Removes extensionConfig.admin
* Adds extensionConfig.groupId  (default: com.rapidminer.extension)
* extensionConfig.homepage is not mandatory anymore (default: www.rapidminer.com)
* extensionConfig.vendor is not mandatory anymore (default: RapidMiner GmbH)

#### 0.1.0 
* Extension release
