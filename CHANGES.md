## Change Log
#### 0.1.0 
* Extension release
#### 0.1.1
* Removes extensionConfig.admin
* Adds extensionConfig.groupId  (default: com.rapidminer.extension)
* extensionConfig.homepage is not mandatory anymore (default: www.rapidminer.com)
* extensionConfig.vendor is not mandatory anymore (default: RapidMiner GmbH)
#### 0.2.0
* Adds extension publishing tasks that allow to upload lib and release to artifactory
#### 0.2.1
* Fixes "null" namespaces when uploading artifacts with no namespace defined
#### 0.2.2
* Fixes error when guessing non default named resource files 
#### 0.2.3
* Fixes wrong multi-extension dependency declaration in MANIFEST.MF 
#### 0.2.4
* Adds default Gradle Wrapper task called 'wrapper'
* Use 'shadowJar' Gradle plugin for building extension releases (much faster than doing it the old way)
* Removes dependency from Artifactory Gradle plugin, only use native 'maven-publish' plugin instead
#### 0.2.6
* Updates 'rapidminer-java-basics' to latest version to fix Gradle 2.0 compile errors
#### 0.2.7
* Updates 'rapidminer-java-basics' to version 0.2.0 which adds Maven publishing
* Updates 'shadowJar' plugin to version 1.0.2 to fix Gradle 2.0 compile errors
#### 0.3.0
* Rename 'install' task to 'instsallExtension'
* Apply 'rapidminer-code-quality' plugin to all projects
* Updates 'rapidminer-java-basics' to version 0.2.1 which fixes the provided configuration
#### 0.3.1
* Updates 'rapidminer-code-quality' to version 0.1.1 which adds prependHeader tasks
