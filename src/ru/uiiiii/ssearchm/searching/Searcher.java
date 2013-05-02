package ru.uiiiii.ssearchm.searching;

import java.io.IOException;
import java.util.LinkedList;

import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.ZeroVectorException;

public class Searcher {

	private static final int MAX_RESULTS = 20;

	public static void main(String[] args) throws IOException, ZeroVectorException {
		String[] query = {"Abraham"};
		
		LinkedList<SearchResult> results = SemanticVectorsSearcher.performSearch(query, MAX_RESULTS);
		
		for (SearchResult result: results) {
			  System.out.println(String.format(
			      "%f:%s",
			      result.getScore(),
			      result.getObjectVector().getObject().toString()));
			}
	}

}
