package com.github.pohtml;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Postlet extends Method<Postlet, Post> {

	public Postlet() {
		super("post");
	}
	
	private static final long serialVersionUID = 1L;
	
	String src;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String key = Constants.BASE + "post.js-path"; 
		src = System.getProperty(key);
		if (src == null) {
			src = getServletContext().getInitParameter(key);
		}
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req, resp);	
	}
	
	@Override
	public abstract Post call();
	
}