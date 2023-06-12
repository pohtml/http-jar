package com.github.pohtml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HtmlMethod<T extends Get2> extends HttpServlet implements Supplier<T> {
	
	private static final long serialVersionUID = 1L;

	private final long lastModified;
	
	public HtmlMethod(long lastModified) {
		this.lastModified = lastModified;
	}

	protected abstract void servlet(ServletContext context,String uri, HttpServletRequest request, HttpServletResponse response) throws Exception;

	final void doIt(HttpServletRequest request, HttpServletResponse response) {
		try {
			String[] uris = getClass().getAnnotation(WebServlet.class).value();
			String uri;
			ServletContext context = getServletContext();
			if (Static.ORIGIN == null) {
				if (Static.CONTEXT == null) {
					if (uris.length > 1) {
						throw new IllegalStateException("Unexpected URI for cross server/context redirection");
					}
					uri = uris[0] + ".html";
				} else {
					uri = uris.length < 2? uris[0] + ".html" : uris[1];
					context = context.getContext(Static.CONTEXT);
				}
			} else {
				if (uris.length < 2) {
					throw new IllegalStateException("Missing URI for static files server redirection");
				}
				uri = uris[1];
			}
			if (uri.endsWith(".html")) {
				html(uri, request, response);
			} else {
				servlet(context, uri, request, response);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	void html(String uri, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		URL url = new URL(Static.ORIGIN + uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if (!Static.REQUEST_HEADERS.contains(headerName.toLowerCase())) {
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
			resp.addHeader("cache-control", "max-age=0, must-revalidate");	
		}
		for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			if (key == null || !Static.RESPONSE_HEADERS.contains(key.toLowerCase())) {
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
			if (responseCode == 200) {
				String start = new String(Arrays.copyOfRange(buffer, 0, 256));
				String head = "<head>";
				int index = start.indexOf(head);
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