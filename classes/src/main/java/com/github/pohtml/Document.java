package com.github.pohtml;

public class Document {

	Json backend;
	
	public Document() {
	}
	
	public Document(Json backend) {
		this.backend = backend;
	}
	
	public void root(Loader<Json> consumer) {
		if (backend != null) {
			throw new IllegalStateException();
		}
		backend = new Json();
		try {
			consumer.load(backend);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public String toString() {
		return backend.toString();
	}
	
}