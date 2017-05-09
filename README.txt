WATSON PROJECT
Mark Hadley
CS 483 
5/1/2017

MAVEN USAGE:
$ mvn compile
$ mvn exec:java
(and modify the argument in the pom.xml file)

JAR USAGE:
USAGE: java -jar Watson.jar -[pre,index,evaluate]

USAGE: java -jar Watson.jar -pre
	Preprocess all files in the 'rawText' directory.
	This is MANDATORY for the indexing of the Core NLP branch discussed in the attached paper.
	This is a 3 hour process on a 2.2 GHz machine with a slow HDD.

USAGE: java -jar Watson.jar -index
	Index all preprocessed and rawFiles for both the StandardAnalzyer Branch and the NLP Core branch.
	Dependant on the -p flag being run on rawFiles ( a 3 hour process )

USAGE: java -jar Watson.jar -evaluate
	Evaluate both branches with both scoring methods.
	Output to predetermined output files.

 ******************************************************
 * Simple Usage:
 * Step 0) Have all raw files in the base directory of the Watson Java project in a directory named 'rawFiles'.
 * Step 1) $ java -jar Watson.jar -pre
 * Step 2) $ java -jar Watson.jar -index
 * Step 3) $ java -jar Watson.jar -evaluate
 ******************************************************
 *  DETAILED USAGE:
 *  Step 1) "-pre" argument
 *  Preprocess the Raw Text files.  
 *  Include them in a local directory called 'rawText'.
 *  *WARNING THIS PROCESS TAKES AROUND 2+ HOURS*
 *  
 *  Step 2) "-index" argument
 *  Index all three models using the -index flag.  This process takes under 10 minutes.
 *  
 *  Step 3) "-evaluate" argument
 *  Evaluate all three models and output 6 .txt files, two for each model (tf-idf, BM25 weighting).
 *  		
 *******************************************************  
 * This is a full end-to-end system for emulating the 'Watson' Jeopardy 
 * system.  
 * 
 * It utilizes Lucene as well as the NLPCore.
 * 
 * CoreNLP is used to remove parts of speech not wanted, and also performs lemmatization.
 * 
 * Lucene is used to index using either the StandardAnalyzer, EnglishAnalyzer or WhitespaceAnalyzer.
 * 
 * Documents that are preprocessed with CoreNLP are then used with just the WhitespaceAnalyzer.
 * 
 * Documents that are not preproccessed with CoreNLP are analyzed with the lucene StandardAnalyzer.
 * 
 * Questions are created by parsing the 'questions.txt' file contained given with these files.
 *******************************************************  
