package com.scriptureearth2meps.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.scriptureearth2meps.model.Book;
import com.scriptureearth2meps.model.Chapter;
import com.scriptureearth2meps.model.Footnote;

public class ParseBook extends Thread {
	
	//private CyclicBarrier barrier;
	private Book book;
	private BibleSetup bibleSetup;
	private Consumer<Float> progressListener;
	
	public ParseBook(Book book, BibleSetup bibleSetup, Consumer<Float> progressListener) {
		//this.barrier = barrier;
		this.book = book;
		this.bibleSetup =bibleSetup;
		this.progressListener = progressListener;
	}
	

	public void run() {
		
		try {
			
			List<Chapter> chapterList = new ArrayList<>();

			// this.setChapterUrl(book.getUrl());
			String localChapterUrl = book.getUrl();

			while (!localChapterUrl.isEmpty()) { // TODO cancel does not show the download button

				

				if (!localChapterUrl.endsWith("000.html")) { // ignore introduction

					Document doc = null;

					// doc = Jsoup.connect(localChapterUrl).get();

					try {
						doc = Jsoup.connect(localChapterUrl).get();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
					doc.outputSettings().charset("UTF-8");

					// get the next chapter URL
					Element nextChapter = doc.selectFirst("a[title=\"Next Chapter\"]");

					// get the current chapter number
					// setCurrentChapter(Integer
					// .parseInt(chapterUrl.substring(chapterUrl.length() - 8, chapterUrl.length() -
					// 5)));
					int localCurrentChapter = Integer.parseInt(
							localChapterUrl.substring(localChapterUrl.length() - 8, localChapterUrl.length() - 5));

					Chapter chapter = new Chapter(book);
					chapterList.add(chapter);

					ParsePage parsePage = new ParsePage(bibleSetup, doc, book, localCurrentChapter);

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
						hrefNextChapter = localChapterUrl.substring(0, localChapterUrl.lastIndexOf("/") + 1)
								+ hrefNextChapter;
						String nameScripEarth = book.getBookName().toString().substring(2);

						// chapterurl
						if (hrefNextChapter.contains("-" + nameScripEarth + "-") // if it's from the same book
								&& !hrefNextChapter.equals(localChapterUrl)) {
							localChapterUrl = hrefNextChapter;
						} else {
							localChapterUrl = "";
						}
					} else {
						localChapterUrl = "";
					}

				}
				
				System.out.println("Formating Chapter: " + localChapterUrl);
				bibleSetup.setCounter(bibleSetup.getCounter()+1);
				progressListener.accept((100 / bibleSetup.getSomaTotalChapters()) * bibleSetup.getCounter());

			}

			if (!bibleSetup.getShouldStop()) {

				book.getFootnotes().stream().forEach(footnote -> {
					book.addHtml("<div>#" + footnote.getChapter() + ":" + footnote.getVerse() + " "
							+ footnote.getNote() + "</div>");
				});

				/*
				 * String fileName = null; try { fileName = new PrintResult().html(book); }
				 * catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
				 * }
				 */
				String fileName = "sbi_" + book.getBookName().getMepsFormat() + ".html";
				book.setFileName(fileName);
				// this.fileList.add(fileName);
			}
			
			//this.barrier.await();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
