package ru.uiiiii.ssearchm.indexing;

import java.io.IOException;
import java.util.Date;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import pitt.search.semanticvectors.BuildIndex;

public class Indexer {
	
	public final static String DOCS_PATH = "C:\\Projects\\SSearchM\\tests\\hudson";

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
	    String docsPath = DOCS_PATH;
	    String indexPath = "index";
	    
	    buildLuceneIndex(docsPath, indexPath);
	    
	    buildSemanticVectorsIndex(docsPath, indexPath);
	}

	private static void buildLuceneIndex(String docsPath, String indexPath)
			throws IOException, CorruptIndexException,
			LockObtainFailedException {
		System.out.println("Lucene indexing...");
	    
	    Date startLuceneIndexing = new Date();
	    
	    LuceneIndexBuilder.buildIndex(docsPath, indexPath);

	    Date endLuceneIndexing = new Date();
	    System.out.println(endLuceneIndexing.getTime() - startLuceneIndexing.getTime() + " total milliseconds");
	}
	
	private static void buildSemanticVectorsIndex(String docsPath,
			String indexPath) throws IOException, CorruptIndexException,
			LockObtainFailedException {
		System.out.println("Semantic vectors indexing to directory '" + indexPath + "'...");
	    
		Date startSemanticVectorsIndexing = new Date();
		
		String[] args = {"-luceneindexpath", "index"};
		BuildIndex.main(args);
		
	    Date endSemanticVectorsIndexing = new Date();
	    System.out.println(endSemanticVectorsIndexing.getTime() - startSemanticVectorsIndexing.getTime() + " total milliseconds");
	}
}
