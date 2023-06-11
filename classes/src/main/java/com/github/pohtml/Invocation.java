package com.github.pohtml;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Invocation<M extends Method<M, I>, I extends Invocation<M, I>> {
	
	HttpServletRequest request;
	HttpServletResponse response;
	Json json;
	Object model;
	String extension;
	M servlet;
	String domainRelativeUri;
	ServletContext context;
	String contextRelativeUri;
	String origin;
	
	String qualifier = "";
	
	protected final void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
	void init(M servlet, HttpServletRequest request, HttpServletResponse response, Json json) throws Exception {
		this.servlet = servlet;
		this.request = request;
		this.response = response;
		this.json = json;
		domainRelativeUri = request.getRequestURI();
		context = servlet.getServletContext();
		contextRelativeUri = domainRelativeUri.substring(context.getContextPath().length());
	}
	
	protected HttpServletRequest getRequest() {
		return request;
	}
	
	protected ServletContext getServletContext() {
		return context;
	}
	
	public static String getOrigin(HttpServletRequest request) {
		String scheme = request.getScheme();
		StringBuilder builder = new StringBuilder(scheme).append("//").append(request.getServerName());
		int port = request.getServerPort();
		if (scheme.equals("http") && port != 80 || scheme.equals("https") && port != 443) {
			builder.append(':').append(port);
		}
		return builder.toString();
	}
	
	protected abstract void finish() throws ServletException, IOException;

	protected String getHtmlExtensionQualifier() {
		return "";
	}
	
	protected String getHtmlExtension() {
		return servlet.extension;
	}
	
	String f(String string) {
		return f(string, '/');
	}
	
	String f(String string, char start) {
		return string.charAt(0) == start? string : start + string;
	}
	
	String getExtension() {
		return f(getHtmlExtension() + servlet.methodExtensionSuffix + getHtmlExtensionQualifier(), '.'); 
	}
	
}