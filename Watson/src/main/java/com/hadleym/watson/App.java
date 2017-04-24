package com.hadleym.watson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class App {

	public static void main(String[] args) throws IOException, ParseException {
		boolean query_flag = false;
		// String queryString = Constants.FIELD_CONTENTS + ":newspaper AND " +
		// Constants.FIELD_CONTENTS + ":washington";
		String queryString = Constants.FIELD_CONTENTS + ":newspaper AND " + 
				Constants.FIELD_CONTENTS + ":washington AND " + 
				Constants.FIELD_CONTENTS + ":capital ";
		if (args.length > 0) {
			if (args[0].equals("-index")) {
			} else if (args[0].equals("-q")) {
				query_flag = true;
				if (args.length > 1) {
//					queryString = args[1];
				} else {
					System.out.println("Incorrect query phrase.");
					System.out.println("Exiting...");
					System.exit(1);
				}
			}

		}
		File indexes = new File(Constants.INDEX_DIRECTORY);

		if (query_flag) {
			searchIndex(indexes.toPath(), queryString);
		}
		// searchIndex(indexes.toPath(), "CATEGORIES");
		// searchIndex(indexes.toPath(), "formally");
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
		QueryParser queryParser = new QueryParser(Constants.FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		if (Constants.DEBUG) {
			System.out.println("SearchString: " + searchString);
			System.out.println("Query: " + query);
		}

		TopDocs docs = indexSearcher.search(query, Constants.HITSPERPAGE);
		ScoreDoc[] hits = docs.scoreDocs;
		printResults(hits, indexSearcher);
		System.out.println(indexSearcher.doc(0).get(Constants.FIELD_CATEGORY));

	}

	public static void printResults(ScoreDoc[] hits, IndexSearcher indexSearcher) throws IOException {
		if (Constants.DEBUG) {
			System.out.println("PrintResults:");
		}
		for (int i = 0; i < hits.length; i++) {

			System.out.println(hits[i].score + ", " + hits[i].doc + ", "
					+ indexSearcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY));
		}
	}
}
