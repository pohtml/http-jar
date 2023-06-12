package com.github.pohtml;

import javax.servlet.http.HttpServletRequest;

public abstract class Get2 extends Invocation2 {

	HttpServletRequest request;
	
	protected abstract void run(Json response) throws Exception;

	protected final HttpServletRequest getRequest() {
		return request;
	}
		
}