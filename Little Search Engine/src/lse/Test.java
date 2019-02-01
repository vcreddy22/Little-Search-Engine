package lse;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Test {

	public static void main(String[] args) throws IOException {
		testGetKeyword();
		testInsertLastOccurrence();
		testTop5Search();
	}
	
	public static void testGetKeyword() {
		LittleSearchEngine lse = new LittleSearchEngine();

		System.out.println("\n=== Test method getKeyWord ===");
		testGetKeyword(lse, "World...");
		testGetKeyword(lse, ".,?:;!abc");
		testGetKeyword(lse, "abc.,?:;!");
		testGetKeyword(lse, "abc-def");
	}

	public static void testGetKeyword(LittleSearchEngine lse, String word) {
		System.out.println(word + " ---> " + lse.getKeyword(word));
	}

	public static void testInsertLastOccurrence() {
		System.out.println("\n=== Test method insertLastOccurrence ===");
		LittleSearchEngine lse = new LittleSearchEngine();

		ArrayList<Occurrence> list = new ArrayList<Occurrence>();
		list.add(new Occurrence("test", 12));
		list.add(new Occurrence("test", 8));
		list.add(new Occurrence("test", 7));
		list.add(new Occurrence("test", 5));
		list.add(new Occurrence("test", 3));
		list.add(new Occurrence("test", 2));
		list.add(new Occurrence("test", 6));
		
		System.out.println("Input: " + list);

		ArrayList<Integer> result = lse.insertLastOccurrence(list);

		System.out.println("Result: " + list);
		System.out.println("Output: " + result);
	}

	public static void testTop5Search() throws IOException {
		System.out.println("\n=== Test method top5Search:");

		LittleSearchEngine lse = new LittleSearchEngine();
		String[] testFiles = setupTestFiles();
		lse.makeIndex(testFiles[0], testFiles[1]);

		System.out.println("\nKeywords Index:");
		for (String key: lse.keywordsIndex.keySet()) {
			System.out.println(key + ": " + lse.keywordsIndex.get(key));
		}

		System.out.println("\nSearch result for 'down or fall'");
		ArrayList<String> result = lse.top5search("down", "fall");
		System.out.println(result);
	}

	public static String[] setupTestFiles() throws IOException {
		File fDocs = File.createTempFile("test_docs", ".txt");
		PrintStream printer = new PrintStream(fDocs);

		File[] fDocList = new File[6];
		for (int i=0; i<6; i++) {
			fDocList[i] = File.createTempFile("test_doc_" + i + "_", ".txt");
			printer.println(fDocList[i].getAbsolutePath());
		}
		printer.close();
		
		String[] docs = new String[] {
			"In another moment down went Alice after it...",
			"Down, down, down.",
			"Would the fall NEVER come to an end!",
			"'I wonder how many miles I've fallen by this time?' she said aloud.",
			"Presently she began again.",
			"'Dinah'll miss me very much to-night, I should think!'"
		};
		
		for (int i=0; i<fDocList.length; i++) {
			printer = new PrintStream(fDocList[i]);
			printer.println(docs[i]);
			printer.close();
		}

		File fNoiseWord = File.createTempFile("test_noisewords", "txt");
		printer = new PrintStream(fNoiseWord);
		for (String str: new String[]{"the", "this", "all", "an", "and", "another"}) {
			printer.println(str);
		}
		printer.close();
		
		return new String[]{fDocs.getAbsolutePath(), fNoiseWord.getAbsolutePath()};
	}
}
