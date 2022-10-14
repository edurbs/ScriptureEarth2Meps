package com.scriptureearth2meps.report;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;

import com.scriptureearth2meps.model.Book;

public class PrintResult   {
	
	private Book book;
	private String format;
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Print the result in the select format
	 *
	 * @throws Exception 
	 */

	public String html(Book book) throws Exception {
		
		PrintWriter file;
		try {
			String fileName="sbi_"+book.getBookName().getMepsFormat()+".html";
			file = new PrintWriter(fileName);
			
			file.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta></head>"
					+book.getHtml().toString()
					+"</html>"
					);
			file.flush();
			file.close();
			return fileName;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	
}
