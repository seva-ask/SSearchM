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
			if (Character.isUpperCase(currentChar)) {
				String word = stringBuilder.toString().toLowerCase();
				if (!word.isEmpty()) {
					result.add(word);
				}
				stringBuilder = new StringBuilder();
			}
			stringBuilder.append(currentChar);
			currentPosition++;
		}
		String word = stringBuilder.toString().toLowerCase();
		if (!word.isEmpty()) {
			result.add(word);
		}
		return result;
	}
}
