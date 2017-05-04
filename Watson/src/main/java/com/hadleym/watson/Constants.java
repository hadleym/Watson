package com.hadleym.watson;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Constants {
	public static final Analyzer analyzer = new WhitespaceAnalyzer();
	public static final boolean DEBUG = false;
//	public static final String FILES_TO_INDEX = "files_testing";
	public static final String FILES_TO_INDEX = "files";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_CATEGORY = "category";
	public static final String INDEX_DIR = "index";
	public static final boolean recreate = true;
	public static final int HITSPERPAGE = 1;
	public static final String PPFILES_DIR = "PPFILES";
	public static final String INDEX_TEST_DIR = "index_test";
	public static final String PPFILES_TEST = "PPFILES_TEST";
	public static final String[] PARTS_OF_SPEECH = {"JJ", "JJR", "JJS", "NN", "NNS", "NNP", 
			"NNPS", "RB", "RBR", "RBS", "VB", "VBD", "VBG" ,"VBN", "VBP", "VBZ"};
	public static final String[] STOP_WORDS = {"a", "an", "and", "are", "as", "at", "be", "but", "by",
			"for", "if", "in", "into", "is", "it",
			"no", "not", "of", "on", "or", "such",
			"that", "the", "their", "then", "there", "these",
			"they", "this", "to", "was", "will", "with"};
	
	public static final String CATEGORY_PREFIX = "[[";
	public static final String[] IGNORE_PREFIX = { "==", "#RED" };
	public static Directory getDirectory(Path p) {
		try { 
			return new SimpleFSDirectory(p);
		} catch (IOException io) {
			io.printStackTrace();
		}
		return null;
	}



}
