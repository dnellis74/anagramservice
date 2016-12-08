package anagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;;


@Controller
public class AnagramController {
	
	class AnagramResult {
		private List<String> anagrams = new ArrayList<String>();

		public List<String> getAnagrams() {
			return anagrams;
		}

		public void setAnagrams(List<String> anagrams) {
			this.anagrams = anagrams;
		}
	}

	Map<String, List<String>> hashedDict = new HashMap<String, List<String>>();

	{
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("dictionary.txt.gz").getFile());
		//File file = new File(classLoader.getResource("dictionary.txt").getFile());
		System.out.println(file.getAbsolutePath());
		try {
			//InputStream inputStream = new FileInputStream(file);
			InputStream inputStream = new GZIPInputStream(new FileInputStream(file));
			try (Scanner scanner = new Scanner(inputStream)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					String hash = stringToHash(line);
					List<String> matches = hashedDict.get(hash);
					if (matches == null) {
						matches = new ArrayList<String>();
						hashedDict.put(hash, matches);
					}
					matches.add(line);
				}
				scanner.close();
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(method = RequestMethod.GET, path = "anagrams/{input}.json")
	public @ResponseBody AnagramResult get(@PathVariable(value = "input") String input) {
		AnagramResult result = new AnagramResult();
		result.setAnagrams(hashedDict.get(stringToHash(input)));
		return result;
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
