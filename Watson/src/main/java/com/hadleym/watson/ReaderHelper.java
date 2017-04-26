package com.hadleym.watson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ReaderHelper {

	public static void main(String[] args) throws IOException{
		Path indexPath = new File(Constants.INDEX_DIR).toPath();
		Directory directory = FSDirectory.open(indexPath);
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		System.out.println("indexReader.getDocCount(FIELD_CONTENTS");
		System.out.println(indexReader.getDocCount(Constants.FIELD_CONTENTS));
		System.out.println(indexReader);
		Term t = new Term(Constants.FIELD_CONTENTS,"science");
		System.out.println(indexReader.totalTermFreq(t));
		for ( int i = 200; i < 250; i++){
			System.out.println(indexReader.document(i));
		}
	}
}
