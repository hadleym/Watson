package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

/*
 * This class provides the functionality of indexing 
 * a given directory of files into another directory
 * using a given analyzer.
 */
public class DocumentIndexer {

	File srcDir, destDir;
	Analyzer analyzer;
	IndexWriter indexWriter;
	BufferedReader br;

	public DocumentIndexer(File srcDir, File destDir, Analyzer analyzer) {
		this.srcDir = srcDir;
		this.destDir = destDir;
		this.analyzer = analyzer;
	}

	// index the files in the given directory with a
	// simpleFSDirectory
	public void indexAllFiles() {
		try {
			Directory directory = new SimpleFSDirectory(destDir.toPath());
			this.indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
			int total = srcDir.listFiles().length;
			int count = 1;
			for (File srcFile : srcDir.listFiles()) {
				System.out.print(count++ + " of " + total + ", ");
				indexFile(srcFile, destDir, indexWriter);
			}
			indexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	// index a single file into the destination directory and the given
	// indexWriter.
	public void indexFile(File srcFile, File destDir, IndexWriter indexWriter) throws IOException {
		Reader reader = new FileReader(srcFile);
		BufferedReader br = new BufferedReader(reader);

		// assumes that the first line of a text document is a subject
		String line = br.readLine();
		Document document = new Document();
		addTextField(document, Constants.FIELD_CATEGORY, line);
		addTextField(document, Constants.FIELD_CONTENTS, line);
		// read through each line until EOF is reached.
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

		System.out.println(srcFile.getName() + " finished");
	}

	// add a single line to the given field of a lucene document.
	public void addTextField(Document document, String field, String line) {
		if (Constants.DEBUG) {
			System.out.println("New Category: " + line);
		}
		document.add(new TextField(field, line, Field.Store.YES));
	}

	// categories are determined by having '[[' as the first
	// two characters.
	public static boolean isCategory(String line) {
		return (line.length() > 2 && line.charAt(0) == '[' && line.charAt(1) == '[');
	}
}
