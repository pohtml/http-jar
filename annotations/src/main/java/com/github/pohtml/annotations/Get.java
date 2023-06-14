package com.github.pohtml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Get {
	public String uri() default ""; // getClass().getName() if not specified
	public String context() default ""; // only if remote
	public String html() default ""; // default: uri() + ".html";
	public String value() default "";
}