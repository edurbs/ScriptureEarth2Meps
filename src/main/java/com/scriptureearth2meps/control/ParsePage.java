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
	
	private static final String SPAN = "span";
	private static final String STRONG = "strong";
	private static final String FONT_MEPS = "font-family:Markup;";
	private StringBuilder stringHtml = new StringBuilder();
	private List<Footnote> footnotes = new ArrayList<>();
	private String javaScriptFootnotes = null;

	BibleSetup bibleSetup = null;


	public ParsePage(BibleSetup bibleSetup, Document document, Book book, int currentChapter) {

		this.bibleSetup = bibleSetup;

		/*
		 * Step 3:2 Remove unwanted text such as introductions, comments, footers, and
		 * page numbers. Note: Text such as headings, superscriptions, and footnotes
		 * should not be removed.
		 */
		removeUnwantedText(document);

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

		Element lastVerse = handleUnitedVerses(document);
		
		/*
		 * handle with chapters with no verses, add text inside: ——
		 */

		
		/*
		 * hanble with chapter with extra verses or fewer 
		 */
		handleExtraOrFewerVerses(lastVerse, book, document, currentChapter);

		/*
		 * Step 4A:1 b. Remove the verse number from the first verse in each chapter, if
		 * it has been included in the pasted text.
		 * 
		 * Step 4A:2 For books not containing chapters, add verse number one to the
		 * beginning of the first verse, if it has not been included in the pasted text.
		 */

		Element spanCDrop = handleFirstVerseNumber(document, book);

		/*
		 * TITLE A hard return must appear at the end of the first line, which includes
		 * unique numbers important for processing of the file.
		 */

		/*
		 * Formatting characters Make sure that a Percent sign (%) appears at the start
		 * of the first line with the book number and at the start of the second line
		 * with the Bible book name.
		 */

		addPercentSign(document, book);

		/*
		 * Place a Dollar sign ($) at the start of a line with a superscription.
		 */		
		
		addDollarSign(document, book, spanCDrop);

		/*
		 * Place an At sign (@) at the start of a line with any heading other than a
		 * book division or superscription. Note: If there are two headings in a row,
		 * place the At sign (@) before both.
		 */

		addAtSign(document);

	/*
		 * ======Poetic text a. Add a soft return </br> (Shift+Enter) at the end of each
		 * line. OK b. Add a hard return at the end of a stanza. OK c. When poetic text
		 * starts in the middle of a verse, add a soft return (Shift+Enter) to the end
		 * of the line preceding the poetic text. OK
		 */

		handlePoeticText(document);

		/*
		 * Footnotes
		 * 
		 * • Replace each footnote reference symbol with an Asterisk (*) in the body
		 * text. DONE
		 *
		 */

		handleFootnotes(document);
		
		/*
		 * Change the apostrofe to glotal
		 */		
		String htmlWithGlotal = changeGlotal(bibleSetup, document);

		/*
		 * ================END=================== Append only the body content
		 */

		stringHtml.append(htmlWithGlotal);

	}

	private Element handleUnitedVerses(Document document) {
		
		
		/*
		 * Step 4A:3 a. Add curly brackets { } around the number. For example, chapter
		 * 10 would appear as {10}. b. Ensure that one space exists after each chapter
		 * number.
		 */

		String chapterNumber = addBrackets(document);

		Elements verses = document.select("span[class=v]");		

		String markupInicial = "\uF850";
		String markupFinal = "\uF851";
		String see = this.bibleSetup.getWordSee();

		Element addToNextVerse = null;
		Iterator<Element> iterator = verses.iterator();
		
		// int lastVerse=verses.size();
		// String lastVerseNumver = null;
		Element lastElement = verses.last();
		Element lastIterator = null;
		boolean firstVerseOnParagraph = false;
		boolean hasTextBeforeFirstVerse = false;

		while (iterator.hasNext()) {
			Element verse = iterator.next();

			if (addToNextVerse != null) {	
				if (firstVerseOnParagraph){
					if(!hasTextBeforeFirstVerse){
						// add on previous §
						lastIterator.parent().appendChild(addToNextVerse);
					}else{
						// on the same §
						verse.before(addToNextVerse);	
					}
				} else if (lastIterator != null 
						&& lastIterator.nextElementSiblings().isEmpty()
						&& lastIterator.parent() != null
						&& lastIterator.parent().previousElementSibling() != null
						// && verse.firstElementChild()!=null 
						// && verse.firstElementChild().className().equalsIgnoreCase("v") // when is not the first verse in the §
				) {
					// works when no more verses in the paragraph
					lastIterator.parent().previousElementSibling().appendChild(addToNextVerse);				
				// } else if(verse.firstElementChild()!=null
				// 		&& !verse.firstElementChild().className().equalsIgnoreCase("v")){
				// 	// when is the first verse in the §
				// 	lastIterator.parent().appendChild(addToNextVerse);				
				} else {
					// TODO if there are more verses in the div p, 					
					//verse.prependChild(addToNextVerse);
					verse.before(addToNextVerse);
				}
				
				if(verse.parent()!=null 
						&& verse.parent().firstElementChild()!= null
						&& verse.parent().firstElementChild().equals(verse)					
				){
					firstVerseOnParagraph=true;				
					if (verse.siblingIndex()==0) {
						hasTextBeforeFirstVerse=false;
					} else {
						hasTextBeforeFirstVerse=true;
					}
				}else{
					firstVerseOnParagraph=false;
				}
				addToNextVerse = null;
			}

			



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
				//int lastUnitedVerse=Integer.parseInt(unitedVerses[1].replace("[^\\d.]", ""));

				// remove first united verse number
				verse.text(" ");

				// if it is not the first verse of the chapter, add que verse number
				if(firstUnitedVerse > 1){
					verse.appendElement(STRONG)
						.text(unitedVerses[0]);
				}

				// append in the scripture which verses was united ex. 1:22-23
				String unitedText = " " 
					+ markupInicial 
					+ chapterNumber 
					+ ":" 
					+ unitedVerses[0] 
					+ "-" 
					+ unitedVerses[1] 
					+ " " + markupFinal;

				Element unitedVersesText = new Element(SPAN)
					.text(unitedText)
					.attr("style", FONT_MEPS)
					.attr("class", "unitedVersesText");
				verse.after(unitedVersesText);

				Element newVerse = new Element(SPAN);
				lastElement = newVerse;

				// add the noexistent verse numbers on html
				for (int i = Integer.parseInt(unitedVerses[0]) + 1; i < Integer.parseInt(unitedVerses[1]) + 1; i++) {

					// if it is not the first verse of the chapter, add the verse number
					if(i > 1){
						newVerse.appendElement(STRONG)
							.text(" " + i);
					}	

					if (see.isEmpty()) {
						newVerse.appendElement(SPAN)
							.text(" —— ");						
					} else {
						String unitedVerseSeeText = " " 
							+ markupInicial 
							+ see 
							+ " " 
							+ chapterNumber 
							+ ":"
							+ unitedVerses[0] 
							+ markupFinal 
							+ " ";
						
						
						newVerse.appendElement(SPAN)
							.text(unitedVerseSeeText)
							.attr("style", FONT_MEPS)
							.attr("class", "unitedVerseSee");						
					}
				}

				/*
				 * if there's no more verse in the chapter, add at the end of the div: parent()
				 * if there no more verse in the chapter, add at the start of the next verse
				 * number: span class=v
				 */
				if (!iterator.hasNext()) { // if there's NO more verse in the chapter
					
					// and add after it
					var verseParent = verse.parent();
					if(verseParent != null){
						Element nextElementAtEnd = verseParent.nextElementSibling();
	
						if (nextElementAtEnd != null 
								&& (
									nextElementAtEnd.className().equalsIgnoreCase("p") // normal paragraph
									|| nextElementAtEnd.className().startsWith("q") // poetic text
								)
						) {							
							nextElementAtEnd.appendChild(newVerse);
						} else {
							// add at the end of the div: parent()
							newVerse.appendTo(verse.parent());													
						}
					}
				} else { // if there's more verses in the chapter,
					// add at the start of the next verse number
					addToNextVerse = newVerse;
					lastIterator = verse;
				}
			}
			// Make sure that each digit is bold.
			makeVerseBold(verse);
			
		}
		return lastElement;
	}

	private void removeUnwantedText(Document document) {
		document.getElementById("book-menu").remove();
		document.getElementById("chapter-menu").remove();
		document.getElementById("toolbar-top").remove();
		document.getElementsByAttributeValue("class", "footer").remove();
		document.select("span[id^=bookmarks]").remove();
		document.select("a").remove();
		document.select("script").remove();
		document.select("span[class=vsp]").remove(); // remove &nbsp
		document.select("div[class=b]").remove();
		document.select("div[class=video-block]").remove();

		// remove book prefix name
		
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
	}

	private String addBrackets(Document document) {
		String chapterNumber = null;
		Element cDrop = document.selectFirst("div.c-drop");
		if (cDrop != null) {
			chapterNumber = cDrop.text();
			cDrop.prependText("{").appendText("} "); // add {}
			cDrop.tagName(SPAN); // change to span
		}
		return chapterNumber;
	}

	private Element handleNoVerseChapter(Document document, int currentChapter, Elements verses, int totalVersesEnum) {
		int lastVerse;
		// create and add verses on the div id content
		Element elementDivClassContent = document.selectFirst("div[id=content]");

		if (verses.isEmpty() && elementDivClassContent != null) {
			lastVerse=totalVersesEnum;
			elementDivClassContent.appendElement(SPAN).text("{" + currentChapter + "} ");
			for (int i = 1; i <= totalVersesEnum; i++) {
				if (i > 1) elementDivClassContent.appendElement(STRONG).text(String.valueOf(i));
				elementDivClassContent.appendElement(SPAN).text(" —— ");

			}
		}
		return elementDivClassContent;
	}

	private Element handleFirstVerseNumber(Document document, Book book) {
		// if the book does not have just one chapter
		int totalChapters = book.getBookName().getNumberOfChapters();
		
		Element spanV = document.selectFirst("span.v");
		Element spanCDrop = document.selectFirst("span[class=c-drop]");
		int verseNumber = 0;

		/*
		 * 2.	For books not containing chapters, 
		 *      add verse number one to the beginning of the first verse, 
		 *      if it has not been included in the pasted text
		 */
		if(spanV != null){
			try {
				verseNumber = Integer.parseInt(spanV.text());	
				if(verseNumber == 1){
					if (totalChapters > 1) {
						spanV.remove(); // delete verse 1
					} else if (totalChapters == 1 && spanCDrop != null) {			
						Element s = new Element(STRONG).text("1");
						spanCDrop.after(s);	
					}
				}				
			} catch (NumberFormatException e){

			} 		
		}	
		return spanCDrop;
	}

	private void addPercentSign(Document document, Book book) {
		// get the book name from site
		BookName bookNameScriptureEarth = book.getBookName();

		// get the book number in ScriptureEarch enum
		int bookNumber = bookNameScriptureEarth.ordinal();


		Element divClassMt = document.selectFirst("div[class=mt]");
		Element spanClassMt = document.selectFirst("span[class=mt]");
		if (divClassMt != null) { // se for o primeiro capítulo
			document.getElementsByTag("body").before("%" + String.format("%02d", bookNumber + 1));
			divClassMt.prependElement(SPAN).text("%");
			spanClassMt.tagName(STRONG);
		}
	}

	private void handleFootnotes(Document document) {
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
	}

	private String changeGlotal(BibleSetup bibleSetup, Document document) {
		String apostrophe = "\u0027"; 		
		String glotal = bibleSetup.getGlotal();
		
		String htmlWithGlotal;
		if (!glotal.isBlank()) {
			htmlWithGlotal = document.getElementsByTag("body").html().replace(apostrophe, glotal);			
		}else {
			htmlWithGlotal = document.getElementsByTag("body").html();
		}
		return htmlWithGlotal;
	}

	private void handlePoeticText(Document document) {
		Elements divsPoetic = document.select("div[class^=q]"); // search for all div with class starting with q
		for (Element divPoetic : divsPoetic) {

			divPoetic.tagName(SPAN);

			// só adicionar BR se o próximo for class=q* (exceto versículos)
			Element nextElement = divPoetic.nextElementSibling();
			if (nextElement != null) {
				if (nextElement.className().startsWith("q") && !nextElement.className().startsWith("v")) {
					divPoetic.appendElement("br");
				}

			}

			Element previousElement = divPoetic.previousElementSibling();
			if (previousElement != null && previousElement.lastElementChild() != null) {

				// verifica se o div anterior começa com class=v, se sim finaliza com <br>
				if (previousElement.lastElementChild().className().equals("v")) {
					previousElement.appendElement("br");
					previousElement.tagName(SPAN);
				}
			}


			/*
			* Place an Equals sign (=) before the first chapter or verse number where poetic text starts.
			*  
			* Note: If a Bible book begins with poetic text, place the Equals sign (=) at the beginning 
			* of the second verse containing poetic text instead. 
			* 
			* If poetic text starts in the middle of a verse, no Equals sign (=) is necessary.
			*/

			if(previousElement != null){
				String previousElementClassName = previousElement.className();
				Element divPoeticFirstElementChild = divPoetic.firstElementChild();
				if(divPoeticFirstElementChild != null 
						&& !previousElementClassName.startsWith("q")
						&& divPoetic.className().startsWith("q-v")
				){
					divPoetic.prependText("=");		

				}
			}

		}
	}


	private void addDollarSign(Document document, Book book, Element spanCDrop) {
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

				addPlusSignAfterHead(divIdD1.parent());
			}


		}
	}

	private void handleExtraOrFewerVerses(Element lastVerse, Book book, Document document, int currentChapter ) {

		Elements verses = document.select("span[class=v]");		
		int totalVersesEnum = book.getBookName().getNumberOfScriptures(currentChapter);

		Element elementDivClassContent = handleNoVerseChapter(document, currentChapter, verses, totalVersesEnum);
		if(lastVerse != null){
			Element lastVerseTemp = lastVerse;
			try {
				int lastVerseNumverInt =Integer.parseInt(lastVerseTemp.text());
	
				if (lastVerseNumverInt > totalVersesEnum) {
					// change the tag of the new verses to sup				
					verses.last().tagName("sup");
				} else if (lastVerseNumverInt < totalVersesEnum) {
					// complete with new verses
					
					if(elementDivClassContent != null) {
						Element div=elementDivClassContent.appendElement("div");
						for (int i = lastVerseNumverInt+1; i <= totalVersesEnum; i++) {					
							div.appendElement(STRONG)
								.text(String.valueOf(i));
							div.appendElement(SPAN)
								.text(" —— ");
		
						}	
					}
				}
				
			} catch (Exception e) {

			}
	
		}
	}

	private void makeVerseBold(Element verse) {
		String verseNumver = verse.text();
		verse.text("");
		verse.appendElement(STRONG)
			.text(verseNumver + " ");

	}

	private void addAtSign(Document document) {
		Elements headers = document.select("div[class=s]");
		for (Element header : headers) {
			Elements children = header.children();
			for (Element child : children) {
				child.prependText("@");
			}

			addPlusSignAfterHead(header);
		}

		Element divClassMt2 = document.selectFirst("div[class=mt2]");
		if (divClassMt2 != null && divClassMt2.firstElementChild() != null) {
			divClassMt2.firstElementChild().prependText("@");

			addPlusSignAfterHead(divClassMt2);
		}
	}
	

	/*
	* Place a Plus sign (+) at the start of a line when body text immediately
	* follows any type of heading
	*/
	private void addPlusSignAfterHead(Element header) {
		Element nextElement = header.nextElementSibling();
		// if is not a header
		if (nextElement != null 
				&& !nextElement.className().equalsIgnoreCase("s") 
				&& !nextElement.className().equalsIgnoreCase("mt2")) 
		{
			Element nextElementChild = nextElement.firstElementChild();
			// if is a chapter number
			if(nextElement.className().startsWith("m") 
					&& nextElement.firstElementChild() != null)
			{
				if(nextElementChild.className().equalsIgnoreCase("c-drop")){
					nextElementChild.prependText("+");
				}							
			// if is a paragraph
			} else if(nextElement.className().equalsIgnoreCase("p") ){
				nextElement.prependText("+");
			}
		}
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
