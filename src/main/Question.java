package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Question {
	public final String prompt;
	public final List<String> acceptableAnswers;

	public Question(String prompt, List<String> acceptableAnswers) {
		this.prompt = prompt;
		this.acceptableAnswers = new ArrayList<>();
		for (String ans : acceptableAnswers) {
			if (ans != null) {
				this.acceptableAnswers.add(normalize(ans));
			}
		}
	}

	public static Question fromLine(String line) {
		// Expected format: "<num>) <question> - <answer>[ or <answer2>]"
		String trimmed = line == null ? "" : line.trim();
		int idxParen = trimmed.indexOf(") ");
		if (idxParen >= 0) {
			trimmed = trimmed.substring(idxParen + 2);
		}
		int sep = trimmed.indexOf(" - ");
		if (sep < 0) {
			return new Question(trimmed, Arrays.asList(""));
		}
		String q = trimmed.substring(0, sep).trim();
		String a = trimmed.substring(sep + 3).trim();
		// multiple answers separated by " or "
		String[] parts = a.split("(?i)\\s+or\\s+");
		return new Question(q, Arrays.asList(parts));
	}

	public boolean matches(String userInput) {
		String norm = normalize(userInput);
		for (String ans : acceptableAnswers) {
			if (norm.equals(ans)) return true;
		}
		return false;
	}

	private static String normalize(String s) {
		return s == null ? "" : s.trim().toLowerCase();
	}
}


