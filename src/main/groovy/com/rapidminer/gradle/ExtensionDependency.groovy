package com.rapidminer.gradle;

/**
 * Extension dependency definition
 * 
 * @author Nils Woehler
 *
 */
public class ExtensionDependency {
	
	public static final String DEFAULT_GROUP = 'com.rapidminer.extension'
	
	String group = DEFAULT_GROUP
	String namespace
	String version
}
