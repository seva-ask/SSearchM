package ru.uiiiii.ssearchm.searching;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.uiiiii.ssearchm.common.SourceData;

public class CommitMessageParser {

	private static String REGEXP_ISSUE_TRACKER = ".*" + SourceData.ISSUE_TRACKER_PREFIX + "-([0-9]+).*";
	
	private static Pattern pattern = Pattern.compile(REGEXP_ISSUE_TRACKER);
	
	public static String getIssueNumber(String commitMessage) {
		Matcher m = pattern.matcher(commitMessage);
		if (m.find()) {
		   return m.group(1);
		}
		else {
			return null;
		}
	}
}
