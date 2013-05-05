package ru.uiiiii.ssearchm.searching;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import ru.uiiiii.ssearchm.common.SourceData;

public class GitHelper {
	
	private Git git;
	
	private ObjectId headId;
	
	private String docsPath;
	
	public GitHelper(String docsPath) throws IOException, NoHeadException, GitAPIException {
		this.docsPath = docsPath;
		git = Git.open(new File(docsPath));
		headId = git.log().call().iterator().next().getId();
	}
	
	private BlameResult getBlameResult(String filePath) throws IOException, GitAPIException {
		String docsPath = SourceData.DOCS_PATH;
		
		String filePathInsideRepo = filePath.replace(docsPath, "").substring(1).replace('\\', '/'); // substring(1) = remove '\'
		
		BlameCommand blame = git.blame();
		blame.setFilePath(filePathInsideRepo);
		blame.setStartCommit(headId);
		blame.setFollowFileRenames(true);
		BlameResult result = blame.call();
		
		return result;
	}
	
	public Set<RevCommit> getCommitsFromFile(String filePath) throws IOException, GitAPIException {
		BlameResult blameResult = getBlameResult(filePath);
		int linesCount = blameResult.getResultContents().size();
		TreeSet<RevCommit> commits = new TreeSet<RevCommit>();
		
		for	(int i = 0; i < linesCount; i++) {
			RevCommit commit = blameResult.getSourceCommit(i);
			if (commit != null) {
				commits.add(commit);
			}
		}
		
		return commits;
	}
	
	public HashMap<String, Integer> getAuthorsFromFile(String filePath) throws IOException, GitAPIException {
		HashMap<String, Integer> authors = new HashMap<String, Integer>();
		BlameResult blameResult = getBlameResult(filePath);
		if (blameResult != null) {
			int linesCount = blameResult.getResultContents().size();
			
			for	(int i = 0; i < linesCount; i++) {
				String author = null;
				PersonIdent authorIdent = blameResult.getSourceCommitter(i);
				if (authorIdent != null) {
					author = authorIdent.getName();
				}
				if (author != null) {
					if (!authors.containsKey(author)) {
						authors.put(author, 0);
					}
					authors.put(author, authors.get(author) + 1);	
				}
			}
		}
		return authors;
	}
	
	public Set<String> getChangedFiles(RevCommit commit) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		TreeSet<String> result = new TreeSet<String>();
		
		Repository repository = git.getRepository();
		RevWalk rw = new RevWalk(repository);
		
		if (commit.getParentCount() == 0) {
			return result;
		}
		
		RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
		
		for (DiffEntry diff : diffs) {
			String filePathInsideRepo = diff.getNewPath();
			if (!filePathInsideRepo.equals("/dev/null") && filePathInsideRepo.endsWith(".cs")) {
				String fullFilePath = docsPath + "\\" + filePathInsideRepo.replace('/', '\\');
				result.add(fullFilePath);
			}
		}
		
		return result;
	}
	
	public void AddCommitsForIssues(HashMap<String, TreeSet<RevCommit>> issueCommits) throws NoHeadException, GitAPIException {
		Iterator<RevCommit> iterator = git.log().call().iterator();
		while (iterator.hasNext()) {
			RevCommit commit = iterator.next();
			String issueNumber = CommitMessageParser.getIssueNumber(commit.getFullMessage());
			if (issueNumber != null && issueCommits.containsKey(issueNumber)) {
				TreeSet<RevCommit> currentIssueCommits = issueCommits.get(issueNumber);
				currentIssueCommits.add(commit);
			}
		}
	}
}
