package com.bertvanbrakel.android.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.NameValuePair;


public class InputStreamPair implements NameValuePair {

	private final byte[] bytes;
	private final String name;

	public InputStreamPair(final String name, final byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}

	public String getName() {
		return name;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(bytes);
	}

	public String getValue() {
		return null;
	}
}
