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

public class GitFileHistoryAnalyzer {
	public static void getBlame(String filePath) throws IOException, GitAPIException {
		String gitDir = "C:\\Projects\\SSearchM\\tests\\hudson\\.git";
		
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File(gitDir))
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		
		Git git = new Git(repository);
		
		BlameCommand blame = git.blame();
		blame.setFilePath(filePath);
		blame.setStartCommit(ObjectId.fromString("be1f8f91a3dcdcdfd2ed07198659e7eb68abf1f7"));
		blame.setFollowFileRenames(true);
		BlameResult result = blame.call();
		
		int linesCount = result.getResultContents().size();
		
		for	(int i = 0; i < linesCount; i++) {
			String name = result.getSourceCommitter(i).getName();
			System.out.println(name);
		}
	}
	
	public static void main(String[] args) throws IOException, GitAPIException {
		String fileBlame = ".gitignore";
		getBlame(fileBlame);
	}
}
