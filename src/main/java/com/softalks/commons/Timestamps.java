package com.softalks.commons;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface Timestamps {

	default String now() {
		return dateFormat().format(new Date());
	}

	default SimpleDateFormat dateFormat() {
		return new SimpleDateFormat(dateFormatString());
	}
	
	default String dateFormatString() {
		return "yyyy.MMdd_HHmm.ss";
	}
	
}
