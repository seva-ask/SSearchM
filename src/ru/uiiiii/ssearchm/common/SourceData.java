package ru.uiiiii.ssearchm.common;

public class SourceData {
	
	public final static String DOCS_PATH = "C:\\Projects\\SSearchM\\tests\\4\\MPS";
	
	public final static String[] QUERY = {"interpreted", "generator"};
	
	public static String getQueryText() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String str : QUERY) {
			stringBuilder.append(str);
			stringBuilder.append(" ");
		}
		String result = stringBuilder.toString();
		return result.substring(0, result.length() - 1);
	}
	
	public final static String ISSUE_TRACKER_PREFIX = "MPS";
}
