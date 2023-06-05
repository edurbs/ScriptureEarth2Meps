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

	private Book book;
	private BibleSetup bibleSetup;
	private Consumer<Float> progressListener;

	public ParseBook(Book book, BibleSetup bibleSetup, Consumer<Float> progressListener) {

		this.book = book;
		this.bibleSetup = bibleSetup;
		this.progressListener = progressListener;
	}

	public void run() {

		try {

			List<Chapter> chapterList = new ArrayList<>();
			float percent = 0;

			String localChapterUrl = book.getUrl();

			while (!localChapterUrl.isEmpty()) {

				bibleSetup.setParsingThisPage(localChapterUrl);
				bibleSetup.setCounter(bibleSetup.getCounter() + 1);
				percent = (100 / bibleSetup.getSomaTotalChapters()) * bibleSetup.getCounter();
				if (percent > 100) {
					percent = 100;
				}
				progressListener.accept(percent);

				if (!localChapterUrl.endsWith("000.html")) { // ignore introduction

					if (bibleSetup.getShouldStop()) {
						return;
					}

					Document doc = null;

					try {
						doc = Jsoup.connect(localChapterUrl).get();
					} catch (IOException e) {
						e.printStackTrace();
					}

					doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);					
					doc.outputSettings().charset("UTF-8");

					// get the next chapter URL ***before*** modifications from Parsepage
					Element nextChapter = doc.selectFirst("a[title=\"Next Chapter\"]");

					// get the current chapter number

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

			}

			book.getFootnotes().stream().forEach(footnote -> {
				book.addHtml("<div>#" + footnote.getChapter() + ":" + footnote.getVerse() + " " + footnote.getNote()
						+ "</div>");
			});

			String fileName = "sbi_" + book.getBookName().getMepsFormat() + ".html";
			book.setFileName(fileName);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
