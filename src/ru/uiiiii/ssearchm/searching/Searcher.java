package ru.uiiiii.ssearchm.searching;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.ZeroVectorException;
import ru.uiiiii.ssearchm.common.SourceData;

public class Searcher {

	private static final int MAX_RESULTS = 20;

	public static void main(String[] args) throws IOException, ZeroVectorException, NoHeadException, GitAPIException {
		
		LinkedList<SearchResult> results = SemanticVectorsSearcher.performSearch(SourceData.QUERY, MAX_RESULTS);
		
		GitHelper gitHelper = new GitHelper(SourceData.DOCS_PATH);
		
		TreeSet<RevCommit> commits = new TreeSet<RevCommit>();
		
		for (SearchResult result: results) {
			String filePath = result.getObjectVector().getObject().toString();
			Set<RevCommit> fileCommits = gitHelper.getCommitsFromFile(filePath);
			commits.addAll(fileCommits);	
		}
		
		TreeSet<RevCommit> commitsWithoutIssues = new TreeSet<RevCommit>(); 
		
		HashMap<String, TreeSet<RevCommit>> issueCommits = new HashMap<String, TreeSet<RevCommit>>();
		
		for (RevCommit revCommit : commits) {
			String issueNumber = CommitMessageParser.getIssueNumber(revCommit.getFullMessage());
			if (issueNumber != null) {
				issueCommits.put(issueNumber, new TreeSet<RevCommit>());
			}
			else {
				commitsWithoutIssues.add(revCommit);
			}
		}
		
		gitHelper.AddCommitsForIssues(issueCommits);
		
		HashMap<String, Integer> filesFrequency = new HashMap<String, Integer>();
		
		for (RevCommit revCommit : commitsWithoutIssues) {
			Set<String> filesInCommit = gitHelper.getChangedFiles(revCommit);
			addCommitInfo(filesFrequency, filesInCommit);
		}
		
		for (TreeSet<RevCommit> commitSet : issueCommits.values()) {
			TreeSet<String> changedFiles = new TreeSet<String>();
			for (RevCommit revCommit : commitSet) {
				Set<String> filesInCommit = gitHelper.getChangedFiles(revCommit);
				changedFiles.addAll(filesInCommit);
			}
			addCommitInfo(filesFrequency, changedFiles);
		}
		
		int max = 0;
		for (Integer val : filesFrequency.values()) {
			max = Math.max(max, val);
		}
		
		System.out.println(filesFrequency);
		
//		for (SearchResult result: results) {
//			  System.out.println(String.format(
//			      "%f:%s",
//			      result.getScore(),
//			      result.getObjectVector().getObject().toString()));
//		}
	}

	private static void addCommitInfo(HashMap<String, Integer> filesFrequency, Set<String> filesInCommit) {
		for (String fileInCommit : filesInCommit) {
			if (!filesFrequency.containsKey(fileInCommit)) {
				filesFrequency.put(fileInCommit, 0);
			}
			filesFrequency.put(fileInCommit, filesFrequency.get(fileInCommit) + 1);
		}
	}

}
