package com.rapidminer.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * A Task that initializes a RapidMiner Extension.
 *
 * @author Nils Woehler
 *
 */
class ExtensionInitialization extends DefaultTask {


    @TaskAction
    def initializeExtension() {

        // .gitignore
        copyResource('gitignore')
        def gitIgnoreFile = project.file('gitignore')
        gitIgnoreFile.renameTo(project.file('.gitignore'))
        gitIgnoreFile.delete()

        // create changes folder
        createFolder('changes/')
        copyResource('changes/CHANGES_1.0.000.txt')

        // create config folder
        createFolder('config/')
        copyResource('config/HEADER')

        // create license folder
        createFolder('licenses/')
        copyResource('licenses/LICENSE')

        // create test-processes folder
        createFolder('test-processes/')
        copyResource('test-processes/README')

        // create Java src folder
        createFolder('src/main/java/')
        copyResource('src/main/java/$GROUP_ID_PATH$/PluginInit$NAME$.java')

        // create resources folder
        createFolder('src/main/resources')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/groups$NAME$.properties')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/ioobjects$NAME$.xml')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/Operators$NAME$.xml')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/parserules$NAME$.xml')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/settings$NAME$.xml')

        // I18N resources
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/i18n/Errors$NAME$.properties')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/i18n/GUI$NAME$.properties')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/i18n/OperatorsDoc$NAME$.xml')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/i18n/UserErrorMessages$NAME$.properties')
        copyResource('src/main/resources/$GROUP_ID_PATH$/resources/i18n/Settings$NAME$.properties')
    }

    /**
     * Creates a folder for the provided relative folder path if it does not exist yet.
     * @param relativeFolderPath the folder path relative to the project repository
     */
    private void createFolder(String relativeFolderPath) {
        logger.info "Creating folder '$relativeFolderPath'."
        def folderFile = project.file(relativeFolderPath)
        if (!folderFile.exists()) {
            folderFile.mkdirs()
        }
    }

    /**
     * Copies a resource file to the file system maintaining the provided relative path
     * @param relativePath
     */
    private void copyResource(String relativePath) {
        if (project.file(relativePath).exists()) {
            logger.info "'$relativePath' already exists. Skipping creation."
            return
        }
        logger.info "Creating file '$relativePath'."
        writeFile(getResourceAsText(relativePath), relativePath)
    }

    /**
     * @param relativePath path relative to /resources/com/rapidminer/extension/com/
     * @return the resource as text
     */
    private getResourceAsText(String relativePath) {
        def fullPath = "/com/rapidminer/extension/template/$relativePath"
        logger.info "Fetching resource at relative path $relativePath. Full path is: $fullPath"
        def resourceStream = ExtensionInitialization.getResourceAsStream(fullPath)
        if (!resourceStream) {
            throw new GradleException("Resource $fullPath not found!")
        }
        return resourceStream.text
    }

    /**
     *
     * @param resourceStream
     * @param filePath
     */
    private void writeFile(String resourceText, String filePath) {
        def correctFilePath = replacePlaceholders(filePath)
        def resourceContent = replacePlaceholders(resourceText)

        // ensure folder exists and store file
        project.file(correctFilePath).parentFile.mkdirs()
        new FileOutputStream(correctFilePath, true).withStream {
            it.write(resourceContent.bytes)
        }
    }

    /**
     * Replaces all known placeholders with correct values for the provided text.
     *
     * @param text the text that might contain placeholders
     * @return the text without placeholders but correct values
     */
    private String replacePlaceholders(String text) {
        text = text.replaceAll('\\$VENDOR\\$', project.extensionConfig.vendor)
        text = text.replaceAll('\\$GROUP_ID\\$', project.extensionConfig.groupId)
        text = text.replaceAll('\\$GROUP_ID_PATH\\$', project.extensionConfig.groupId.replace('.', '/'))
        text = text.replaceAll('\\$NAMESPACE\\$', project.extensionConfig.namespace)

        def resourceFileName = project.extensionConfig.name.replace(' ', '')
        text = text.replaceAll('\\$NAME\\$', resourceFileName)
        text = text.replaceAll('\\$HOMEPAGE\\$', project.extensionConfig.homepage)
        text = text.replaceAll('\\$CURRENT_YEAR\\$', String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))
        text = text.replaceAll('\\$DOC_BUNDLE\\$',
                "${project.extensionConfig.groupId.replace('.', '/')}/resources/i18n/OperatorsDoc$resourceFileName")
        return text
    }

}
