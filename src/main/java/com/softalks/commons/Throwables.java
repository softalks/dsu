package com.softalks.commons;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Logger;

public abstract class Throwables extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	protected final Logger logger;
	protected final String message;
	protected final Object[] parameters;
	
	protected Throwables(String message, Object... parameters) {
		this(Logger.getGlobal(), message, parameters);
	}
	
	protected Throwables(Logger logger, String message, Object... parameters) {
		this(null, logger, message, parameters);
	}

	public static String string(Throwable object) {
		if (object == null) {
			return "null";
		}
		StringWriter writer = new StringWriter();
		object.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
	
	protected Throwables(Throwable thrown, Logger logger, String message, Object... parameters) {
		super(thrown);
		this.logger = logger;
		this.message = message;
		this.parameters = Arrays.copyOf(parameters, parameters.length + 1);
		String stack = string(this);
		this.parameters [parameters.length] = stack.substring(stack.indexOf(LINE_SEPARATOR));
	}
	
	abstract void error();
	
}