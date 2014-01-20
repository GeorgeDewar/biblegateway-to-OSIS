package nz.co.dewar.biblescraper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class MainNoFootnotes {
	
	static final int NUM_BOOKS_OT = 39;
	static final int NUM_BOOKS_NT = 27;
	
	//static final String source = "D:\\George\\Development\\BibleGateway2OSIS\\{BOOK}-{CHAPTER}-{VERSION}.html";
	static final String source = "http://mobile.biblegateway.com/passage/?search={BOOK}%20{CHAPTER}&version={VERSION}";
	
	static String version = "NKJV";
	
	public static void main(String[] args) throws Exception{
		
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" 
			+ "<osis xmlns=\"http://www.bibletechnologies.net/2003/OSIS/namespace\"\n"
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "xsi:schemaLocation=\"http://www.bibletechnologies.net/2003/OSIS/namespace http://www.bibletechnologies.net/osisCore.2.1.1.xsd\">\n"

			+ "<osisText osisIDWork=\"" + version + "\" osisRefWork=\"bible\" xml:lang=\"en\">\n\n"
			+ "<header>\n"
			+ "\t<work osisWork=\"" + version + "\">\n"
            + "\t\t<title>" + version + "</title>\n"
			+ "\t</work>\n"
			+ "</header>\n\n";
		
		String footer = "\n</osisText>\n</osis>\n";
		
		// Get all the books of the old and new testament
		Scanner bookScanner = new Scanner(new File("books.dat"));
		Element oldTestament = getTestament(bookScanner, NUM_BOOKS_OT);
		Element newTestament = getTestament(bookScanner, NUM_BOOKS_NT);
		bookScanner.close();
		
		// Output passage to String
		String passageXml = (oldTestament.outerHtml() + newTestament.outerHtml())
			.replaceAll("osisid", "osisID");
		
		// Print output to file
		PrintStream output = new PrintStream(new File(version + ".xml"), "UTF-8");
		output.print(header + passageXml + footer);
		output.close();
		
	}
	
	static Element getTestament(Scanner scanner, int numBooks) throws IOException{
		Element testament = new Element(Tag.valueOf("div"), "")
			.attr("type", "x-testament");
	
		for(int i=0; i<numBooks; i++){
			String book = scanner.next();
			int numChapters = scanner.nextInt();
			Element bookEl = getBook(book, numChapters, version);
			testament.appendChild(bookEl);
		}
		
		return testament;
	}
	
	static Element getBook(String book, int numChapters, String version) throws IOException{
		System.out.print("Fetching " + book);
		
		Element bookEl = new Element(Tag.valueOf("div"), "")
			.attr("type", "book")
			.attr("osisID", book);
		
		for(int i=1; i<=numChapters; i++){
			System.out.print(".");
			Element passage = getChapter(book, i, version);
			bookEl.appendChild(passage);
		}
		
		System.out.println();
		
		return bookEl;
	}
	
	static Element getChapter(String book, int chapter, String version) throws IOException{
		String url = source
				.replace("{BOOK}", book)
				.replace("{CHAPTER}", String.valueOf(chapter))
				.replace("{VERSION}", version);
		
		Document doc;
		
		if(source.startsWith("http")){
			doc = Jsoup.connect(url).get();
		}
		else{
			doc = Jsoup.parse(new File(url), "UTF-8");
		}
		
		Element passage = doc.select(".passage.text-html").first();
		
		// Convert div to chapter tag
		passage.tagName("chapter")
			.removeAttr("class")
			.attr("osisID", book + "." + chapter);
		
		// Clean up stuff we don't want
		passage.select(".crossreference, .crossrefs, .footnote, .footnotes, sup.versenum, span.chapternum").remove();
		passage.select("p").removeAttr("class");
		removeComments(passage);
		
		// Remove redundant poetry small-caps span
		passage.select(".poetry span.small-caps").unwrap();
		
		// Iterate through each verse, converting it to the right format
		Elements verses = passage.select("p span.text");
		for(Element verse : verses){
			int verseNum = Integer.parseInt(verse.className().substring(verse.className().indexOf("-" + chapter + "-") + ("-" + chapter + "-").length()));
			
			verse.tagName("verse")
				.removeAttr("id").removeAttr("class")
				.attr("osisID", book + "." + chapter + "." + verseNum);
		}
		
		// Convert poetry div to lg
		passage.select("div.poetry p").tagName("lg");
		passage.select("lg verse").wrap("<l level=\"1\"></l>");
		passage.select("div.poetry").tagName("p").removeAttr("class");
		passage.select("lg br").remove();
		
		// Convert h3s to titles
		passage.select("h3 span").unwrap();
		passage.select("h3").tagName("title");
		
		// Convert I tags to transchange
		passage.select("i").tagName("transchange").attr("type", "added");
		
		// Ignore words of Jesus
		passage.select(".woj").unwrap();
		
		
		return passage;
	}
	
	private static void removeComments(Node node) {
        for (int i = 0; i < node.childNodes().size();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }
	
}
