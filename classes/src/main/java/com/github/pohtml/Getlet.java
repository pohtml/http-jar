package com.github.pohtml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Getlet extends DeprecatedMethod<Getlet, DeprecatedGet> {

	private static final long serialVersionUID = 1L;

	protected Getlet() {
		super("get");
	}
	
 	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
		doIt(request, response);
	}
 	
 	@Override
 	public abstract DeprecatedGet call() throws Exception;
	
}