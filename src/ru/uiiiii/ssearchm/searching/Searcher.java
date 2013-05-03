package ru.uiiiii.ssearchm.searching;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.ZeroVectorException;
import ru.uiiiii.ssearchm.indexing.Indexer;

public class Searcher {

	private static final int MAX_RESULTS = 20;

	public static void main(String[] args) throws IOException, ZeroVectorException, NoHeadException, GitAPIException {
		String[] query = {"queue"};
		
		LinkedList<SearchResult> results = SemanticVectorsSearcher.performSearch(query, MAX_RESULTS);
		
		GitHelper gitHelper = new GitHelper(Indexer.DOCS_PATH);
		
		TreeSet<RevCommit> commits = new TreeSet<RevCommit>();
		
		for (SearchResult result: results) {
			String filePath = result.getObjectVector().getObject().toString();
			Set<RevCommit> fileCommits = gitHelper.getCommitsFromFile(filePath);
			commits.addAll(fileCommits);	
		}
		
		System.out.println(gitHelper.getChangedFiles(commits.first()));
		
//		for (SearchResult result: results) {
//			  System.out.println(String.format(
//			      "%f:%s",
//			      result.getScore(),
//			      result.getObjectVector().getObject().toString()));
//		}
	}

}
