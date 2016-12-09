package anagram;

import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class AnagramController {

	Map<String, Collection<String>> hashedDict = new HashMap<String, Collection<String>>();

	{
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("dictionary.txt.gz").getFile());
		try {
			InputStream inputStream = new GZIPInputStream(new FileInputStream(file));
			try (Scanner scanner = new Scanner(inputStream)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					addWord(line);
				}
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
