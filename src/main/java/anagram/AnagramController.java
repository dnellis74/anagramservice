package anagram;

import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import dto.AnagramResult;
import dto.Words;

@Controller
//TODO Refactor this class to just be a webservice controller and move anagram logic to a delegate class
public class AnagramController {

	//TODO Refactor the dictionary and the stats into one class.  Hide the details of the anagram implementation
	//TODO persist the state of the dictionary back to the file
	//TODO reload from the file
	DictionaryInfo dictionaryInfo = new DictionaryInfo();
	Map<String, Collection<String>> hashedDict = new HashMap<String, Collection<String>>();

	{
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("dictionary.txt.gz").getFile());
		try {
			InputStream inputStream = new GZIPInputStream(new FileInputStream(file));
			try (Scanner scanner = new Scanner(inputStream)) {
				int totalChars = 0;
				int totalLines = 0;
				Map<Integer, Integer> medianMap = new HashMap<Integer, Integer>();
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					addWord(line);
					
					// Stats
					// TODO Make these stats update as the dictionary is modified.
					// Average
					totalLines++;
					totalChars += line.length();

					// Minimum
					if (line.length() < dictionaryInfo.getMinLength()) {
						dictionaryInfo.setMinLength(line.length());
					}
					
					// Max
					if (line.length() > dictionaryInfo.getMaxLength()) {
						dictionaryInfo.setMaxLength(line.length());
					}
					
					// Median
					// This "unique implementation for a median avoided a sort.   It'd be interesting to see if this is faster.
					// My guess is the adds to the map are way slower
					Integer count = medianMap.get(line.length());
					if (count == null) {
						medianMap.put(line.length(), 1);
					} else {
						medianMap.put(line.length(), count + 1);
					}
				}
				// Calculate aggregate stats
				// Final median
				int i = 1;
				int progress = 0;
				while (true) {
					Integer count = medianMap.get(i);
					System.out.println(i + " " + count + " " + progress + " " + totalLines / 2);
					if (count != null) {
						progress += count;
						if (progress > totalLines / 2 ) {
							dictionaryInfo.setMedian(i);
							break;
						}
					}				
					i++;
				}
				// Final average
				// This could likely be a float and not a BigDecimal
				// Initially I was worried about integer overflow.
				dictionaryInfo.setAverage(new BigDecimal(totalChars).divide(new BigDecimal(totalLines), 1, RoundingMode.HALF_UP));
				// TODO prune words with no anagrams
				scanner.close();
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Loaded hashes: " + hashedDict.size());
	}

	// @ResponseStatus(value = HttpStatus.OK)
	// public class NoAnagramsFound extends RuntimeException {
	// private static final long serialVersionUID = -4388096553842069272L;
	// }

	@RequestMapping(method = RequestMethod.GET, path = "anagrams/{input}.json")
	public @ResponseBody AnagramResult get(@PathVariable(value = "input") String input,
			@RequestParam(required = false) Integer limit) {
		AnagramResult result = new AnagramResult();
		result.setAnagrams(hashedDict.get(stringToHash(input)));
		if (limit != null) {
			if (result.getAnagrams() != null) {
				result.setAnagrams(result.getAnagrams().stream().limit(limit).collect(toCollection(HashSet::new)));
			}
		}
		if (result.getAnagrams() == null) {
			result.setAnagrams(new HashSet<String>());
		}
		return result;
	}

	@RequestMapping(method = RequestMethod.GET, path = "words.json")
	@ResponseStatus(code = HttpStatus.OK)
	public @ResponseBody DictionaryInfo getWordInfo() {
		return dictionaryInfo;
	}

	@RequestMapping(method = RequestMethod.POST, path = "words.json")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void post(@RequestBody Words words) {
		for (String word : words.getWords()) {
			addWord(word);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "words.json")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteAll() {
		hashedDict.clear();
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "words/{input}.json")
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteWord(@PathVariable(value = "input") String input) {
		String hash = stringToHash(input);
		Collection<String> anagrams = hashedDict.get(hash);
		if (anagrams != null) {
			for (Iterator<String> iter = anagrams.iterator(); iter.hasNext();) {
				String a = iter.next();
				if (a.equals(input)) {
					iter.remove();
				}
			}
			if (anagrams.isEmpty()) {
				hashedDict.remove(hash);
			}
		}
	}

	// Extra: delete a grouping
	@RequestMapping(method = RequestMethod.DELETE, path = "group/{input}.json")
	@ResponseStatus(code = HttpStatus.OK)
	public void deleteGroup(@PathVariable(value = "input") String input) {
		String hash = stringToHash(input);
		hashedDict.remove(hash);
	}

	private void addWord(String word) {
		String hash = stringToHash(word);
		Collection<String> matches = hashedDict.get(hash);
		if (matches == null) {
			matches = new HashSet<String>();
			hashedDict.put(hash, matches);
		}
		matches.add(word);
	}

	private String stringToHash(String input) {
		char[] array = new char[input.length()];
		for (int index = 0; index < input.length(); index++) {
			array[index] = input.charAt(index);
		}
		Arrays.sort(array);
		return Arrays.toString(array);
	}
}
