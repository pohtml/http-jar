package com.github.pohtml;

import javax.servlet.http.HttpServlet;

public abstract class Method {
	
	HttpServlet servlet;
	
	protected final HttpServlet getHttpServlet() {
		return servlet;
	}
	
}