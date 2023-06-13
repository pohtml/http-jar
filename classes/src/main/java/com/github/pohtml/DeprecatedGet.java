package com.github.pohtml;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

public abstract class DeprecatedGet extends DeprecatedInvocation<Getlet, DeprecatedGet> {

	protected class View {
		String origin;
		public String context;
		String contextRelativeUri;
		String extension;
	}
	
	protected DeprecatedGet() {
		this(view -> {
			
		});
	}
	
	protected DeprecatedGet(Consumer<View> view) {
		
	}
	
	protected abstract void init(Json response) throws Exception;
	
	@Override
	protected void finish() throws ServletException, IOException {
		byte[] encoded = Base64.getEncoder().encode(json.toString().getBytes(UTF_8));
		Cookie cookie = new Cookie("json", new String(encoded));
		cookie.setPath(domainRelativeUri);
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		servlet.getServletContext().getRequestDispatcher(contextRelativeUri + getExtension()).forward(request, response);
	}
	
}