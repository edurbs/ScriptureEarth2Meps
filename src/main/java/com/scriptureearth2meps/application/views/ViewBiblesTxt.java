package com.scriptureearth2meps.application.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Scanner;

import com.scriptureearth2meps.control.BibleSetup;
import com.scriptureearth2meps.model.Bible;
import com.scriptureearth2meps.model.Language;

public class ViewBiblesTxt {

	BibleSetup bibleSetup;

	public ViewBiblesTxt() {
		try {
			this.bibleSetup = new BibleSetup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		showLanguageList();
	}

	public void showLanguageList() {

		System.out.println("Language codes:");
		for (Language language : bibleSetup.getLanguageList()) {
			System.out.println(language.getLanguage());
		}
		askLanguageCode();

	}

	private void askLanguageCode() {
		// ask user for the language of the bible

		System.out.println("Enter language Code:");
		try (Scanner scanner = new Scanner(System.in)) {
			String languageCode = scanner.nextLine();
			bibleSetup.setLanguageCode(languageCode);
			processLanguageCode();
			//scanner.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void processLanguageCode() throws IOException {

		if (!bibleSetup.verifyWebBible()) {
			System.out.println("This language does not have the Bible web page ");
			// showLanguageList();
		} else {
			if (bibleSetup.hasMoreBibles()) {
				// System.out.println("This language have more than one Bible");
				showBibleList();
			} else {
				askWordSee();
			}
		}

	}

	private void showBibleList() {
		System.out.println("Bible codes:");
		List<Bible> bibleList = bibleSetup.getBibleList();
		for (Bible bibleCode : bibleList) {
			System.out.println(bibleCode.getBible());
		}
		askForBibleCode();

	}

	public void askForBibleCode() {

		System.out.println("Enter Bible Code:");
		try (Scanner scanner = new Scanner(System.in)) {
			String bibleCode = scanner.nextLine();
			bibleSetup.setBibleCode(bibleCode);
			askWordSee();
			//scanner.close();

		} catch (Exception e) {
			e.printStackTrace();

			showBibleList();
		}
	}


	
	private void askWordSee() {
		System.out.println("Enter the translation of the word 'See':\n");
		try (Scanner scanner = new Scanner(System.in)) {			
			String see = scanner.nextLine();
			bibleSetup.setWordSee(see);
			scanner.close();
			processBible();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	private void processBible() throws MalformedURLException, IOException {

		try {
			bibleSetup.process(null, null);
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

}
