package com.scriptureearth2meps.model;

import java.util.Objects;

public class BiblePage {
	private String url = null;

	public BiblePage(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BiblePage other = (BiblePage) obj;
		return Objects.equals(url, other.url);
	}
	
	

}
