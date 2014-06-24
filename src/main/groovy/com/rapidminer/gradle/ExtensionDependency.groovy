package com.rapidminer.gradle;

/**
 * Extension dependency definition
 * 
 * @author Nils Woehler
 *
 */
public class ExtensionDependency {
	
	//FIXME remove .release
	public static final String DEFAULT_GROUP = 'com.rapidminer.extension.release'
	
	String group = DEFAULT_GROUP
	String namespace
	String version
}
