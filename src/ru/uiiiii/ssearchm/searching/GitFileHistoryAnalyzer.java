package ru.uiiiii.ssearchm.searching;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import ru.uiiiii.ssearchm.indexing.Indexer;

public class GitFileHistoryAnalyzer {
	public static BlameResult getBlameResult(String filePath) throws IOException, GitAPIException {
		String docsPath = Indexer.DOCS_PATH;
		String gitDir = docsPath + "\\.git";
		
		String filePathInsideRepo = filePath.replace(docsPath, "").substring(1); // remove \\
		
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File(gitDir)).readEnvironment().findGitDir().build();
		Git git = new Git(repository);
		
		ObjectId headId = git.log().call().iterator().next().getId();
		
		BlameCommand blame = git.blame();
		blame.setFilePath(filePathInsideRepo);
		blame.setStartCommit(headId);
		blame.setFollowFileRenames(true);
		BlameResult result = blame.call();
		
		return result;
	}
	
	public static void main(String[] args) throws IOException, GitAPIException {
		String fileBlame = "C:\\Projects\\SSearchM\\tests\\hudson\\.gitignore";
		BlameResult result = getBlameResult(fileBlame);
		
		int linesCount = result.getResultContents().size();
		
		for	(int i = 0; i < linesCount; i++) {
			String name = result.getSourceCommitter(i).getName();
			System.out.println(name);
		}
	}
}
