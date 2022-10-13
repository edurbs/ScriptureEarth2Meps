package com.scriptureearth2meps.model;

public enum BookName {
	/*
	 * Constant name - Name in ScriptureEarth format
	 * String - name in Meps format	  
	 * Integer Array - number of scriptures in each chapter
	 */

	T_GEN("01_GEN", new Integer[] {31,25,24,26,32,22,24,22,29,32,32,20,18,24,21,16,27,33,38,18,34,24,20,67,34,35,46,22,35,43,55,32,20,31,29,43,36,30,23,23,57,38,34,34,28,34,31,22,33,26}),
	T_EXO("02_EXO", new Integer[] {22,25,22,31,23,30,25,32,35,29,10,51,22,31,27,36,16,27,25,26,36,31,33,18,40,37,21,43,46,38,18,35,23,35,35,38,29,31,43,38}),
	T_LEV("03_LEV", new Integer[] {17,16,17,35,19,30,38,36,24,20,47,8,59,57,33,34,16,30,37,27,24,33,44,23,55,46,34}),
	T_NUM("04_NUM", new Integer[] {54,34,51,49,31,27,89,26,23,36,35,16,33,45,41,35,28,32,22,29,35,41,30,25,18,65,23,31,39,17,54,42,56,29,34,13}),
	T_DEU("05_DEU", new Integer[] {46,37,29,49,33,25,26,20,29,22,32,32,18,29,23,22,20,22,21,20,23,30,25,22,19,19,26,68,29,20,30,52,29,12}),
	T_JOS("06_JOS", new Integer[] {18,24,17,24,15,27,26,35,27,43,23,24,33,15,63,10,18,28,51,9,45,34,16,33}),
	T_JDG("07_JUD", new Integer[] {36,23,31,24,31,40,25,35,57,18,40,15,25,20,20,31,13,31,30,48,25}),
	T_RUT("08_RUT", new Integer[] {22,23,18,22}),
	T_1SA("09_1SA", new Integer[] {28,36,21,22,12,21,17,22,27,27,15,25,23,52,35,23,58,30,24,42,15,23,29,22,44,25,12,25,11,31,13}),
	T_2SA("10_2SA", new Integer[] {27,32,39,12,25,23,29,18,13,19,27,31,39,33,37,23,29,33,43,26,22,51,39,25}),
	T_1KI("11_1KI", new Integer[] {53,46,28,34,18,38,51,66,28,29,43,33,34,31,34,34,24,46,21,43,29,53}),
	T_2KI("12_2KI", new Integer[] {18,25,27,44,27,33,20,29,37,36,21,21,25,29,38,20,41,37,37,21,26,20,37,20,30}),
	T_1CH("13_1CH", new Integer[] {54,55,24,43,26,81,40,40,44,14,47,40,14,17,29,43,27,17,19,8,30,19,32,31,31,32,34,21,30}),
	T_2CH("14_2CH", new Integer[] {17,18,17,22,14,42,22,18,31,19,23,16,22,15,19,14,19,34,11,37,20,12,21,27,28,23,9,27,36,27,21,33,25,33,27,23}),
	T_EZR("15_EZR", new Integer[] {11,70,13,24,17,22,28,36,15,44}),
	T_NEH("16_NEH", new Integer[] {11,20,32,23,19,19,73,18,38,39,36,47,31}),
	T_EST("17_EST", new Integer[] {22,23,15,17,14,14,10,17,32,3}),
	T_JOB("18_JOB", new Integer[] {22,13,26,21,27,30,21,22,35,22,20,25,28,22,35,22,16,21,29,29,34,30,17,25,6,14,23,28,25,31,40,22,33,37,16,33,24,41,30,24,34,17}),
	T_PSA("19_PSA", new Integer[] {6,12,8,8,12,10,17,9,20,18,7,8,6,7,5,11,15,50,14,9,13,31,6,10,22,12,14,9,11,12,24,11,22,22,28,12,40,22,13,17,13,11,5,26,17,11,9,14,20,23,19,9,6,7,23,13,11,11,17,12,8,12,11,10,13,20,7,35,36,5,24,20,28,23,10,12,20,72,13,19,16,8,18,12,13,17,7,18,52,17,16,15,5,23,11,13,12,9,9,5,8,28,22,35,45,48,43,13,31,7,10,10,9,8,18,19,2,29,176,7,8,9,4,8,5,6,5,6,8,8,3,18,3,3,21,26,9,8,24,13,10,7,12,15,21,10,20,14,9,6}),
	T_PRO("20_PRO", new Integer[] {33,22,35,27,23,35,27,36,18,32,31,28,25,35,33,33,28,24,29,30,31,29,35,34,28,28,27,28,27,33,31}),
	T_ECC("21_ECC", new Integer[] {18,26,22,16,20,12,29,17,18,20,10,14}),
	T_SNG("22_SON", new Integer[] {17,17,11,16,16,13,13,14}),
	T_ISA("23_ISA", new Integer[] {31,22,26,6,30,13,25,22,21,34,16,6,22,32,9,14,14,7,25,6,17,25,18,23,12,21,13,29,24,33,9,20,24,17,10,22,38,22,8,31,29,25,28,28,25,13,15,22,26,11,23,15,12,17,13,12,21,14,21,22,11,12,19,12,25,24}),
	T_JER("24_JER", new Integer[] {19,37,25,31,31,30,34,22,26,25,23,17,27,22,21,21,27,23,15,18,14,30,40,10,38,24,22,17,32,24,40,44,26,22,19,32,21,28,18,16,18,22,13,30,5,28,7,47,39,46,64,34}),
	T_LAM("25_LAM", new Integer[] {22,22,66,22,22}),
	T_EZK("26_EZE", new Integer[] {28,10,27,17,17,14,27,18,11,22,25,28,23,23,8,63,24,32,14,49,32,31,49,27,17,21,36,26,21,26,18,32,33,31,15,38,28,23,29,49,26,20,27,31,25,24,23,35}),
	T_DAN("27_DAN", new Integer[] {21,49,30,37,31,28,28,27,27,21,45,13}),
	T_HOS("28_HOS", new Integer[] {11,23,5,19,15,11,16,14,17,15,12,14,16,9}),
	T_JOL("29_JOE", new Integer[] {20,32,21}),
	T_AMO("30_AMO", new Integer[] {15,16,15,13,27,14,17,14,15}),
	T_OBA("31_OBA", new Integer[] {21}),
	T_JON("32_JON", new Integer[] {17,10,10,11}),
	T_MIC("33_MIC", new Integer[] {16,13,12,13,15,16,20}),
	T_NAM("34_NAH", new Integer[] {15,13,19}),
	T_HAB("35_HAB", new Integer[] {17,20,19}),
	T_ZEP("36_ZEP", new Integer[] {18,15,20}),
	T_HAG("37_HAG", new Integer[] {15,23}),
	T_ZEC("38_ZEC", new Integer[] {21,13,10,14,11,15,14,23,17,12,17,14,9,21}),
	T_MAL("39_MAL", new Integer[] {14,17,18,6}),
	T_MAT("40_MAT", new Integer[] {25,23,17,25,48,34,29,34,38,42,30,50,58,36,39,28,27,35,30,34,46,46,39,51,46,75,66,20}),
	T_MRK("41_MAR", new Integer[] {45,28,35,41,43,56,37,38,50,52,33,44,37,72,47,20}),
	T_LUK("42_LUK", new Integer[] {80,52,38,44,39,49,50,56,62,42,54,59,35,35,32,31,37,43,48,47,38,71,56,53}),
	T_JHN("43_JOH", new Integer[] {51,25,36,54,47,71,53,59,41,42,57,50,38,31,27,33,26,40,42,31,25}),
	T_ACT("44_ACT", new Integer[] {26,47,26,37,42,15,60,40,43,48,30,25,52,28,41,40,34,28,41,38,40,30,35,27,27,32,44,31}),
	T_ROM("45_ROM", new Integer[] {32,29,31,25,21,23,25,39,33,21,36,21,14,23,33,27}),
	T_1CO("46_1CO", new Integer[] {31,16,23,21,13,20,40,13,27,33,34,31,13,40,58,24}),
	T_2CO("47_2CO", new Integer[] {24,17,18,18,21,18,16,24,15,18,33,21,14}),
	T_GAL("48_GAL", new Integer[] {24,21,29,31,26,18}),
	T_EPH("49_EPH", new Integer[] {23,22,21,32,33,24}),
	T_PHP("50_PHI", new Integer[] {30,30,21,23}),
	T_COL("51_COL", new Integer[] {29,23,25,18}),
	T_1TH("52_1TH", new Integer[] {10,20,13,18,28}),
	T_2TH("53_2TH", new Integer[] {12,17,18}),
	T_1TI("54_1TI", new Integer[] {20,15,16,16,25,21}),
	T_2TI("55_2TI", new Integer[] {18,26,17,22}),
	T_TIT("56_TIT", new Integer[] {16,15,15}),
	T_PHM("57_PHM", new Integer[] {25}),
	T_HEB("58_HEB", new Integer[] {14,18,19,16,14,20,28,13,28,39,40,29,25}),
	T_JAS("59_JAM", new Integer[] {27,26,18,17,20}),
	T_1PE("60_1PE", new Integer[] {25,25,22,19,14}),
	T_2PE("61_2PE", new Integer[] {21,22,18}),
	T_1JN("62_1JO", new Integer[] {10,29,24,21,21}),
	T_2JN("63_2JO", new Integer[] {13}),
	T_3JN("64_3JO", new Integer[] {15}),
	T_JUD("65_JUD", new Integer[] {25}),
	T_REV("66_REV", new Integer[] {20,29,22,11,14,17,17,13,21,11,19,17,18,20,8,21,18,24,21,15,27,21}),
	ANY(null, new Integer[] {null});

	private final String meps;
	private final Integer[] scriptures;
	
	private BookName(String meps, Integer[] scriptures) {
		this.meps = meps;
		this.scriptures = scriptures;		
	}

	public String getMepsFormat(){
		return meps;
	}
	
	public int getNumberOfChapters() {
		return scriptures.length;
	}
	
	public int getNumberOfScriptures(int chapter) {
		return scriptures[chapter-1];
		
	}
	
	public int getBookOrdinal(String bookScriptureEarth) {

		return this.valueOf(bookScriptureEarth.toUpperCase()).ordinal();
	}
	
	
	
	

}