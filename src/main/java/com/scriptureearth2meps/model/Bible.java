package com.scriptureearth2meps.model;

import java.util.Objects;

public class Bible {

	private String bible = null;

	public Bible(String bible) {
		this.bible = bible;
	}

	public String getBible() {
		return bible;
	}

	public void setBible(String bible) {
		this.bible = bible;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bible);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bible other = (Bible) obj;
		return Objects.equals(bible, other.bible);
	}
	
	

}
