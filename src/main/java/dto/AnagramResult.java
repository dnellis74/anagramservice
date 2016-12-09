package dto;

import java.util.Collection;

public class AnagramResult {
	private Collection<String> anagrams ;

	public AnagramResult(Collection<String> anagrams) {
		this.anagrams = anagrams;
	}

	public AnagramResult() {
		// TODO Auto-generated constructor stub
	}

	public Collection<String> getAnagrams() {
		return anagrams;
	}

	public void setAnagrams(Collection<String> anagrams) {
		this.anagrams = anagrams;
	}
}