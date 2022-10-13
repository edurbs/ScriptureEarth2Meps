package com.scriptureearth2meps.model;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.processing.Generated;



public class JsonBook implements Serializable {

	private String name;
	
	private String ref;
	private final static long serialVersionUID = -1373358093592801097L;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public JsonBook() {
	}

	/**
	 *
	 * @param ref
	 * @param name
	 */
	public JsonBook(String name, String ref) {
		this.name = name;
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonBook withName(String name) {
		this.name = name;
		return this;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public JsonBook withRef(String ref) {
		this.ref = ref;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, ref);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonBook other = (JsonBook) obj;
		return Objects.equals(name, other.name) && Objects.equals(ref, other.ref);
	}
	
	

}