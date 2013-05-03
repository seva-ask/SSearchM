package ru.uiiiii.ssearchm.searching;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;

import pitt.search.semanticvectors.SearchResult;
import pitt.search.semanticvectors.ZeroVectorException;
import ru.uiiiii.ssearchm.common.SourceData;

public class Searcher {

	private static final int MAX_RESULTS = 20;

	public static void main(String[] args) throws IOException, ZeroVectorException, NoHeadException, GitAPIException {
		
		System.out.println("Search started: query = \"" + SourceData.getQueryText() + "\"");
	    Date startSearch = new Date();
		
		System.out.println("Semantic Vectors searching...");
		LinkedList<SearchResult> results = SemanticVectorsSearcher.performSearch(SourceData.QUERY, MAX_RESULTS);
		System.out.println("Semantic Vectors search ended...");
		
		GitHelper gitHelper = new GitHelper(SourceData.DOCS_PATH);
		
		HashMap<String, Integer> filesFrequency = getFileFrequency(results, gitHelper);
		HashMap<String, Double> sourceSearchResultsRatings = getRankedSourceSearchResults(results);
		TreeMap<Double, TreeSet<String>> normalizedResult = getNormalizedResults(filesFrequency, sourceSearchResultsRatings);
		
		TreeMap<Double, TreeSet<String>> normalizedAuthors = getNormalizedAuthors(gitHelper, normalizedResult);
		
		System.out.println("Search ended");
	    Date endSearch = new Date();
	    System.out.println(endSearch.getTime() - startSearch.getTime() + " total milliseconds");

		System.out.println("Result:");
		
		System.out.println("Scope:");
		System.out.println(normalizedResult);
		
		System.out.println("Authors:");
		System.out.println(normalizedAuthors);
	}

	private static TreeMap<Double, TreeSet<String>> getNormalizedAuthors(
			GitHelper gitHelper,
			TreeMap<Double, TreeSet<String>> normalizedResult)
			throws IOException, GitAPIException {
		HashMap<String, Double> authors = getAuthors(gitHelper, normalizedResult);
		
		TreeMap<Double, TreeSet<String>> targetAuthors = new TreeMap<Double, TreeSet<String>>(Collections.reverseOrder());
		
		for (String author : authors.keySet()) {
			double rating = authors.get(author);
			if (!targetAuthors.containsKey(rating)) {
				targetAuthors.put(rating, new TreeSet<String>());
			}
			targetAuthors.get(rating).add(author);
		}
		
		TreeMap<Double, TreeSet<String>> normalizedAuthors = normalize(targetAuthors);
		return normalizedAuthors;
	}

	private static HashMap<String, Double> getAuthors(GitHelper gitHelper, TreeMap<Double, TreeSet<String>> normalizedResult) throws IOException, GitAPIException {
		System.out.println("Authors searching...");
		HashMap<String, Double> authors = new HashMap<String, Double>();
		int filesSearchedForAuthors = 0;
		for (Double rating : normalizedResult.keySet()) {
			TreeSet<String> files = normalizedResult.get(rating);
			for (String file : files) {
				HashMap<String, Integer> fileAuthors = gitHelper.getAuthorsFromFile(file);
				for (String author : fileAuthors.keySet()) {
					if (!authors.containsKey(author)) {
						authors.put(author, 0.0);					
					}
					authors.put(author, authors.get(author) + fileAuthors.get(author) * rating);
				}
				filesSearchedForAuthors++;
				if (filesSearchedForAuthors > MAX_RESULTS) {
					System.out.println("Authors searched...");
					return authors;
				}
			}
		}
		System.out.println("Authors searched...");
		return authors;
	}

	private static TreeMap<Double, TreeSet<String>> getNormalizedResults(
			HashMap<String, Integer> filesFrequency,
			HashMap<String, Double> sourceSearchResultsRatings) {
		TreeMap<Double, TreeSet<String>> targetResult = getTargetResults(filesFrequency, sourceSearchResultsRatings);
		TreeMap<Double, TreeSet<String>> normalizedResult = normalize(targetResult);
		return normalizedResult;
	}

