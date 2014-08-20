## Change Log

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