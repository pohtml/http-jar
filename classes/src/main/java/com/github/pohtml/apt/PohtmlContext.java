package com.github.pohtml.apt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class PohtmlContext extends HttpServlet {

	private static final String DOMAIN = "com.github.pohtml.";
	public static final String ORIGIN = System.getProperty(DOMAIN + "origin");
	public static final String CONTEXT = getContext();
	public static final Set<String> REQUEST_HEADERS = getRequestHeaders();
	public static final Set<String> RESPONSE_HEADERS = getResponseHeaders();

	private final String buildTimestamp;
	
	protected PohtmlContext(String build) {
		this.buildTimestamp = build;
	}
	
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
	
	private static String getContext() {
		String property = System.getProperty(DOMAIN + "context");
		return property == null? "" : property;
	}
	
	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.getWriter().write(buildTimestamp);
	}

}