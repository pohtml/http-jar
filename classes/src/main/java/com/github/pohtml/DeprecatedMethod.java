package com.github.pohtml;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pohtml.json.Document;

public abstract class DeprecatedMethod<M extends DeprecatedMethod<M, I>, I extends DeprecatedInvocation<M, I>> extends HttpServlet implements Callable<I> {

	private final String method;
	String methodExtensionSuffix;
	String extension;
	M servlet;
	
	@SuppressWarnings("unchecked")
	protected DeprecatedMethod(String method) {
		servlet = (M)this;
		this.method = method;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String key = Constants.EXTENSION_SUFFIX + method; 
		methodExtensionSuffix = getServletContext().getInitParameter(key);
		if (methodExtensionSuffix == null) {
			methodExtensionSuffix = System.getProperty(key);
		}
		if (methodExtensionSuffix == null) {
			methodExtensionSuffix = "";
		}
		key = Constants.EXTENSION; 
		extension = getServletContext().getInitParameter(key);
		if (extension == null) {
			extension = System.getProperty(key);
		}
		if (extension == null) {
			extension = "html";
		}
	}
	
	final void doIt(HttpServletRequest request, HttpServletResponse response) {
		try {
			Document document = new Document();
			document.root(json -> {
				request.setAttribute("softalks.com/json", json);
				I invocation = call();
				invocation.init(servlet, request, response, json);
				invocation.finish();
			});
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static final long serialVersionUID = 1L;

	@Override
	protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
	}

	@Override
	protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doHead(req, resp);
	}

	@Override
	protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doOptions(req, resp);
	}

	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	@Override
	protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doTrace(req, resp);
	}
 
}