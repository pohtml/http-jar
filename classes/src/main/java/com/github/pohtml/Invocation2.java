package com.github.pohtml;

import javax.servlet.http.HttpServlet;

public abstract class Invocation2 {
	
	HttpServlet servlet;
	
	protected final HttpServlet getHttpServlet() {
		return servlet;
	}
	
}