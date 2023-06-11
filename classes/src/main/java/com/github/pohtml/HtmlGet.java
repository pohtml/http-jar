package com.github.pohtml;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HtmlGet<T extends Get2> extends HtmlMethod<T> {
	
	private static final long serialVersionUID = 1L;

	public HtmlGet(long lastModified) {
		super(lastModified);
	}

	@Override
	protected void doModel(String uri, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Json json = new Json();
		request.setAttribute("com.github.pohtml.json", json);
		T method = get();
		method.servlet = this;
		method.request = request;
		method.run(json); 
		byte[] encoded = Base64.getEncoder().encode(json.toString().getBytes(UTF_8));
		Cookie cookie = new Cookie("pohtml", new String(encoded));
		cookie.setPath(uri);
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		ServletContext context = getServletContext();
		String path = context.getContextPath();
		uri = uri.substring(path.length());
		RequestDispatcher dispatcher = context.getRequestDispatcher(uri + ".html");
		dispatcher.forward(request, response);	
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doIt(request, response);
	}

}
