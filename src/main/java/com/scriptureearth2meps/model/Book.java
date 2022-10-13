package com.scriptureearth2meps.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Book {

	private BookName bookName;
	private int TotalChapters;

	private List<Chapter> chapters = new ArrayList<>();
	private List<Footnote> footnotes = new ArrayList<>();
	private StringBuilder html = new StringBuilder();
	private String url;

	

	public Book(BookName bookName, String url) {
		this.bookName = bookName;
		this.url = url;		
	}
	
	public void setTotalChapters(int totalChapters) {
		TotalChapters = totalChapters;
	}
	
	public int getTotalChapters() {
		return TotalChapters;
	}

	public String getNameMeps() {
		return bookName.getMepsFormat();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setHtml(StringBuilder html) {
		this.html = html;
	}

	public StringBuilder getHtml() {
		return html;
	}

	public void addHtml(String html) {
		this.html.append(html);
	}

	public void setFootnotes(List<Footnote> footnotes) {
		this.footnotes = footnotes;
	}

	public List<Footnote> getFootnotes() {
		return footnotes;
	}
	
	public void addFootnote(Footnote foonote) {
		this.footnotes.add(foonote);
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

	public BookName getBookName() {
		return bookName;
	}

	public void setBookName(BookName name) {
		this.bookName = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bookName, chapters, footnotes, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		return bookName == other.bookName && Objects.equals(chapters, other.chapters)
				&& Objects.equals(footnotes, other.footnotes) && Objects.equals(url, other.url);
	}
	
	

}
