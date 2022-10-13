package com.scriptureearth2meps.model;

import java.util.Objects;

public class Chapter {
	
	private Book book;
	


	public Chapter(Book book) {
		
		this.book = book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public Book getBook() {
		return book;
	}

	@Override
	public int hashCode() {
		return Objects.hash(book);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chapter other = (Chapter) obj;
		return Objects.equals(book, other.book);
	}

	


}
