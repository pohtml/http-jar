package com.github.pohtml;

import org.json.JSONArray;

public class Array {
	
	JSONArray backend = new JSONArray();
	
	public void array(Loader<Array> consumer) {
		Array array = new Array();
		try {
			consumer.load(array);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		backend.put(array.backend);
	}
	
	public void object(Loader<Json> consumer) {
		Json object = new Json();
		try {
			consumer.load(object);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		backend.put(object.backend);
	}
	
	public void value(Object value) {
		backend.put(value);
	}
	
}