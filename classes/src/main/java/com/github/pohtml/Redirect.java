package com.github.pohtml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Redirect extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String base;
	private String contextPrefix;
	private String context;
	private String maxAge;
	private Set<String> requestHeaders;
	private Set<String> responseHeaders;
	
	@Override
	public void init(ServletConfig properties) throws ServletException {
		super.init(properties);
		base = getPropertyAsString(properties, "com.softalks.pohtml.base", null);
		contextPrefix = getPropertyAsString(properties, "com.softalks.pohtml.context.prefix", "");
		if (!contextPrefix.isEmpty() && contextPrefix.charAt(0) != '/') {
			contextPrefix = '/' + contextPrefix;	
		}
		context = getPropertyAsString(properties, "com.softalks.pohtml.context", "");
		if (!context.isEmpty() && context.charAt(0) != '/') {
			context = '/' + context;	
		}
		maxAge = getPropertyAsString(properties, "com.softalks.pohtml.max-age", "0");
		requestHeaders = getPropertyAsSet("com.softalks.pohtml.headers.request");
		requestHeaders.add("If-Modified-Since".toLowerCase());
		responseHeaders = getPropertyAsSet("com.softalks.pohtml.headers.response");
		responseHeaders.remove("Cache-Control".toLowerCase());
		responseHeaders.add("Last-Modified".toLowerCase());
		responseHeaders.add("Content-Type".toLowerCase());
		responseHeaders.add("Content-Length".toLowerCase());
		responseHeaders.add("Accept-Ranges".toLowerCase());
	}
	
	private String getPropertyAsString(ServletConfig config, String key, String defaultValue) {
		String value = config.getInitParameter(key);
		if (value == null) {
			value = getServletContext().getInitParameter(key);
			if (value == null) {
				value = System.getProperty(key);
				if (value == null && defaultValue == null) {
					throw new IllegalStateException("Missing system/context/servlet configuration property: " + key);
				} else if (value == null && defaultValue != null) {
					return defaultValue;
				}
			}
		}
		return value;
	}

	
	private Set<String> getPropertyAsSet(String key, String... defaultSet) {
		String value = System.getProperty(key);
		if (value == null) {
			value = getServletContext().getInitParameter(key);
			if (value == null) {
				value = getServletConfig().getInitParameter(key);
				if (value == null && defaultSet == null) {
					throw new IllegalStateException("Missing system/context/servlet configuration property: " + key);
				} else if (value == null && defaultSet != null) {
					return headers(defaultSet);
				}
			}
		}
		return headers(key);
	}
	
	private Set<String> headers(Collection<String> elements) {
		return elements.stream().map(String::toLowerCase).collect(Collectors.toSet());
	}
	
	private Set<String> headers(String... elements) {
		return headers(Arrays.asList(elements));
	}
	
	private Set<String> headers(String property) {
		return headers(property.split("\\s*,\\s*"));
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI();
		int index = uri.lastIndexOf('.');
		String extension = null;
		if (index != -1) {
			extension = uri.substring(index);
		}
		if (extension != null && !extension.equals("html")) {
			uri = uri.substring(0, index + 1) + "html";
		}
		index = getServletContext().getContextPath().length();
		URL url = new URL(base + contextPrefix + context + uri.substring(index));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if (!requestHeaders.contains(headerName.toLowerCase())) {
				continue;
			}
			Enumeration<String> headerValues = req.getHeaders(headerName);
			while (headerValues.hasMoreElements()) {
				String headerValue = headerValues.nextElement();
				connection.setRequestProperty(headerName, headerValue);
			}
		}
		int responseCode = connection.getResponseCode();
		resp.setStatus(responseCode);
		if (responseCode == 200) {
			resp.addHeader("cache-control", "max-age=" + maxAge + ", must-revalidate");	
		}
		for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			if (key == null || !responseHeaders.contains(key.toLowerCase())) {
				continue;
			}
			for (String value : entry.getValue()) {
				resp.addHeader(key, value);
			}
		}
		try (InputStream reader = getInputStream(connection); OutputStream writer = resp.getOutputStream()) {
			int read = reader.read();
			if (read == -1) {
				return;
			}
			byte[] buffer = new byte[1024];
			buffer[0] = (byte) read;
			read = reader.read(buffer, 1, buffer.length - 1) + 1;
			if (responseCode == 200 && uri.endsWith(".html")) {
				String start = new String(Arrays.copyOfRange(buffer, 0, 256));
				String head = "<head>";
				index = start.indexOf(head);
				if (index == -1) {
					index = start.indexOf("<HEAD>");
				}
				byte[] base = ("<base href='" + getHtmlBase(url.toString()) + "/'>").getBytes();
				if (index == -1) {
					writer.write(base);
				} else {
					index += head.length();
					writer.write(buffer, 0, index);
					writer.write(base);
					writer.write(buffer, index, read - index);
				}
			} else {
				writer.write(buffer, 0, read);
			}
			read = reader.read(buffer);
			while (read != -1) {
				writer.write(buffer, 0, read);
				read = reader.read(buffer);
			}
		}

	}

	private InputStream getInputStream(HttpURLConnection connection) {
		try {
			return connection.getInputStream();
		} catch (IOException e) {
			return connection.getErrorStream();
		}
	}

	private String getHtmlBase(String uri) {
		return uri.substring(0, uri.lastIndexOf("/"));
	}

}