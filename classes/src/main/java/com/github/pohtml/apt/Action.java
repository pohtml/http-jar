package com.github.pohtml.apt;

import com.github.pohtml.annotations.Get;

class Action {
	
	final String value;
	final String html;
	final String uri;
	final String context;

	Action(Get get) {
		this(get.uri(), get.context(), get.html(), get.value());
	}

	Action(String uri, String context, String html, String value) {
		this.uri = uri;
		this.context = context;
		this.html = html;
		this.value = value;
	}
	
}