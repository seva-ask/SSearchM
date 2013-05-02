package ru.uiiiii.ssearchm.indexing;

import java.util.LinkedList;

public class JavaNameParser {
	public static LinkedList<String> parseName(String source)
	{
		LinkedList<String> result = new LinkedList<String>();
		StringBuilder stringBuilder = new StringBuilder();
		int currentPosition = 0;
		int length = source.length();
		while (currentPosition < length) {
			char currentChar = source.charAt(currentPosition);
			if (currentChar == '.' || currentChar == '_') {
				addWord(result, stringBuilder);
				stringBuilder = new StringBuilder();
			}
			else if (Character.isUpperCase(currentChar)) {
				addWord(result, stringBuilder);
				stringBuilder = new StringBuilder();
				stringBuilder.append(currentChar);
			}
			else {
				stringBuilder.append(currentChar);
			}
			currentPosition++;
		}
		addWord(result, stringBuilder);
		return result;
	}

	private static void addWord(LinkedList<String> result, StringBuilder stringBuilder) {
		String word = stringBuilder.toString().toLowerCase();
		if (!word.isEmpty()) {
			result.add(word);
		}
	}
}
