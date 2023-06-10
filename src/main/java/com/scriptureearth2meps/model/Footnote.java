package com.scriptureearth2meps.model;

import java.util.Objects;

public class Footnote {

	private String key;
	private String note;

	private int chapter;
	private int verse;



	public Footnote(String key, String note, int chapter, int verse) {
		this.key = key;
		this.note = note;
		this.chapter = chapter;
		this.verse = verse;
	}

	@Override
	public String toString() {
		return "Footnote [getKey()=" + getKey() + ", getNote()=" + getNote() + ", getChapter()=" + getChapter()
				+ ", getVerse()=" + getVerse() + "]";
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getNote() {
		return note;
	}

	public String getText(){
		return this.getNote();
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setText(String text){
		this.setNote(text);
	}
	
	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public int getVerse() {
		return verse;
	}

	public void setVerse(int verse) {
		this.verse = verse;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chapter, key, note, verse);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Footnote other = (Footnote) obj;
		return chapter == other.chapter && Objects.equals(key, other.key) && Objects.equals(note, other.note)
				&& verse == other.verse;
	}
	
	

}
