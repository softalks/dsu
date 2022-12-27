package com.softalks.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Consumed {

	public static <T> T map(Consumer<T> consumer) {
		Map<?, ?> map = new HashMap<>();
		@SuppressWarnings("unchecked")
		T cast = (T) map;
		consumer.accept(cast);
		return cast;
	}

}
