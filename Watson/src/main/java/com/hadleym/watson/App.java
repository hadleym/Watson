package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

public class App {
	public static final boolean DEBUG = true;
	public static final String FILES_TO_INDEX = "files";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "cotents";
	public static final String FIELD_CATEGORY = "category";
	public static final String INDEX_DIRECTORY = "index";
	public static final boolean recreate = true;
	public static final int hitsPerPage = 10;

	public static void main(String[] args) throws IOException, ParseException {

		boolean indexing_flag = false;
		boolean query_flag = false;
		String queryString = "";
		if (args.length > 0) {
			if (args[0].equals("-index")) {
				indexing_flag = true;
			} else if (args[0].equals("-q")) {
				query_flag = true;
				if (args.length > 1) {
					queryString = args[1];
				} else {
					System.out.println("Incorrect query phrase.");
					System.out.println("Exiting...");
					System.exit(1);
				}
			}

		}
		File indexes = new File(INDEX_DIRECTORY);
		if (indexing_flag) {
			createIndex(indexes);
		}

		if (query_flag) {
			searchIndex(indexes.toPath(), queryString);
		}
		// searchIndex(indexes.toPath(), "CATEGORIES");
		// searchIndex(indexes.toPath(), "formally");
	}

	public static void createIndex(File indexes) throws IOException {
		if (DEBUG) {
			System.out.println("Starting Index");
		}
		Directory directory = new SimpleFSDirectory(indexes.toPath());
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		File dir = new File(FILES_TO_INDEX);
		File[] files = dir.listFiles();
		int debugCounter = 0;
		for (File file : files) {

			Document document = new Document();
			String path = file.getCanonicalPath();
			Reader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);

			String line = br.readLine();
			document.add(new TextField(FIELD_CATEGORY, line, Field.Store.YES));
			
			for (line = br.readLine(); line != null; line = br.readLine()) {
				if (!line.equals("")) {
					debugCounter++;
					if (DEBUG && (debugCounter % 100 == 0)) {
						System.out.println(debugCounter);
					}
					
					if ( !isCategory(line)){
						document.add(new TextField(FIELD_CONTENTS, line, Field.Store.YES));
					} else {
						indexWriter.addDocument(document);
						document = new Document();
						document.add(new TextField(FIELD_CATEGORY, line, Field.Store.YES));
					}
				}
			}

		}
		indexWriter.close();
		if (DEBUG) {
			System.out.println("Indexing finished");
		}
	}
	
	public static boolean isCategory(String line){
		return (line.length() > 2 && line.charAt(0) == '[' && line.charAt(1) == '[') ;
	}

	public static String parseCategory(String s) {
		String returnString = s.substring(2, s.length() - 2);
		return returnString;

	}

	public static void searchIndex(Path directoryPath, String searchString) throws IOException, ParseException {
		Directory directory = FSDirectory.open(directoryPath);
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		System.out.println(query.toString());

		TopDocs docs = indexSearcher.search(query, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		printResults(hits, indexSearcher);
		System.out.println(indexSearcher.doc(0).get(FIELD_CATEGORY));

	}

	public static void printResults(ScoreDoc[] hits, IndexSearcher indexSearcher) throws IOException {
		for (int i = 0; i < hits.length; i++) {
			System.out.println(hits[i].score + ", " + hits[i].doc + ", " + indexSearcher.doc(hits[i].doc).get(FIELD_CATEGORY));
		}
	}
}
