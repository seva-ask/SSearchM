package ru.uiiiii.ssearchm.searching;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;


public class ResultsSet extends TreeMap<Double, TreeSet<String>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ResultsSet(Comparator<Object> reverseOrder) {
		super(reverseOrder);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for (Double key : keySet()) {
			if (key > 0) {
				builder.append(key).append(" =\n[ ");
				for (String str : get(key)) {
					builder.append(str).append("\n");
				}
				builder.append("],\n");
			}
		}
		builder.append("\n}");
		return builder.toString();
	}

}
