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

import groovy.xml.QName
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * A Task that creates a new operator.
 *
 * @author Nils Woehler
 *
 */
class OperatorCreation extends DefaultTask {


    @TaskAction
    def createOperator() {


        def operatorName = project.hasProperty("opName") ? project.opName : ''
        if (!operatorName) throw new GradleException("No operator name specified. Specify via -PopName=%NAME.")

        def operatorKey = project.hasProperty("opKey") ? project.opKey : operatorName.replace(' ', '_').toLowerCase()
        def operatorGroup = project.hasProperty("opGroup") ? project.opGroup : ''
        def operatorGroups = operatorGroup.split('\\.')

        def packageName = "${project.extensionConfig.groupId}.operator"
        def operatorClassName = "${operatorName.capitalize().replace(' ', '')}Operator"

        // TODO check if file exists
        def xmlFilePath = "src/main/resources/${project.extensionConfig.resources.operatorDefinition}"
        File operatorsXmlFile = project.file(xmlFilePath)

        XmlParser parser = new XmlParser()
        Node root = parser.parseText(operatorsXmlFile.text)

        // lookup specified group (and create new ones if necessary
        def parentNode = root
        operatorGroups.each {
            def groupNode = parentNode.group.find { n -> n.@key == it }

            // create new node if group not found
            if (!groupNode) {
                groupNode = parser.createNode(parentNode, new QName("group"), [key: it])
            }
            if(groupNode){
                parentNode = groupNode
            }
        }

        if(!parentNode.operator.'**'.key.find { it.text() == operatorKey }) {
            def operatorNode = parser.createNode(parentNode, new QName("operator"), [:])
            operatorNode.appendNode(new QName("key"), [:], operatorKey)
            operatorNode.appendNode(new QName("class"), [:], "${packageName}.${operatorClassName}")

            // write updated XML to file
            operatorsXmlFile.text = XmlUtil.serialize(root)
        } else {
            project.logger.info "Skipping Operators XML update as operator with key $operatorKey is already present."
        }

        // copy template class file to correct package
        def opPackagePath = "src/main/java/${packageName.replace('.', '/')}"
        createFolder(opPackagePath)

        def opTemplate = replaceOperatorPlaceholders(getResourceAsText("TemplateOperator.java"), packageName, operatorClassName)

        def fullOpClassPath = "${opPackagePath}/${operatorClassName}.java"
        project.file(fullOpClassPath).parentFile.mkdirs()
        project.file(fullOpClassPath).text = opTemplate

        // copy template documentation to correct package
        def opDocPackagePath = "src/main/resources/${project.extensionConfig.namespace}/${operatorGroup.replace('.', '/')}"
        createFolder(opDocPackagePath)

        def opDocTemplate = replaceOperatorDocPlaceholders(getResourceAsText("TemplateDoc.xml"),
                project.extensionConfig.namespace, operatorKey, operatorName)

        def fullOpDocPath = "${opDocPackagePath}/${operatorKey}.xml"
        project.file(fullOpDocPath).parentFile.mkdirs()
        project.file(fullOpDocPath).text = opDocTemplate


        // adapt operator name

        def docBundle = new XmlSlurper().parse(operatorsXmlFile)?.attributes()?.get('docbundle')
        def docBundleFile = project.file("src/main/resources/" + docBundle + ".xml")

        XmlParser docParser = new XmlParser()
        def docRoot = docParser.parseText(docBundleFile.text)

        // add operator to doc file if it is not present yet
        if(!docRoot.'**'.key.find { it.text == operatorKey}) {
            def operatorNode = docParser.createNode(docRoot, new QName("operator"), [:])
            operatorNode.appendNode(new QName("key"), [:], operatorKey)
            operatorNode.appendNode(new QName("name"), [:], operatorName)

            // write updated XML to file
            docBundleFile.text = XmlUtil.serialize(docRoot)
        }
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

    private getResourceAsText(String relativePath) {
        def fullPath = "/com/rapidminer/extension/template/$relativePath"
        logger.debug "Fetching resource at relative path $relativePath. Full path is: $fullPath"
        def resourceStream = ExtensionInitialization.getResourceAsStream(fullPath)
        if (!resourceStream) {
            throw new GradleException("Resource $fullPath not found!")
        }
        return resourceStream.text
    }

    private String replaceOperatorPlaceholders(String text, String packageName, String className) {
        text = text.replaceAll('\\$OPERATOR_NAME\\$', className)
        text = text.replaceAll('\\$PACKAGE_NAME\\$', packageName)
        return text
    }

    private String replaceOperatorDocPlaceholders(String text, String namespace, String opKey, String opName) {
        text = text.replaceAll('\\$NAMESPACE\\$', namespace)
        text = text.replaceAll('\\$OPERATOR_KEY\\$', opKey)
        text = text.replaceAll('\\$OPERATOR_NAME\\$', opName)
        return text
    }


}
