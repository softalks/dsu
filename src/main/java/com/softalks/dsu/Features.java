package com.softalks.dsu;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.osgi.framework.BundleContext;

public interface Features extends Consumer<BundleContext>, Function<Map<String, Object>, List<String>> {
	
}