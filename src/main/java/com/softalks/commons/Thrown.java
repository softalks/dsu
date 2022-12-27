package com.softalks.commons;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Thrown extends Throwables {

	private static final long serialVersionUID = 1L;

	public Thrown(Throwable thrown, Logger logger, String message, Object... parameters) {
		super(thrown, logger, message, parameters);
		error();
	}
	
	@Override
	void error() {
		logger.log(Level.SEVERE, message, super.parameters);		
	}
	
	

}
