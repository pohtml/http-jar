package com.github.pohtml.apt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class FilterAdapter implements Filter {
	
	private class Cookies {
		Long timestamp;
		int redirects;
		Cookies(HttpServletRequest request) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equalsIgnoreCase(Constants.MODEL)) {
						timestamp = Long.parseLong(cookie.getValue());
					} else if (cookie.getName().equalsIgnoreCase(Constants.REDIRECTS)) {
						redirects = Integer.parseInt(cookie.getValue());
					}
				}		
			}		
		}
	}
	
	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
	}
	
	public final void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		Cookies cookies = new Cookies(request);
		if (cookies.timestamp == null && cookies.redirects == 0) {
			redirect(request, response, cookies);
		} else {
			setRedirects(response, 0);
			chain.doFilter(request, response);
		}
	}
	
	protected abstract String getModelUri();
	
	private void setRedirects(HttpServletResponse response, int value) {
		Cookie redirector = new Cookie(Constants.REDIRECTS, String.valueOf(value));
		redirector.setMaxAge(-1);
		redirector.setPath(getModelUri());
		response.addCookie(redirector);
	}
	
	private void redirect(HttpServletRequest request, HttpServletResponse response, Cookies cookies) throws IOException {
		setRedirects(response, cookies.redirects + 1);
		response.sendRedirect(getModelUri());
	}
	
}