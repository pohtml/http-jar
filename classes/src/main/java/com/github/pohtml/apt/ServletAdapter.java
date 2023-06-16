package com.github.pohtml.apt;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pohtml.annotations.Get;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class ServletAdapter<T, I> extends HttpServlet implements Supplier<T> {

	static final String[] HTML = {"<!DOCTYPE html><html><head><meta charset=utf-8><script id=model type=application/ld+json>", "</script><script", "</script></head></html>"};
	
	private static final long serialVersionUID = 1L;
	
	private boolean cookieModel = false;
	
	private final Class<I> input;
	private final long lastModified;
	private final Action action;
	private final String html;

	private ServletContext context;
	private String uri;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		context = getServletContext();
		String path = context.getContextPath(); 
		uri = path + (action.uri.isEmpty()? computeUri() : action.uri);
		config.getInitParameter("com.github.pohtml");
	}
	
	private String computeUri() {
		return getClass().getAnnotation(WebServlet.class).value()[0]; // TODO Check
	}
	
	protected ServletAdapter(long lastModified, Class<T> annotated, Action annotation, Class<I> input) {
		this.lastModified = lastModified;
		this.input = input;
		this.action = annotation;			
		String[] patterns = getClass().getAnnotation(WebServlet.class).value();
		this.html = patterns.length == 1? patterns[0] + ".html" : patterns[1];
	}
	
	protected ServletAdapter(long lastModified, Class<T> annotated, Class<Get> annotation, Class<I> input) {
		this(lastModified, annotated, new Action(annotated.getDeclaredAnnotation(annotation)), input);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (cookieModel) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(Constants.MODEL)) {
						cookie.setMaxAge(0);
						cookie.setPath(uri);
						response.addCookie(cookie);
						response.setStatus(500);
						new IllegalStateException().printStackTrace();
						return;
					}
				}	
			}	
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		T t = get();
		if (t instanceof Consumer) {
			I consumable;
			if (input == Void.class) {
				consumable = null;
			} else if (input == HttpServletRequest.class) {
				@SuppressWarnings("unchecked")
				I i = (I)request;
				consumable = i;
			} else {
				JsonObject jsonObject = new JsonObject();
				Enumeration<String> parameters = request.getParameterNames();
				while(parameters.hasMoreElements()) {
					String key = parameters.nextElement();
					String[] values = request.getParameterValues(key);
					if (values == null) {
						continue;
					} else if (values.length == 1) {
						jsonObject.addProperty(key, values[0]);
					} else {
						JsonArray array = new JsonArray(values.length);
						for (String value : values) {
							array.add(value);	
						}
						jsonObject.add(key, array);
					}
				}
				consumable = gson.fromJson(jsonObject, input);	
			}
			@SuppressWarnings("unchecked")
			Consumer<I> consumer = ((Consumer<I>)t); 
			consumer.accept(consumable);
		}
		JsonElement representation = gson.toJsonTree(t);
		if (t instanceof Supplier) {
			Object gotten = ((Supplier<?>)t).get();
			representation.getAsJsonObject().add("com.github.pohtml.form", toJsonObject(gson, gotten));
		}
		if (request.getHeader("accept").equals("application/json")) {
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			JsonObject result = toJsonObject(gson, representation);
			String json = gson.toJson(result);
			response.getWriter().print(json);
		} else if (cookieModel) {
			JsonObject result = toJsonObject(gson, representation);
			String json = gson.toJson(result);
			byte[] encoded = Base64.getEncoder().encode(json.getBytes(UTF_8));
			Cookie cookie = new Cookie(Constants.MODEL, new String(encoded));
			cookie.setPath(uri);
			cookie.setMaxAge(-1);
			response.addCookie(cookie);
			RequestDispatcher dispatcher = context.getRequestDispatcher(uri + ".html");
			dispatcher.include(request, response);
		} else {
			representation.getAsJsonObject().addProperty("com.github.pohtml.view", request.getRequestURI() + ".html");
			JsonObject result = toJsonObject(gson, representation);
			String json = gson.toJson(result);
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			PrintWriter writer = response.getWriter();
			writer.append(HTML[0]).append(json.toString()).append(HTML[1]);
			/*<TODO task='Include, when configured, an src attribute instead of this embedded resource'>*/
			writer.append('>');
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transfer(getClass().getClassLoader().getResourceAsStream("/com/github/pohtml/common.js"), os);
			/*</TODO>*/
			writer.write(new String(os.toByteArray(), StandardCharsets.UTF_8));
			writer.append(HTML[2]);
		}
	}
	
	private JsonObject toJsonObject(Gson gson, Object source) {
		return gson.toJsonTree(source).getAsJsonObject();
	}
	
	public static void transfer(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read = in.read(buffer);
		while (read != -1) {
			out.write(buffer, 0, read);
			read = in.read(buffer);
		}
	}
	

}