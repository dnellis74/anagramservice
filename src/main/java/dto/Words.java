package dto;

import java.util.List;

public class Words {
	private List<String> words;
	
	public Words() {}

	public Words(List<String> words) {
		this.words = words;
	}

	public List<String> getWords() {
		return words;
	}

	public void setWords(List<String> words) {
		this.words = words;
	}
}