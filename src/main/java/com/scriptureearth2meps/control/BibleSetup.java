package com.scriptureearth2meps.control;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scriptureearth2meps.model.Bible;
import com.scriptureearth2meps.model.Book;
import com.scriptureearth2meps.model.BookName;
import com.scriptureearth2meps.model.Chapter;
import com.scriptureearth2meps.model.Footnote;
import com.scriptureearth2meps.model.JsonBook;
import com.scriptureearth2meps.model.Language;
import com.scriptureearth2meps.report.PrintResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class BibleSetup {

	private String languageCode;
	private String bibleCode;
	private String wordSee;
	private int currentChapter;
	private String glotal;
	private String chapterUrl;
	private float counter;
	private float somaTotalChapters;

	private List<Language> languageList = new ArrayList<>();
	private List<Bible> bibleList = new ArrayList<>();
	private List<Book> bookList = new ArrayList<>();
	private List<String> fileList = new ArrayList<>();

	private volatile boolean shouldStop = false;

	public BibleSetup() throws IOException {
		// parse the site ScriptEarth for the language list only once
		if (languageList.size() == 0) {
			makeLanguageList();
		}
	}

	public void setShouldStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
	}

	public void setCounter(float counter) {
		this.counter = counter;
	}

	public float getCounter() {
		return counter;
	}

	public void setChapterUrl(String chapterUrl) {
		this.chapterUrl = chapterUrl;
	}

	public String getChapterUrl() {
		return chapterUrl;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setGlotal(String glotal) {
		this.glotal = glotal;
	}

	public String getGlotal() {
		return glotal;
	}

	public void setCurrentChapter(int currentChapter) {
		this.currentChapter = currentChapter;
	}

	public int getCurrentChapter() {
		return currentChapter;
	}

	public void setWordSee(String wordSee) {
		this.wordSee = wordSee;
	}

	public String getWordSee() {
		return wordSee;
	}

	public void setBookList(List<Book> bookList) {
		this.bookList = bookList;
	}

	public List<Book> getBookList() {
		return bookList;
	}

	public List<Language> getLanguageList() {
		return languageList;
	}

	public void setLanguageList(List<Language> languageList) {
		this.languageList = languageList;
	}

	public List<Bible> getBibleList() {
		return bibleList;
	}

	public void setBibleList(List<Bible> bibleList) {
		this.bibleList = bibleList;
	}

	public void setBibleCode(String bibleCode) {
		this.bibleCode = bibleCode;
	}

	public String getBibleCode() {
		return bibleCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public boolean hasMoreBibles() throws IOException {

		boolean moreBibles;

		int qtBibles = bibleList.size();

		if (qtBibles > 1) { // if the language has more then 1 bible
			moreBibles = true;
			setBibleCode(null);
			// makeBibleList();
		} else {
			moreBibles = false;
			setBibleCode(getLanguageCode()); // default equais the language code
		}

		return moreBibles;
	}

	public boolean verifyWebBible() throws IOException {
		makeBibleList();
		return bibleList.isEmpty() ? false : true;

	}

	/**
	 * get the bible's list from https://www.scriptureearth.org/data/
	 * 
	 * @throws IOException
	 */
	public void makeLanguageList() throws IOException {

		Document languageListPage = Jsoup.connect("https://www.scriptureearth.org/data/").get();
		// get all <a href="xxx/"
		Elements hrefElements = languageListPage.select("a[href~=\\b[a-z]{3}\\/]");
		hrefElements.stream().forEach(href -> {
			languageList.add(new Language(href.text().substring(0, 3)));
		});

	}

	/**
	 * create the list of languages avaiable at
	 * https://www.scriptureearth.org/data/language/sab/
	 * 
	 * @throws IOException
	 */
	public void makeBibleList() throws IOException, MalformedURLException {
		bibleList.clear();

		String url = "https://www.scriptureearth.org/data/" + this.getLanguageCode() + "/sab/";

		Document bibleListPage = Jsoup.connect(url).get();

		// get all a href with languageCode and with ends with a /
		// <a href="tcaP/">tcaP/</a>
		Elements hrefElements = bibleListPage.select("a[href^=" + this.getLanguageCode() + "]");
		if (hrefElements != null) {

			for (Element element : hrefElements) {
				bibleList.add(new Bible(element.text().substring(0, 4)));

			}
		}

	}

	/**
	 * make the list of the books avaiable at scriptureEarch of this Language
	 * 
	 * @throws Exception
	 */

	private void makeBookList() throws Exception {

		// TODO make book list First, and later the parsing

		// <script type="text/javascript" src="js/book-names.js"></script>
		// https://www.scriptureearth.org/data/xav/sab/xav/js/book-names.js

		// get the javascript File
		URL urlBooks = new URL("https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/" + getBibleCode()
				+ "/js/book-names.js");
		String jsonBooks = null;
		try (InputStream in = urlBooks.openStream()) {
			jsonBooks = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		// fix the javascript file
		jsonBooks = jsonBooks.substring(11, jsonBooks.length() - 5);
		jsonBooks = jsonBooks + "\n]";
		jsonBooks = jsonBooks.replaceAll("name", "\"name\"");
		jsonBooks = jsonBooks.replaceAll("ref", "\"ref\"");

		Type listType = new TypeToken<ArrayList<JsonBook>>() {
		}.getType();
		ArrayList<JsonBook> jsonBooksList = new Gson().fromJson(jsonBooks, listType);

		for (JsonBook jsonBook : jsonBooksList) {

			String urlBook = "https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/" + getBibleCode() + "/"
					+ jsonBook.getRef();

			// get the book name from site
			String nameScriptureEarth = urlBook.substring(urlBook.length() - 12, urlBook.length() - 9);

			// get the book number in ScriptureEarch enum
			int bookNumber;
			int bookTotalChapters;
			try {
				bookNumber = BookName.ANY.getBookOrdinal("T_" + nameScriptureEarth);
				bookTotalChapters = BookName.valueOf("T_" + nameScriptureEarth).getNumberOfChapters();
			} catch (IllegalArgumentException e) {
				// No enum constant parseScriptureEarth.Model.BookNameScriptureEarth.T_XXD
				continue;
			}

			// get the MEPS book name by number without "T"
			String nameMeps = BookName.values()[bookNumber].toString();
			nameMeps = nameMeps.substring(2);

			Book book = new Book(BookName.valueOf("T_" + nameScriptureEarth.toUpperCase()), urlBook);
			book.setTotalChapters(bookTotalChapters);

			// System.out.println("*******Formating Book: " + book.getNameMeps());

			// List<Chapter> chapterList = makeChaptersList(book);

			// book.setChapters(chapterList);

			bookList.add(book);

			// createZip();
		}

	}

	// private List<Chapter> makeChaptersList(Book book) throws Exception {
	// private ListenableFuture<String> makeChaptersList() throws Exception {
	// public void process(Consumer<Float> progressListener, Runnable
	// succeededListener) throws Exception {
	@Async
	public void process(Consumer<Float> progressListener, Runnable succeededListener) throws Exception {

		makeBookList();

		for (Book book : getBookList()) {
			somaTotalChapters += book.getTotalChapters();
		}

		// while (!Thread.currentThread().isInterrupted()) {

		new Thread(() -> {

			for (Book book : getBookList()) {

				List<Chapter> chapterList = new ArrayList<>();

				this.setChapterUrl(book.getUrl());

				while (!chapterUrl.isEmpty() && !shouldStop) {

					System.out.println("Formating Chapter: " + chapterUrl);

					counter++;

					progressListener.accept((100 / somaTotalChapters) * counter);

					if (!chapterUrl.endsWith("000.html")) { // ignore introduction

						Document doc = null;
						try {
							doc = Jsoup.connect(chapterUrl).get();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
						doc.outputSettings().charset("UTF-8");

						// get the next chapter URL
						Element nextChapter = doc.selectFirst("a[title=\"Next Chapter\"]");

						// get the current chapter number
						setCurrentChapter(Integer
								.parseInt(chapterUrl.substring(chapterUrl.length() - 8, chapterUrl.length() - 5)));

						Chapter chapter = new Chapter(book);
						chapterList.add(chapter);

						ParsePage parsePage = new ParsePage(this, doc, book);

						/*
						 * Footnotes Add a hard return at the end of each line with footnote text. Place
						 * a Number sign (#) at the start of a line with footnote text. Place all
						 * footnote text at the end of the Bible book.
						 */
						List<Footnote> footnotes = parsePage.getFootnotes();
						for (Footnote footnote : footnotes) {
							book.addFootnote(footnote);
						}

						book.addHtml(parsePage.getStringHtml().toString());

						// if it has another chapter and not equais this chapter
						if (nextChapter != null) {

							String hrefNextChapter = nextChapter.attr("href");
							hrefNextChapter = chapterUrl.substring(0, chapterUrl.lastIndexOf("/") + 1)
									+ hrefNextChapter;
							String nameScripEarth = book.getBookName().toString().substring(2);

							// chapterurl
							if (hrefNextChapter.contains("-" + nameScripEarth + "-") // if it's from the same book
									&& !hrefNextChapter.equals(chapterUrl)) {
								chapterUrl = hrefNextChapter;
							} else {
								chapterUrl = "";
							}
						} else {
							chapterUrl = "";
						}

					}
				}

				book.getFootnotes().stream().forEach(footnote -> {
					book.addHtml("<div>#" + footnote.getChapter() + ":" + footnote.getVerse() + " " + footnote.getNote()
							+ "</div>");
				});

				String fileName = null;
				try {
					fileName = new PrintResult().html(book);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.fileList.add(fileName);
			}

			// return chapterList;
			succeededListener.run();

		}).start();

	}

	public void createZip() throws IOException {
		String outputZipFileName = "C:\\Users\\edurb\\Downloads\\sbi" + Calendar.DAY_OF_YEAR + Calendar.HOUR_OF_DAY
				+ Calendar.MINUTE + Calendar.SECOND + "_" + this.getBibleCode() + ".zip";

		// create a ZipOutputStream object
		FileOutputStream fos = new FileOutputStream(outputZipFileName);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (String file : this.fileList) {

			File srcFile = new File(file);
			FileInputStream fis = new FileInputStream(srcFile);

			// Start writing a new file entry
			zos.putNextEntry(new ZipEntry(srcFile.getName()));

			int length;
			// create byte buffer
			byte[] buffer = new byte[1024];

			// read and write the content of the file
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			// current file entry is written and current zip entry is closed
			zos.closeEntry();

			// close the InputStream of the file
			fis.close();

		}

		// close the ZipOutputStream
		zos.close();

	}

}
