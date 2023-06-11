package com.github.pohtml;

@FunctionalInterface
public interface Loader<T> {

	void load(T instance) throws Exception;
	
}
