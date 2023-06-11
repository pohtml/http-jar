package com.github.pohtml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Getlet extends Method<Getlet, Get> {

	private static final long serialVersionUID = 1L;

	protected Getlet() {
		super("get");
	}
	
 	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
		doIt(request, response);
	}
 	
 	@Override
 	public abstract Get call() throws Exception;
	
}