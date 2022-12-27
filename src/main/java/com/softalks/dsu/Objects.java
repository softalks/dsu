package com.softalks.dsu;

import java.util.Locale;
import java.util.function.Supplier;

public interface Objects {
	
	default Supplier<String> log(String message, Object ... parameters) {
		Locale locale = Locale.getDefault();
		return () -> {
			return String.format(locale, message, parameters);
		};
	}
	
	default Object[] with(Object... parameters) {
		return parameters;
	}
	
}
