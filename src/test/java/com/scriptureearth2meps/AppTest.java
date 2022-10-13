package com.scriptureearth2meps;

import org.junit.jupiter.api.Test;

import com.scriptureearth2meps.control.BibleSetup;
import com.scriptureearth2meps.model.BookName;


/**
 * Unit test for simple App.
 */
public class AppTest{

	@Test
	public void testEnum() {
		
		System.out.println(BookName.T_GEN.toString());
		System.out.println(BookName.T_GEN.getNumberOfChapters());
		System.out.println(BookName.T_GEN.getNumberOfScriptures(2));
		System.out.println(BookName.T_GEN.getBookOrdinal("T_MAT"));
		
		System.out.println(BookName.values()[63].toString());
		
	}
	
	@Test
	public void testParse() {
		
		try {
			BibleSetup bibleSetup = new BibleSetup();
			bibleSetup.setLanguageCode("kgp");
			bibleSetup.setBibleCode("kgp");			
			bibleSetup.setWordSee("ꞌMadöꞌö");
			bibleSetup.setGlotal("A078");
			bibleSetup.process(null, null);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
