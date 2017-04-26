package com.hadleym.watson;

import org.apache.lucene.store.Directory;

public class Constants {
	public static final boolean DEBUG = false;
//	public static final String FILES_TO_INDEX = "files_testing";
	public static final String FILES_TO_INDEX = "files";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_CATEGORY = "category";
	public static final String INDEX_DIR = "index";
	public static final boolean recreate = true;
	public static final int HITSPERPAGE = 10;
	public static final String PPFILES_DIR = "PPFILES";
	public static final String INDEX_TEST_DIR = "index_test";
	public static final String PPFILES_TEST = "PPFILES_TEST";
}
