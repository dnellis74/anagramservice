package anagram;

import java.math.BigDecimal;

public class DictionaryInfo {
	private int minLength = Integer.MAX_VALUE;
	private int maxLength = Integer.MIN_VALUE;
	private int median = 0;
	private BigDecimal average;

	public DictionaryInfo() {
		// TODO Auto-generated constructor stub
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMedian() {
		return median;
	}

	public void setMedian(int median) {
		this.median = median;
	}

	public BigDecimal getAverage() {
		return average;
	}

	public void setAverage(BigDecimal average) {
		this.average = average;
	}
}