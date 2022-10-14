package com.scriptureearth2meps.control;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scriptureearth2meps.model.Bible;
import com.scriptureearth2meps.model.Book;
import com.scriptureearth2meps.model.BookName;
import com.scriptureearth2meps.model.JsonBook;
import com.scriptureearth2meps.model.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/*
 * TODO label instruction on top on 2 columns, add instruction to fill the fields
 * TODO and destroy button download when language change or start is clicked
 */

@Service
public class BibleSetup {

	private String languageCode;
	private String bibleCode;
	private String wordSee;
	private String glotal;
	private float counter;
	private float somaTotalChapters;
	private boolean oldStyleBible;
	private List<Language> languageList = new ArrayList<>();
	private List<Bible> bibleList = new ArrayList<>();
	private List<Book> bookList = new ArrayList<>();
	private List<String> fileList = new ArrayList<>();

	private volatile boolean shouldStop = false;

	private String outputZipFileName;
	private byte[] zipBytes;

	private ExecutorService executor;

	public BibleSetup() throws IOException {
		// parse the site ScriptEarth for the language list only once
		if (languageList.size() == 0) {
			makeLanguageList();
		}
	}

	public void setSomaTotalChapters(float somaTotalChapters) {
		this.somaTotalChapters = somaTotalChapters;
	}

	public float getSomaTotalChapters() {
		return somaTotalChapters;
	}

	public void setZipBytes(byte[] zipBytes) {
		this.zipBytes = zipBytes;
	}

	public byte[] getZipBytes() {
		return zipBytes;
	}

	public void setOldStyleBible(boolean oldStyleBible) {
		this.oldStyleBible = oldStyleBible;
	}

	public boolean getOldStyleBible() {
		return this.oldStyleBible;
	}

	public void setOutputZipFileName(String outputZipFileName) {
		this.outputZipFileName = outputZipFileName;
	}

	public String getOutputZipFileName() {
		return outputZipFileName;
	}

	public void setShouldStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
	}

	public boolean getShouldStop() {
		return this.shouldStop;
	}

	public void setCounter(float counter) {
		this.counter = counter;
	}

	public float getCounter() {
		return counter;
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
	 * @return
	 * 
	 * @throws IOException
	 */
	public void makeBibleList() throws IOException, MalformedURLException {
		bibleList.clear();
		setOldStyleBible(false);

		String url = "https://www.scriptureearth.org/data/" + this.getLanguageCode() + "/sab/";

		URL u = new URL(url);
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET");
		huc.connect();
		if (huc.getResponseCode() != 200) {
			return;
		}
		;

		Document bibleListPage = Jsoup.connect(url).get();

		// get all a href with languageCode and with ends with a /
		// <a href="tcaP/">tcaP/</a>
		Elements hrefElements = bibleListPage.select("a[href^=" + this.getLanguageCode() + "]");
		if (hrefElements != null) {

			for (Element element : hrefElements) {

				if (element.text().endsWith(".html")) {
					// old style web Bible, and just one Bible
					setOldStyleBible(true);
					bibleList.add(new Bible(this.getLanguageCode()));
					return;
				}
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

		// <script type="text/javascript" src="js/book-names.js"></script>
		// https://www.scriptureearth.org/data/xav/sab/xav/js/book-names.js
		// https://www.scriptureearth.org/data/aaz/sab/js/book-names.js (oldStyle)

		// get the javascript File
		URL urlBooks = null;
		if (getOldStyleBible()) {
			urlBooks = new URL("https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/js/book-names.js");
		} else {
			urlBooks = new URL("https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/" + getBibleCode()
					+ "/js/book-names.js");
		}

		// TODO error with AAZ

		System.out.println(urlBooks.toString());

		String jsonBooks = null;
		try (InputStream in = urlBooks.openStream()) {
			jsonBooks = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		// fix the javascript file
		int lastComma = jsonBooks.lastIndexOf(",");
		// jsonBooks = jsonBooks.substring(11, jsonBooks.length() - 5);
		jsonBooks = jsonBooks.substring(11, lastComma);

		jsonBooks = jsonBooks + "\n]";
		jsonBooks = jsonBooks.replaceAll("name", "\"name\"");
		jsonBooks = jsonBooks.replaceAll("ref", "\"ref\"");
		System.out.println(jsonBooks);

		Type listType = new TypeToken<ArrayList<JsonBook>>() {
		}.getType();
		ArrayList<JsonBook> jsonBooksList = new Gson().fromJson(jsonBooks, listType);

		for (JsonBook jsonBook : jsonBooksList) {

			String urlBook;
			if (getOldStyleBible()) {
				urlBook = "https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/" + jsonBook.getRef();
			} else {
				urlBook = "https://www.scriptureearth.org/data/" + getLanguageCode() + "/sab/" + getBibleCode() + "/"
						+ jsonBook.getRef();
			}

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

			bookList.add(book);

		}

	}

	// private List<Chapter> makeChaptersList(Book book) throws Exception {
	// private ListenableFuture<String> makeChaptersList() throws Exception {
	// public void process(Consumer<Float> progressListener, Runnable
	// succeededListener) throws Exception {
	@Async
	public void process(Consumer<Float> progressListener, Runnable succeededListener) throws Exception {
		this.outputZipFileName = null;
		makeBookList();

		for (Book book : getBookList()) {
			somaTotalChapters += book.getTotalChapters();
		}


		// parse 3 book at same time
		this.executor = Executors.newFixedThreadPool(3);



		new Thread(() -> {

			for (Book book : getBookList()) {

				executor.execute(new ParseBook(book, this, progressListener));

			}

			if (!shouldStop) {

				try {
					createZip();
				} catch (IOException | InterruptedException   e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			succeededListener.run();

		}).start();
		
		

	}

	public void createZip() throws IOException, InterruptedException {
		
		// wait for all book parsers to finish
		//this.barrier.await();
		this.executor.shutdown(); 
		this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		this.outputZipFileName = "sbi_" + Calendar.DAY_OF_YEAR + Calendar.HOUR_OF_DAY + Calendar.MINUTE
				+ Calendar.SECOND + "_" + this.getBibleCode() + ".zip";

		/*
		 * // create a ZipOutputStream object FileOutputStream fos = new
		 * FileOutputStream(outputZipFileName); ZipOutputStream zos = new
		 * ZipOutputStream(fos); this.fileList.clear();
		 * 
		 * for (String file : this.fileList) {
		 * 
		 * File srcFile = new File(file); FileInputStream fis = new
		 * FileInputStream(srcFile);
		 * 
		 * // Start writing a new file entry zos.putNextEntry(new
		 * ZipEntry(srcFile.getName()));
		 * 
		 * int length; // create byte buffer byte[] buffer = new byte[1024];
		 * 
		 * // read and write the content of the file while ((length = fis.read(buffer))
		 * > 0) { zos.write(buffer, 0, length); } // current file entry is written and
		 * current zip entry is closed zos.closeEntry();
		 * 
		 * // close the InputStream of the file fis.close();
		 * 
		 * }
		 * 
		 * // close the ZipOutputStream zos.close();
		 */

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(baos);

		for (Book book : getBookList()) {

			String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta></head>"
					+ book.getHtml().toString() + "</html>";
			ByteArrayInputStream bais = new ByteArrayInputStream(html.getBytes());

			ZipEntry zipEntry = new ZipEntry(book.getFileName());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = bais.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			bais.close();
		}

		zipOut.close();

		baos.close();

		this.zipBytes = baos.toByteArray();

	}

}
