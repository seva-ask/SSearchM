package ru.uiiiii.ssearchm.searching;

import java.io.IOException;
import java.util.LinkedList;

import pitt.search.semanticvectors.CloseableVectorStore;
import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.LuceneUtils;
import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.VectorSearcher;
import pitt.search.semanticvectors.VectorStoreReader;
import pitt.search.semanticvectors.ZeroVectorException;

public class SemanticVectorsSearcher {

	public static LinkedList<SearchResult> performSearch(String[] query, int maxResults) throws IllegalArgumentException, IOException, ZeroVectorException {
		for (int i = 0; i < query.length; ++i) {
			query[i] = query[i].toLowerCase();
		}
		
		String[] searchArgs = {"-luceneindexpath", "index", "-queryvectorfile", "termvectors.bin", "-searchvectorfile", "docvectors.bin"};
		FlagConfig config = FlagConfig.getFlagConfig(searchArgs);
		CloseableVectorStore queryVecReader = VectorStoreReader.openVectorStore(config.termvectorsfile(), config); 
		CloseableVectorStore resultsVecReader = VectorStoreReader.openVectorStore(config.docvectorsfile(), config);
		LuceneUtils luceneUtils = new LuceneUtils(config); 
		VectorSearcher vecSearcher = new VectorSearcher.VectorSearcherCosine(queryVecReader, resultsVecReader, luceneUtils, config, query); 
		LinkedList<SearchResult> results = vecSearcher.getNearestNeighbors(maxResults);
		
		return results;			
	}
}
