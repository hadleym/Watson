package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
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
		File destDir = new File(Constants.NLP_INDEX);
		File srcDir = new File(Constants.PREPROCESS_DIR);
		Directory directory = new SimpleFSDirectory(destDir.toPath());
		Analyzer analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		
		int total = srcDir.listFiles().length;
		int count = 1;
		for (File srcFile : srcDir.listFiles()){
			System.out.print(count + " of " + total +", ");
			indexFile(srcFile, destDir, indexWriter);
		}
		indexWriter.close();
	}

	public static void indexFile(File srcFile, File destDir, IndexWriter indexWriter) throws IOException {
		Reader reader = new FileReader(srcFile);
		BufferedReader br = new BufferedReader(reader);

		// assumes that the first line of a text document is a subject
		String line = br.readLine();
		Document document = new Document();
		addTextField(document, Constants.FIELD_CATEGORY, line);
		addTextField(document, Constants.FIELD_CONTENTS, line);
		for (line = br.readLine(); line != null; line = br.readLine()) {
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
				addTextField(document, Constants.FIELD_CONTENTS, line);
			}
		}
		br.close();

		System.out.println( srcFile.getName() + " finished");
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
