package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class IndexDocuments {

	public static void main(String[] args) throws IOException {
		File index = new File(Constants.INDEX_DIRECTORY);
		createIndex(index);
	}

	public static void createIndex(File indexes) throws IOException {
		if (Constants.DEBUG) {
			System.out.println("Starting Index");
		}
		// Directory directory = new FSDirectory(indexes.toPath());
		Directory directory = new SimpleFSDirectory(indexes.toPath());
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		File dir = new File(Constants.FILES_TO_INDEX);
		File[] files = dir.listFiles();
		int debugCounter = 0;
		for (File file : files) {

			Document document = new Document();
			String path = file.getCanonicalPath();
			Reader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);

			String line;
			for (line = br.readLine(); line != null; line = br.readLine()) {
				if (!line.equals("")) {
					debugCounter++;
					if (Constants.DEBUG && (debugCounter % 100 == 0)) {
						System.out.println(debugCounter);
					}

					if (!isCategory(line)) {
						if (Constants.DEBUG) {
							System.out.print("Adding to contents--> ");
							System.out.println(line);
						}
						document.add(new TextField(Constants.FIELD_CONTENTS, line, Field.Store.YES));
					} else {
						indexWriter.addDocument(document);
						document = new Document();
						addTextField(document, Constants.FIELD_CATEGORY, line);
					}
				}
			}

		}
		indexWriter.close();
		if (Constants.DEBUG) {
			System.out.println("Indexing finished");
		}
	}

	public static void addTextField(Document document, String field, String line) {
		if (Constants.DEBUG) {
			System.out.println("New Category: " + line);
		}
		document.add(new TextField(field, line, Field.Store.YES));
	}

	public static boolean isCategory(String line) {
		return (line.length() > 2 && line.charAt(0) == '[' && line.charAt(1) == '[');
	}
}
