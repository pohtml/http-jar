package com.github.pohtml.json;


import org.json.JSONObject;

import com.github.pohtml.Loader;

public class Json {
	
	JSONObject backend = new JSONObject();
	
	public void reset() {
		backend = new JSONObject();
	}
	
	public void array(String key, Loader<Array> consumer) {
		Array array = new Array();
		try {
			consumer.load(array);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		backend.put(key, array.backend);
	}
	
	public void object(String key, Object delegate) {
		if (delegate instanceof JSONObject) {
			backend.put(key, (JSONObject)delegate);
		} else if (delegate instanceof Json) {
			backend.put(key, ((Json)delegate).backend);
		} else {
			throw new IllegalStateException("Gson delegation not implemented yet");
		}
	}
	
	public void object(String key, Loader<Json> consumer) {
		Json object = new Json();
		try {
			consumer.load(object);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		backend.put(key, object.backend);
	}
	
	public void value(String key, Object value) {
		backend.put(key, value);
	}

	public String toString(int indent) {
		return backend.toString(indent);
	}
	
	@Override
	public String toString() {
		return backend.toString();
	}
	
}