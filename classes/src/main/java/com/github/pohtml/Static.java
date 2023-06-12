package com.github.pohtml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Static extends HttpServlet {

	public static final String ORIGIN = System.getProperty("com.github.pohtml.base");
	public static final String QUALIFIER = getQualifier();
	public static final String CONTEXT = getContext();
	public static final Set<String> REQUEST_HEADERS = getRequestHeaders();
	public static final Set<String> RESPONSE_HEADERS = getResponseHeaders();
	
	private static final long serialVersionUID = 1L;
	
	private static Set<String> getPropertyAsSet(String key, String... defaultSet) {
		String value = System.getProperty(key);
		if (value == null && defaultSet == null) {
			throw new IllegalStateException("Missing system property: " + key);
		}
		return value == null? headers(defaultSet) : headers(value);
	}
	
	private static Set<String> headers(String property) {
		return headers(property.split("\\s*,\\s*"));
	}
	
	private static Set<String> headers(String... elements) {
		return headers(Arrays.asList(elements));
	}
	
	private static Set<String> headers(Collection<String> elements) {
		return elements.stream().map(String::toLowerCase).collect(Collectors.toSet());
	}
	
	private static Set<String> getRequestHeaders() {
		Set<String> headers = getPropertyAsSet("com.github.pohtml.headers.request");
		headers.add("If-Modified-Since".toLowerCase());
		return Collections.unmodifiableSet(headers);
	}
	
	private static Set<String> getResponseHeaders() {
		Set<String> headers = getPropertyAsSet("com.github.pohtml.headers.response");
		headers.remove("Cache-Control".toLowerCase());
		headers.add("Last-Modified".toLowerCase());
		headers.add("Content-Type".toLowerCase());
		headers.add("Content-Length".toLowerCase());
		headers.add("Accept-Ranges".toLowerCase());
		return Collections.unmodifiableSet(headers);
	}
	
	private static String getQualifier() {
		String property = System.getProperty("com.github.pohtml.qualifier");
		return property == null? "pohtml": property;
	}
	
	private static String getContext() {
		String property = System.getProperty("com.github.pohtml.context");
		return property == null? "": property;
	}
	
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.addHeader("Cache-Control", "max-age=31536000");
		resp.setContentType("application/javascript; charset=utf-8");
		String path = "/com/github/pohtml/common.js";
		ClassLoader resources = getClass().getClassLoader(); 
		try (InputStream is = resources.getResourceAsStream(path); OutputStream os = resp.getOutputStream()) {
			byte[] buffer = new byte[445];
			int read = is.read(buffer);
			while (read != -1) {
				os.write(buffer, 0, read);
				read = is.read(buffer);
			}
		}
	}

}