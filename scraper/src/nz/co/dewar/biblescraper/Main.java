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

public class Main {
	
	static final int NUM_BOOKS_OT = 39;
	static final int NUM_BOOKS_NT = 27;
	
	static String version = "ESV";
	
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
		Element newTestament = getTestament(bookScanner, 1);
		bookScanner.close();
		
		// Output passage to String
		String passageXml = (oldTestament.outerHtml() + newTestament.outerHtml())
			.replaceAll("osisid", "osisID")
			.replaceAll("osisref", "osisRef")
			.replaceAll("divinename", "divineName")
			.replaceAll("transchange", "transChange");
		
		// Remove whitespace from before a </note> tag to fix mystery blank cross-reference in AndBible
		passageXml = passageXml.replaceAll("</reference>[ \t\r\n]*</note>", "</reference></note>");
		
		// Print output to file
		PrintStream output = new PrintStream(new File("osis_output/" + version + ".xml"), "UTF-8");
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
	
	static Element getChapter(String book, int chapter, String version) throws IOException {
		var url = "raw_html/" + version + "/" + book + "_" + chapter + ".html";
		var doc = Jsoup.parse(new File(url), "UTF-8");

		Element passage = doc.select(".passage-content .text-html").first();
		
		// Convert div to chapter tag
		passage.tagName("chapter")
			.removeAttr("class")
			.attr("osisID", book + "." + chapter);
		
		// Clean up stuff we don't want
		passage.select("sup.versenum, span.chapternum").remove();
		passage.select("p").removeAttr("class");
		removeComments(passage);
		
		// Convert small-caps span to divineName tag
		passage.select("p span.text span.small-caps").tagName("divineName").removeAttr("class").removeAttr("style");
		
		// Remove redundant chapter spans (used only in isolated places)
		passage.select("span.chapter-1, span.chapter-2, span.chapter-3").unwrap();
		
		// Iterate through each verse, converting it to the right format
		Elements verses = passage.select("p span.text");
		for(Element verse : verses){
			int verseNum = Integer.parseInt(verse.className().substring(verse.className().indexOf("-" + chapter + "-") + ("-" + chapter + "-").length()));
			
			String osisId = book + "." + chapter + "." + verseNum;
			
			verse.tagName("verse")
				.removeAttr("id").removeAttr("class")
				.attr("osisID", osisId);
			
			// Handle footnotes
			for(Element footnote : verse.select(".footnote")){
				// Determine ID of content <li> in page
				String id = footnote.select("a").first().attr("href").substring(1);
				// Determine letter of footnote
				String letter = footnote.select("a").first().text();
				// Transform <sup> tag into <note> tag with required attributes
				var noteTag = createTag("note");
				footnote.replaceWith(noteTag);
				noteTag
					.attr("n", letter)
					.attr("osisRef", osisId)
					.attr("osisID", osisId + "!" + letter);
				// Grab the content of the footnote from the footnotes section
				Elements noteContent = passage.select("#" + id);
				// The first link in a footnote is not required
				noteContent.select("a").first().remove();
				// The span is unnecessary
				noteContent.select("span").unwrap();
				// Subsequent links are to verses, and the text of these must be kept
				noteContent.select("a").unwrap();
				// Change italic text to correct tag
				noteContent.select("i").tagName("hi").attr("type", "italic");
				noteTag.html(noteContent.html());
			}
			
			// Handle cross-references
			for(Element crossref : verse.select(".crossreference")){
				var crossReferenceContent = crossref.child(0);

				String linkValue = crossReferenceContent.attr("href");
				// Determine ID of content <li> in page
				String id = linkValue.substring(linkValue.indexOf("#") + 1);
				// Determine letter of footnote
				String letter = crossReferenceContent.text();
				// Transform <sup> tag into <note> tag with required attributes
				var noteTag = createTag("note");
				noteTag
					.attr("type", "crossReference")
					.attr("n", letter)
					.attr("osisID", osisId + "!crossReference." + letter);
				crossref.replaceWith(noteTag);
				// Grab the cross-reference IDs from the footnote link
				String[] refs = passage.select("#" + id).select("a").get(1).attr("data-bibleref").split(",");
				for(int i=0; i<refs.length; i++){
					String ref = refs[i];
					// Turn it into a <reference> tag
					Element refEl = createTag("reference").attr("osisRef", ref);
					// Change the osisID notation into readable notation, with spaces and :s
					refEl.html(ref.replaceFirst("\\.", " ").replaceFirst("\\.", ":").replaceFirst("\\.", " ").replaceFirst("\\.", ":"));
					// Append it to the <note> element
					noteTag.appendChild(refEl);
					// Append the semicolon which the OSIS spec dictates
					if(i < refs.length - 1)
						noteTag.append("; ");
				}
			}


		}
		
		// Convert poetry div to lg
		passage.select("div.poetry p").tagName("lg");
		passage.select("lg verse").wrap("<l level=\"1\"></l>");
		passage.select("span.indent-1-breaks").remove();
		passage.select("span.indent-1").unwrap();
		passage.select("div.poetry").tagName("p").removeAttr("class");
		passage.select("lg br").remove();
		
		// Convert h3s to titles
		passage.select("h3 span").unwrap();
		passage.select("h3").tagName("title");

		// Convert h4s to titles (this is used in some Psalms as a kind of secondary title) and remove references
		passage.select("h4 span").unwrap();
		passage.select("h4").tagName("title").removeAttr("class").select("sup").remove();
		
		// Convert I tags to transchange
		passage.select("i").tagName("transChange").attr("type", "added");
		
		// Ignore words of Jesus
		passage.select(".woj").unwrap();
		
		// Remove footnotes / cross-references sections (which have served their purpose)
		passage.select(".footnotes, .crossrefs").remove();

		// Remove psalm-books
		passage.select(".psalm-book").remove();

		// Convert selahs
//		passage.select("span.selah").removeAttr("class").tagName("l").attr("type", "selah");

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
	
	private static Element createTag(String tagName){
		return new Element(Tag.valueOf(tagName), "");
	}
	
}
