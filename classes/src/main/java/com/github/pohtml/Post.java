package com.github.pohtml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Post extends Invocation<Postlet, Post> {

	static final String[] HTML = {"<!DOCTYPE html><html><head><script type='application/json'>", "</script><script", "></script></head></html>"};
	
	void init(Postlet servlet, HttpServletRequest request, HttpServletResponse response, Json json) throws Exception {
		super.init(servlet, request, response, json);
		origin = getOrigin(request);
		post(request, json);
	}
	
	protected abstract void post(HttpServletRequest request, Json response) throws Exception;
	
	@Override
	protected void finish() throws IOException {
		json.value("softalks.com/view", "?");
		PrintWriter writer = response.getWriter();
		writer.append(HTML[0]).append(json.toString()).append(HTML[1]);
		if (servlet.src == null) {
			writer.append('>');
			transfer(getClass().getResourceAsStream("post.js"), response.getOutputStream());
		} else {
			writer.append(" src='").append(servlet.src).append("'");
		}
		writer.append(HTML[2]);
	}
	
	void transfer(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read = in.read(buffer);
		while (read != -1) {
			out.write(buffer, 0, read);
			read = in.read(buffer);
		}
	}
	
}