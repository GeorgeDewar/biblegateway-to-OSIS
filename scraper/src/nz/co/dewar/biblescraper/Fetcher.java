package nz.co.dewar.biblescraper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

import static nz.co.dewar.biblescraper.Common.NUM_BOOKS_NT;
import static nz.co.dewar.biblescraper.Common.NUM_BOOKS_OT;

/**
 * This utility simply fetches and stores the raw pages from BibleGateway.com
 */
public class Fetcher {
    private String translationName;
    static final String source = "http://mobile.biblegateway.com/passage/?search={BOOK}%20{CHAPTER}&version={VERSION}";

    public Fetcher(String translationName) {
        this.translationName = translationName;
    }

    public void fetch() throws IOException, InterruptedException {
        // Prepare
        new File("raw_html/" + translationName).mkdirs();

        // Get all the books of the old and new testament
        Scanner bookScanner = new Scanner(new File("books.dat"));
        getTestament(bookScanner, NUM_BOOKS_OT);
        getTestament(bookScanner, NUM_BOOKS_NT);
        bookScanner.close();
    }

    private void getTestament(Scanner scanner, int numBooks) throws IOException, InterruptedException {
        for(int i=0; i<numBooks; i++){
            String book = scanner.next();
            int numChapters = scanner.nextInt();
            getBook(book, numChapters, translationName);
        }
    }

    private void getBook(String book, int numChapters, String version) throws IOException, InterruptedException {
        System.out.print("Fetching " + book);

        for(int i=1; i<=numChapters; i++){
            System.out.print(".");
            getChapter(book, i, version);
        }

        System.out.println();
    }

    private void getChapter(String book, int chapter, String version) throws IOException, InterruptedException {
        var filename = "raw_html/" + this.translationName + "/" + book + "_" + chapter + ".html";
        // Return if the file already exists
        if (new File(filename).exists()) return;

        String url = source
                .replace("{BOOK}", book)
                .replace("{CHAPTER}", String.valueOf(chapter))
                .replace("{VERSION}", version);

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        var response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var body = response.body();
            var fileWriter = new FileWriter(filename);
            fileWriter.write(body);
            fileWriter.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new Fetcher("NASB").fetch();
    }
}
