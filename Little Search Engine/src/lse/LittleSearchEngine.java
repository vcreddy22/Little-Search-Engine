package lse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine 
{
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() 
	{
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException 
	{
		// create a hash table to store result
		HashMap<String,Occurrence> kwMap = new HashMap<String,Occurrence>();

		// read document content from file
		String docContent;

		try 
		{
			docContent = new String(Files.readAllBytes(Paths.get(docFile)));
		} catch (IOException e) 
		{
			e.printStackTrace();
			return kwMap;
		}
		
		String[] originalWords = docContent.split("\\s");

		for (String word: originalWords) 
		{
			String kw = getKeyword(word);

			if (kw == null) continue;

			// add new occurrence to hash table or update frequency if exists
			Occurrence occr = kwMap.get(kw);
			if (occr != null) 
			{
				occr.frequency += 1;
			} 
			else 
			{
				kwMap.put(kw, new Occurrence(docFile, 1));
			}
		}

		return kwMap;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) 
	{
		for (String kw: kws.keySet())
		{
			ArrayList<Occurrence> occurrences = keywordsIndex.get(kw);
			if (occurrences == null) 
			{
				occurrences = new ArrayList<Occurrence>();
				occurrences.add(kws.get(kw));
				keywordsIndex.put(kw, occurrences);
			} 
			else 
			{
				occurrences.add(kws.get(kw));
				insertLastOccurrence(occurrences);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) 
	{
		word = word.toLowerCase()
			.replaceAll("^[.,?:;!]+", "")
			.replaceAll("[.,?:;!]+$", "");

		if (word.matches("^[a-zA-Z]+$") && !noiseWords.contains(word)) 
		{
			return word;
		}

		return null;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) 
	{
		ArrayList<Integer> mpIndexes = new ArrayList<Integer>();

		if (occs.size() == 1) return mpIndexes;
		
		// calculate mid point and add to mid point indexes
		int mp = (occs.size() - 2) / 2;
		mpIndexes.add(mp);

		Occurrence last = occs.get(occs.size()-1);

		if (occs.get(mp).frequency > last.frequency) 
		{
			// make a copy of right part to continue processing
			ArrayList<Occurrence> right_occs = new ArrayList<Occurrence>(occs.subList(mp+1, occs.size()));
			ArrayList<Integer> subIndexes = insertLastOccurrence(right_occs);

			// update result
			for (int i=0; i<right_occs.size(); i++) 
			{
				occs.set(i+mp+1, right_occs.get(i));
			}

			// append mid point indexes
			for (int i: subIndexes) mpIndexes.add(i+mp+1);
		} 
		else 
		{
			// move the last occurrence to the end of the left part
			occs.remove(occs.size()-1);
			occs.add(mp, last);

			// then make a copy of the left part to continue processing
			ArrayList<Occurrence> left_occs = new ArrayList<Occurrence>(occs.subList(0, mp+1));
			ArrayList<Integer> subIndexes = insertLastOccurrence(left_occs);

			// update result
			for (int i=0; i<left_occs.size(); i++) 
			{
				occs.set(i, left_occs.get(i));
			}

			// append mid point indexes
			for (int i: subIndexes) mpIndexes.add(i);
		}

		return mpIndexes;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException 
	{
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) 
		{
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) 
		{
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, returns null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) 
	{
		// create a hash table to store documents
		HashMap<String,Occurrence> docMap = new HashMap<String, Occurrence>();

		// add documents containing kw1 to hash table
		if (keywordsIndex.containsKey(kw1)) 
		{
			for (Occurrence ocr: keywordsIndex.get(kw1)) 
			{
				docMap.put(ocr.document, new Occurrence(ocr.document, ocr.frequency));
			}
		}

		// add documents containing kw2 to hash table, also update frequency
		if (keywordsIndex.containsKey(kw2)) 
		{
			for (Occurrence ocr: keywordsIndex.get(kw2)) 
			{
				Occurrence docOcr = docMap.get(ocr.document);
				if (docOcr != null) 
				{
					docOcr.frequency += ocr.frequency;
				} else 
				{
					docMap.put(ocr.document, new Occurrence(ocr.document, ocr.frequency));
				}
			}
		}

		// if no document found
		if (docMap.size() == 0) return new ArrayList<String>();

		// add all documents to a list and sort descending using method insertLastOccurrence
		ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
		for (Occurrence ocr: docMap.values()) 
		{
			occurrences.add(ocr);
			insertLastOccurrence(occurrences);
		}
		
		// extract documents as result
		ArrayList<String> result = new ArrayList<String>();
		for (int i=0; i<5 && i<occurrences.size(); i++) 
		{
			result.add(occurrences.get(i).document);
		}

		return result;
	}
}
