package nz.co.dewar.biblescraper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DictionaryBuilder {

	static String fileName = "NIV.xml";
	static String outputFileName = "verses.txt";
	
	public static void main(String[] args) throws IOException{

		Document doc = Jsoup.parse(new File(fileName), "UTF-8");
		Elements verses = doc.select("verse");
		
		// Remove footnotes and cross-references
		verses.select("note").remove();
		
		PrintStream output = new PrintStream(new File(outputFileName), "UTF-8");
				
		for(Element verse : verses){
			// Get the raw text of the verse
			String verseText = verse.text();
			
			// Turn dashes into spaces (the code below will remove them and inappropriately conjoin words)
			verseText = verseText.replaceAll("—", " ").trim();
			
			// Clean it up (remove all punctuation, excess spacing, etc)
			verseText = verseText.replaceAll("[^a-zA-Z0-9 ]", "").trim();
			while(verseText.contains("  ")){
				verseText = verseText.replaceAll("  ", " ");
			}
			
			// Make it be upper case for case-insensitive matching
			verseText = verseText.toUpperCase();
			
			// Write it out
			output.println(verseText);
		}
		
		output.close();
	}
	
	
	
}
