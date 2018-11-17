package edu.smith.cs.csc212.p8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CheckSpelling {
	/**
	 * Read all lines from the UNIX dictionary.
	 * @return a list of words!
	 */
	public static List<String> loadDictionary() {
		long start = System.nanoTime();
		List<String> words;
		try {
			words = Files.readAllLines(new File("src/main/resources/words").toPath());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find dictionary.", e);
		}
		long end = System.nanoTime();
		double time = (end - start) / 1e9;
		System.out.println("Loaded " + words.size() + " entries in " + time +" seconds.");
		return words;
	}
	/**
	 * Read all lines from a project gutenberg book, using wordsplitter to get rid of punctuation
	 * @param filepath - path to the book
	 * @return a list of words 
	 */
	public static List<String> loadBook(String filepath) {
		long start = System.nanoTime();
		List<String> words;
		List<String> splitwords = new ArrayList<>();
		try {
			words = Files.readAllLines(new File(filepath).toPath());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find book.", e);
		}
		for( String w : words ) {
			splitwords.addAll(WordSplitter.splitTextToWords(w));
		}
		long end = System.nanoTime();
		double time = (end - start) / 1e9;
		System.out.println("Loaded " + splitwords.size() + " entries in " + time +" seconds.");
		return splitwords;
	}

	/**
	 * Create a data set of misspelled and correctly spelled words
	 * @param yesWords - The words that need to be mixed
	 * @param numSamples - The total number of words that should be mixed
	 * @param fractionYes - The fraction of words that should stay correctly spelled
	 * @return A list of both misspelled and correctly spelled words
	 */
	public static List<String> createMixedDataset( List<String> yesWords, int numSamples, double fractionYes ) {
		double numFake = numSamples * fractionYes; 
		int i = 0; 
		List<String> mixedData = new ArrayList<>();
		// Until we have the desired number of fake words, keep creating fake words by adding xyzzz to the end of each
		for ( String w : yesWords ) {
			if ( i < numFake ) { 
				mixedData.add(w.concat("xyzzz"));
				i++;
			}
			else {
				mixedData.add(w);
				i++;
			}
		}
		return mixedData;
		
	}
	
	/**
	 * This method looks for all the words in a dictionary.
	 * @param words - the "queries"
	 * @param dictionary - the data structure.
	 */
	public static void timeLookup(List<String> words, Collection<String> dictionary) {
		long startLookup = System.nanoTime();
		List<String> misspelled = new ArrayList<>();
		
		int found = 0;
		for (String w : words) {
			if (dictionary.contains(w)) {
				found++;
			} 
			else {
				misspelled.add(w);
			}
		}
		double misspelledSize = misspelled.size();
		double wordsSize = words.size();
		double misspelledRatio = ( misspelledSize / wordsSize );
		
		long endLookup = System.nanoTime();
		double fractionFound = found / (double) words.size();
		double timeSpentPerItem = (endLookup - startLookup) / ((double) words.size());
		int nsPerItem = (int) timeSpentPerItem;
		System.out.println(dictionary.getClass().getSimpleName()+": Lookup of items found="+fractionFound+" time="+nsPerItem+" ns/item");
		System.out.println("Misspelled words ratio: " + misspelledRatio);
	}
	
	
	public static void main(String[] args) {
		// --- Load the dictionary.
		List<String> listOfWords = loadDictionary();
		
		List<String> listOfBookWords = loadBook("src/main/resources/Frankenstein.txt");
		
		// --- Create a bunch of data structures for testing:
		long startLookup = System.nanoTime();
		TreeSet<String> treeOfWords = new TreeSet<>(listOfWords);
		long endLookup = System.nanoTime();
		System.out.println("Creation time: " + ((endLookup-startLookup)/1e9));
		HashSet<String> hashOfWords = new HashSet<>(listOfWords);
		
		SortedStringListSet bsl = new SortedStringListSet(listOfWords);
		
		CharTrie trie = new CharTrie();
		for (String w : listOfWords) {
			try {
				trie.insert(w);
			} catch ( RuntimeException e ) {
				continue;
			}
			
		}
		
		LLHash hm100k = new LLHash(100000);
		for (String w : listOfWords) {
			hm100k.add(w);
		}
		
		
		// --- Make sure that every word in the dictionary is in the dictionary:
		timeLookup(listOfWords, treeOfWords);
		timeLookup(listOfWords, hashOfWords);
		timeLookup(listOfWords, bsl);
		timeLookup(listOfWords, trie);
		timeLookup(listOfWords, hm100k);
		
		
		// --- Create a dataset of mixed hits and misses:
		for (int j=0; j<2; j++) {
			System.out.println("Warm-up, j="+j);
			for (int i=0; i<=10; i++) {
				double fraction = i / 10.0;
				// --- Create a dataset of mixed hits and misses:
				List<String> hitsAndMisses = createMixedDataset(listOfWords, listOfWords.size(), 0.0);
					
				timeLookup(hitsAndMisses, treeOfWords);
				timeLookup(hitsAndMisses, hashOfWords);
				timeLookup(hitsAndMisses, bsl);
				timeLookup(hitsAndMisses, trie);
				timeLookup(hitsAndMisses, hm100k);
			}
		}
		
		//Create data structures for the words in the gutenberg book
		TreeSet<String> treeOfBookWords = new TreeSet<>(listOfBookWords);
		HashSet<String> hashOfBookWords = new HashSet<>(listOfBookWords);
		
		SortedStringListSet Bookbsl = new SortedStringListSet(listOfBookWords);
		
		CharTrie Booktrie = new CharTrie();
		for (String w : listOfBookWords) {
			try {
				Booktrie.insert(w);
			} catch ( RuntimeException e ) {
				continue;
			}
			
		}
		
		LLHash Bookhm100k = new LLHash(100000);
		for (String w : listOfBookWords) {
			Bookhm100k.add(w);
		}
		
		System.out.println("\nLooking at book data now");
		timeLookup(listOfBookWords, treeOfBookWords);
		timeLookup(listOfBookWords, hashOfBookWords);
		timeLookup(listOfBookWords, Bookbsl);
		timeLookup(listOfBookWords, Booktrie);
		timeLookup(listOfBookWords, Bookhm100k);
		
		System.out.println("\nRatio of misspelled words in book: ");
		timeLookup(listOfBookWords, treeOfWords);
		
		// --- linear list timing:
		// Looking up in a list is so slow, we need to sample:
		System.out.println("\nStart of list: ");
		timeLookup(listOfWords.subList(0, 1000), listOfWords);
		System.out.println("End of list: ");
		timeLookup(listOfWords.subList(listOfWords.size()-100, listOfWords.size()), listOfWords);
		
	
		// --- print statistics about the data structures:
		System.out.println("Count-Nodes: "+trie.countNodes());
		System.out.println("Count-Items: "+hm100k.size());

		System.out.println("Count-Collisions[100k]: "+Bookhm100k.countCollisions());
		System.out.println("Count-Used-Buckets[100k]: "+Bookhm100k.countUsedBuckets());
		System.out.println("Load-Factor[100k]: "+hm100k.countUsedBuckets() / 100000.0);

		
		System.out.println("log_2 of listOfWords.size(): "+listOfWords.size());
		
		System.out.println("Done!");
	}
}
