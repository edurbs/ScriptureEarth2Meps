package com.scriptureearth2meps.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scriptureearth2meps.model.Book;
import com.scriptureearth2meps.model.BookName;
import com.scriptureearth2meps.model.Footnote;

public class ParsePage   {
	

	private StringBuilder stringHtml = new StringBuilder();
	private List<Footnote> footnotes = new ArrayList<>();
	private String javaScriptFootnotes = null;

	BibleSetup bibleSetup = null;


	public ParsePage(BibleSetup bibleSetup, Document document, Book book, int currentChapter) {

		/*
		 * Step 3:2 Remove unwanted text such as introductions, comments, footers, and
		 * page numbers. Note: Text such as headings, superscriptions, and footnotes
		 * should not be removed.
		 */
		document.getElementById("book-menu").remove();
		document.getElementById("chapter-menu").remove();
		document.getElementById("toolbar-top").remove();
		document.getElementsByAttributeValue("class", "footer").remove();
		document.select("span[id^=bookmarks]").remove();
		document.select("a").remove();
		document.select("span[class=vsp]").remove(); // remove &nbsp
		document.select("div[class=b]").remove();
		document.select("div[class=video-block]").remove();

		// remove book prefix name
		/*
		Element mt = document.selectFirst("div[class=mt]");
		*/
		
		Elements mts = document.select("div[class=mt]");
		Element mt = null;
		for (int i = 0; i < mts.size(); i++) {
			if(i==0) {
				mt = document.selectFirst("div[class=mt]");
			}else {
				//mt.remove();
				mts.get(i).remove();				
			}
		}
		
		Element mt2 = document.selectFirst("div[class=mt2]");
		if (mt2 != null) {
			mt.after(mt2.outerHtml());
			mt2.remove();
		}
		
		

		/*
		 * Step 3:3 Remove unnecessary formatting such as bulleted list styles or
		 * hyperlinks.
		 */
		document.getElementsByAttributeValue("class", "r").remove();

		/*
		 * Step 3:3 Note: If the text contains attributes, such as italics, this can
		 * remain so as to retain the original formatting of the text
		 */
		document.getElementsByAttributeValue("class", "it").tagName("i");

		/*
		 * Step 4A:3 a. Add curly brackets { } around the number. For example, chapter
		 * 10 would appear as {10}. b. Ensure that one space exists after each chapter
		 * number.
		 */

		String chapterNumber = null;
		Element cDrop = document.selectFirst("div.c-drop");
		if (cDrop != null) {
			chapterNumber = cDrop.text();
			cDrop.prependText("{").appendText("} "); // add {}
			cDrop.tagName("span"); // change to span
		}

		/*
		 * Step 4A:4 b. Make sure that each digit is bold.
		 */
		String markupInicial = "\uF850";
		String markupFinal = "\uF851";
		String see = bibleSetup.getWordSee();

		Elements verses = document.select("span[class=v]");
		Iterator<Element> iterator = verses.iterator();

		Element addToNextVerse = null;
		
		int lastVerse=verses.size();
		String lastVerseNumver = null;

		while (iterator.hasNext()) {
			Element verse = iterator.next();

			if (addToNextVerse != null) {
				verse.before(addToNextVerse.html());
				addToNextVerse = null;
			}

			// Make sure that each digit is bold.
			String verseNumver = verse.text();
			lastVerseNumver=verseNumver;
			verse.text(" ");
			verse.append("<strong>" + verseNumver + "</strong> ");

			/*
			 * IF VERSES WERE PUT TOGETHER OR PARAPHRASED
			 * 
			 * The content of the verses and a comment can be included in the first verse.
			 * The other verses can be left with two em dashes or equivalent vernacular
			 * characters.
			 * As an option, you can add comments in the other verses.
			 */

			// search for hifen and split it

			if (verse.text().contains("-")) { // united verses
				String[] unitedVerses = verse.text().split("-");
				
				int firstUnitedVerse=Integer.parseInt(unitedVerses[0].replace("[^\\d.]", ""));
				int lastUnitedVerse=Integer.parseInt(unitedVerses[1].replace("[^\\d.]", ""));

				lastVerseNumver=Integer.toString(lastUnitedVerse);

				int diff = lastUnitedVerse - firstUnitedVerse;
								
				lastVerse += diff;

				// Ester 1 (first verse united)
				if(firstUnitedVerse == 1){
					lastVerse--;
				}

				// remover the number of the first united verse
				verse.text(" ");

				// if it is not the first verse of the chapter, add que verse number
				if(firstUnitedVerse > 1){
					verse.append(" <strong>" + unitedVerses[0] + "</strong> ");
				}

				// append in the scripture which verses was united ex. 1:22-23
				verse.appendElement("span").text(" " + markupInicial + chapterNumber + ":" + unitedVerses[0] + "-"
						+ unitedVerses[1] + " " + markupFinal).attr("style", "font-family:MEPS Bookman WTS;");

				Element newVerse = new Element("span");

				// add the noexistent verse numbers on html
				for (int i = Integer.parseInt(unitedVerses[0]) + 1; i < Integer.parseInt(unitedVerses[1]) + 1; i++) {

					// if it is not the first verse of the chapter, add que verse number
					if(i > 1){
						newVerse.appendElement("strong").text(" " + String.valueOf(i));
					}	

					if (see.isEmpty()) {
						newVerse.appendElement("span").text(" —— ");						
					} else {
						newVerse.appendElement("span").text(" " + markupInicial + see + " " + chapterNumber + ":"
								+ unitedVerses[0] + markupFinal + " ").attr("style", "font-family:MEPS Bookman WTS;");
					}
				}

				/*
				 * if there's no more verse in the chapter, add at the end of the div: parent()
				 * if there no more verse in the chapter, add at the start of the next verse
				 * number: span class=v
				 */
				if (!iterator.hasNext()) { // if there's NO more verse in the chapter
					// add at the end of the div: parent()
					newVerse.appendTo(verse.parent());

				} else { // if there's more verse in the chapter,
					// add at the start of the next verse number
					addToNextVerse = newVerse;
				}
			}
		}

		/*
		 * handle with chapters with no verses, add text inside: ——
		 */

		int totalVersesEnum = book.getBookName().getNumberOfScriptures(currentChapter);

		// create and add verses on the div id content
		Element elementDivClassContent = document.selectFirst("div[id=content]");

		if (verses.size() == 0 && elementDivClassContent != null) {
			lastVerse=totalVersesEnum;
			elementDivClassContent.appendElement("span").text("{" + currentChapter + "} ");
			for (int i = 1; i <= totalVersesEnum; i++) {
				if (i > 1) elementDivClassContent.appendElement("strong").text(String.valueOf(i));
				elementDivClassContent.appendElement("span").text(" —— ");

			}
		}

		/*
		 * hanble with chapter with extra verses or fewer 
		 */
		if(lastVerseNumver != null){
			int lastVerseNumverInt = Integer.parseInt(lastVerseNumver);
	
			if (lastVerseNumverInt > totalVersesEnum) {
				// change the tag of the new verses to sup				
				verses.last().tagName("sup");
			} else if (lastVerseNumverInt < totalVersesEnum) {
				// complete with new verses
				
				if(elementDivClassContent != null) {
					Element div=elementDivClassContent.appendElement("div");
					for (int i = lastVerseNumverInt+1; i <= totalVersesEnum; i++) {
						
						div.appendElement("strong").text(String.valueOf(i));
						div.appendElement("span").text(" —— ");
	
					}	
				}
			}
		}

		/*
		 * Step 4A:1 b. Remove the verse number from the first verse in each chapter, if
		 * it has been included in the pasted text.
		 * 
		 * Step 4A:2 For books not containing chapters, add verse number one to the
		 * beginning of the first verse, if it has not been included in the pasted text.
		 */

		// if the book does not have just one chapter
		int totalChapters = book.getBookName().getNumberOfChapters();
		
		Element spanV = document.selectFirst("span.v");		
		Element spanCDrop = document.selectFirst("span[class=c-drop]");

		if (totalChapters > 1 && spanV != null) {
			spanV.remove(); // delete verse 1
		} else if (totalChapters == 1 && spanCDrop != null) {			
			spanCDrop.after(" <strong>1</strong> ");
		}

		/*
		 * TITLE A hard return must appear at the end of the first line, which includes
		 * unique numbers important for processing of the file.
		 */

		// get the book name from site
		// String bookNameScriptureEarth = url.substring(url.length() - 12, url.length()
		// - 9);
		BookName bookNameScriptureEarth = book.getBookName();

		// get the book number in ScriptureEarch enum
		// int bookNumber = BookNameScriptureEarth.valueOf("T_" +
		// bookNameScriptureEarth.toUpperCase()).ordinal();
		int bookNumber = bookNameScriptureEarth.ordinal();

		// get the MEPS book name by number without "T"
		// String bookNameMeps = BookNameScriptureEarth.values()[bookNumber].toString();
		// bookNameMeps = bookNameMeps.substring(2);
		// String bookNameMeps =
		// BookNameMeps.values()[bookNumber].toString().substring(2);

		/*
		 * Formatting characters Make sure that a Percent sign (%) appears at the start
		 * of the first line with the book number and at the start of the second line
		 * with the Bible book name.
		 */
		Element divClassMt = document.selectFirst("div[class=mt]");
		Element spanClassMt = document.selectFirst("span[class=mt]");
		if (divClassMt != null) { // se for o primeiro capítulo
			document.getElementsByTag("body").before("%" + String.format("%02d", bookNumber + 1));
			divClassMt.prependElement("span").text("$");
			spanClassMt.tagName("Strong");
		}

		/*
		 * Place a Dollar sign ($) at the start of a line with a superscription.
		 */
		// get the chapter number
		//Element spanCDrop = document.selectFirst("span[class=c-drop]");
		if (spanCDrop != null) {

			String capTag = spanCDrop.text();

			capTag = capTag.substring(1, capTag.length() - 1);
			int cap = Integer.parseInt(capTag);

			// all chapters of Salm without a superscription
			boolean capHasSuper = switch (cap) {
				case 1, 2, 10, 33, 43, 71, 91, 93, 94, 95, 96, 97, 99, 104, 105, 106, 107, 111, 112, 113, 114, 115, 116,
						117, 118, 119, 135, 136, 137, 146, 147, 148, 149, 150 ->
					false;
				default -> true;
			};

			// if superscription exists, add the sign $ at the start
			Element divIdD1 = document.selectFirst("div[id=d1]");
			if (capHasSuper && divIdD1 != null && book.getBookName().ordinal() == 18) {
				divIdD1.prependText("$");
			}
			// IF THE SUPERSCRIPTION IS MISSING IN THE SECULAR BIBLE and the book is Psalm:
			// else if (capHasSuper && divIdD1 != null && book.getBookName().ordinal() == 18) {
			// 	// Add an empty superscription befora the chapter.
			// 	//spanCDrop.before("<div>$</div>");
			// 	divIdD1.prependText("$");

			// }

		}

		/*
		 * Place an At sign (@) at the start of a line with any heading other than a
		 * book division or superscription. Note: If there are two headings in a row,
		 * place the At sign (@) before both.
		 */

		Elements headers = document.select("div[class=s]");
		for (Element header : headers) {
			Elements children = header.children();
			for (Element child : children) {
				child.prependText("@");
			}
		}

		Element divClassMt2 = document.selectFirst("div[class=mt2]");
		if (mt2 != null && divClassMt2 != null && divClassMt2.firstElementChild() != null) {
			divClassMt2.firstElementChild().prependText("@");
		}

		/*
		 * ======Poetic text a. Add a soft return </br> (Shift+Enter) at the end of each
		 * line. OK b. Add a hard return at the end of a stanza. OK c. When poetic text
		 * starts in the middle of a verse, add a soft return (Shift+Enter) to the end
		 * of the line preceding the poetic text. OK
		 */

		Elements divsPoetic = document.select("div[class^=q]"); // search for all div with class starting with q
		for (Element divPoetic : divsPoetic) {

			divPoetic.tagName("span");

			// só adicionar BR se o próximo for class=q* (exceto versículos)
			Element nextElement = divPoetic.nextElementSibling();
			if (nextElement != null) {
				if (nextElement.className().startsWith("q") && !nextElement.className().startsWith("v")) {
					//divPoetic.append("<br>").attr("id", "none");
					divPoetic.appendElement("br");
					//divPoetic.after("<br></br>");
				}

			}

			Element previousElement = divPoetic.previousElementSibling();
			if (previousElement.lastElementChild() != null) {

				// verifica se o div anterior começa com class=v, se sim finaliza com <br>
				if (previousElement.lastElementChild().className().equals("v")) {
					//previousElement.append("<br/>");
					previousElement.appendElement("br");
					//previousElement.after("<br></br>");
					previousElement.tagName("span");
				}
			}

			/*
			 * Formatting characters Place an Equals sign (=) before the first chapter or
			 * verse number where poetic text starts. Note: If a Bible book begins with
			 * poetic text, place the Equals sign (=) at the beginning of the second verse
			 * containing poetic text instead. If poetic text starts in the middle of a
			 * verse, no Equals sign (=) is necessary.
			 */

			// se o ultimo foi div class=p ou span class=p ou div class=m
			// então adiciona "=" antes do versículo span b class=v (se existir)

			if ((previousElement.selectFirst("div[class=p]") != null
					|| previousElement.selectFirst("span[class=p]") != null
					|| previousElement.selectFirst("div[class=m]") != null) && divPoetic.firstElementChild() != null
					&& divPoetic.firstElementChild().className().equals("v")
					&& divPoetic.selectFirst("b[class=v]") != null) {
				// if first element is a verse, add = in last block/verse
				divPoetic.selectFirst("b[class=v]").prependText("=");
			}

			/*
			 * Place a Plus sign (+) at the start of a line when body text immediately
			 * follows poetic text or any type of heading.
			 */
			if (nextElement != null) {
				if (nextElement.selectFirst("div[class=m]") != null || nextElement.selectFirst("span[class=p]") != null
						|| nextElement.selectFirst("div[class=p]") != null) {
					if (nextElement.selectFirst("b[class=v]") != null) {
						nextElement.selectFirst("b[class=v]").prependText("+"); // add before verse
					} else {
						nextElement.prependText("+"); // add before text
					}
				}
			}

		}

		/*
		 * Footnotes
		 * 
		 * • Replace each footnote reference symbol with an Asterisk (*) in the body
		 * text. DONE
		 *
		 */

		Elements sups = document.select("sup");

		for (Element sup : sups) {
			String id = sup.parent().id(); // get the note key,

			sup.text("*"); // Replace each footnote reference symbol with an Asterisk (*) in the body text.

			if (id != null && getJavaScriptFootnotes() == null) { // get the var from javascript just once

				Elements scripts = document.select("script");
				scripts.stream().forEach(script -> { // do a "foreach" using lambda
					int start = script.toString().indexOf("var footnotes = ");
					int end = script.toString().indexOf("initFootnotes");
					if (start > 0) {
						setJavaScriptFootnotes(script.toString().substring(start + 16, end - 3));
						return;
					}
				});
			}
		}

		if (getJavaScriptFootnotes() != null) {

			HashMap<String, String> mapFootnotes = new Gson().fromJson(getJavaScriptFootnotes(),
					new TypeToken<HashMap<String, String>>() {
					}.getType());

			for (String key : mapFootnotes.keySet()) {

				// extract and set the chapter and verse number

				String noteHtml = "<html><body>" + mapFootnotes.get(key) + "</body></html>";
				Document documentNoteHtml = getDocFromString(noteHtml);
				documentNoteHtml.outputSettings().charset("UTF-8");

				// get the Chapter and Verse, and later remove it
				Element elementFr = documentNoteHtml.selectFirst("span[class=fr]");
				String[] stringFr = elementFr.text().split("\\.");
				elementFr.remove();

				// get the footnote
				Element elementFootnote = documentNoteHtml.selectFirst("div[class=footnote-p]");
				String stringFootnote = elementFootnote.text();

				// handle with united verses
				if (stringFr[1].contains("-")) { // united verses
					String[] unitedVersesFr = stringFr[0].split("-");
					stringFr[1] = unitedVersesFr[0];
				}

				this.footnotes.add(new Footnote(key, stringFootnote, Integer.parseInt(stringFr[0]),
						Integer.parseInt(stringFr[1])));
			}
		}
		
		/*
		 * Change the apostrofe to glotal
		 */		
		String apostrophe = "\u0027"; 		
		String glotal = bibleSetup.getGlotal();
		
		String htmlWithGlotal;
		if (!glotal.isBlank()) {
			htmlWithGlotal = document.getElementsByTag("body").html().replace(apostrophe, glotal);			
		}else {
			htmlWithGlotal = document.getElementsByTag("body").html();
		}

		/*
		 * ================END=================== Append only the body content
		 */

		stringHtml.append(htmlWithGlotal);

	}

	private Document getDocFromString(String string) {
		return Jsoup.parse(string);
	}

	public void setFootnotes(List<Footnote> footnotes) {
		this.footnotes = footnotes;
	}

	public List<Footnote> getFootnotes() {
		return footnotes;
	}

	public void setJavaScriptFootnotes(String javaScriptFootnotes) {
		this.javaScriptFootnotes = javaScriptFootnotes;
	}

	public String getJavaScriptFootnotes() {
		return javaScriptFootnotes;
	}

	public void setStringHtml(StringBuilder stringHtml) {
		this.stringHtml = stringHtml;
	}

	public StringBuilder getStringHtml() {
		return stringHtml;
	}

}
