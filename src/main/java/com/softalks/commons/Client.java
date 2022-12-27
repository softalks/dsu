package com.softalks.commons;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Throwables {

	private static final long serialVersionUID = 1L;

	public Client(String message, Object... parameters) {
		super(message, parameters);
		error();
	}
	
	public Client(Logger logger, String message, Object... parameters) {
		super(logger, message, parameters);
		error();
	}

	@Override
	void error() {
		logger.log(Level.SEVERE, message, super.parameters);		
	}
	
	

}