	private static TreeMap<Double, TreeSet<String>> normalize(TreeMap<Double, TreeSet<String>> targetResult) {
		double max = targetResult.firstKey();
		double min = targetResult.lastKey();
		
		TreeMap<Double, TreeSet<String>> normalizedResult = new TreeMap<Double, TreeSet<String>>(Collections.reverseOrder());
		for (Double rating : targetResult.keySet()) {
			double normalizedRating = (rating - min) / (max - min);
			normalizedResult.put(normalizedRating, targetResult.get(rating));
		}
		return normalizedResult;
	}

	private static TreeMap<Double, TreeSet<String>> getTargetResults(
			HashMap<String, Integer> filesFrequency,
			HashMap<String, Double> sourceSearchResultsRatings) {
		TreeMap<Double, TreeSet<String>> targetResult = new TreeMap<Double, TreeSet<String>>(Collections.reverseOrder());
		
		for (String file : filesFrequency.keySet()) {
			File testFile = new File(file);
			if (testFile.exists()) {
				double sourceRating = 0;
				if (sourceSearchResultsRatings.containsKey(file)) {
					sourceRating = sourceSearchResultsRatings.get(file);
				}
				int fileFrequency = filesFrequency.get(file);
				double targetRating = sourceRating + fileFrequency * 0.01;
				
				if (!targetResult.containsKey(targetRating)) {
					targetResult.put(targetRating, new TreeSet<String>());
				}
				targetResult.get(targetRating).add(file);
			}
		}
		return targetResult;
	}

	private static HashMap<String, Double> getRankedSourceSearchResults(LinkedList<SearchResult> results) {
		HashMap<String, Double> sourceSearchResultsRatings = new HashMap<String, Double>();
		
		for (SearchResult result: results) {
			sourceSearchResultsRatings.put(result.getObjectVector().getObject().toString(), result.getScore());
		}
		
		return sourceSearchResultsRatings;
	}

	private static HashMap<String, Integer> getFileFrequency(
			LinkedList<SearchResult> results, GitHelper gitHelper)
			throws IOException, GitAPIException, NoHeadException,
			MissingObjectException, IncorrectObjectTypeException {
		
		System.out.println("Extracting commit info about found files...");
		TreeSet<RevCommit> commits = getCommitsFromSearchResults(results, gitHelper);
		System.out.println("Extracted commit info about found files...");
		
		TreeSet<RevCommit> commitsWithoutIssues = new TreeSet<RevCommit>(); 		
		HashMap<String, TreeSet<RevCommit>> issueCommits = new HashMap<String, TreeSet<RevCommit>>();
		
		System.out.println("Parsing found commits for issue numbers...");
		for (RevCommit revCommit : commits) {
			String issueNumber = CommitMessageParser.getIssueNumber(revCommit.getFullMessage());
			if (issueNumber != null) {
				issueCommits.put(issueNumber, new TreeSet<RevCommit>());
			}
			else {
				commitsWithoutIssues.add(revCommit);
			}
		}
		System.out.println("Parsed found commits for issue numbers...");
		
		System.out.println("Parsing all commits for found issue numbers...");
		gitHelper.AddCommitsForIssues(issueCommits);
		System.out.println("Parsed all commits for found issue numbers...");
		
		return calculateFileFrequency(gitHelper, commitsWithoutIssues, issueCommits);
	}

	private static HashMap<String, Integer> calculateFileFrequency(
			GitHelper gitHelper, TreeSet<RevCommit> commitsWithoutIssues,
			HashMap<String, TreeSet<RevCommit>> issueCommits)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		
		System.out.println("Calculating file frequency...");
		
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
		
		System.out.println("Calculated file frequency...");
		
		return filesFrequency;
	}

	private static TreeSet<RevCommit> getCommitsFromSearchResults(LinkedList<SearchResult> results, GitHelper gitHelper) throws IOException, GitAPIException {
		TreeSet<RevCommit> commits = new TreeSet<RevCommit>();
		
		for (SearchResult result: results) {
			String filePath = result.getObjectVector().getObject().toString();
			Set<RevCommit> fileCommits = gitHelper.getCommitsFromFile(filePath);
			commits.addAll(fileCommits);	
		}
		return commits;
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
