package com.github.pohtml;

@FunctionalInterface
public interface Representation<T> {

	void load(T model) throws Exception;
	
}
