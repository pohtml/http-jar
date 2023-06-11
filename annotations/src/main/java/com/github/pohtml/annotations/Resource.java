package com.github.pohtml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Resource {
	String servlet() default "";

	/**
	 * The context path of the static HTML file presenting the JSON representation
	 * of this resource. The value of this parameter can be
	 * <ul>
	 * <li>An empty string (the default) meaning that the URI of this resource is
	 * relative to the same context in the static files server as the one in the
	 * servlet container</li>
	 * <li>The string "/" meaning that the URI of this resource is relative to the
	 * static files server base URI</li>
	 * <li>Any string starting by "/" meaning that the URI of this resource is
	 * relative to the specified context in the static files server base URI</li>
	 * </ul>
	 */
	String context() default "";

	/**
	 * The context relative URI of this resource. Wild cards not allowed
	 */
	String uri() default "";

	/**
	 * It will be interpreted as the context relative URI of this resource if the
	 * parameter <b><code>uri</code></b> is not provided. This will allow using this
	 * annotation with its default, not named, parameter (as the @WebServlet
	 * annotation behaves)
	 */
	String value() default "";
}
