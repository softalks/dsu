package com.softalks.dsu;

import java.util.Arrays;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class Metadata {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private final ServiceReference<?> reference;
	private final BundleContext context;
	
	public Metadata(BundleContext context, ServiceEvent event) {
		this.context = context;
		this.reference = event.getServiceReference();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(context.getService(reference).getClass());
		builder.append(", metadata -> ");
		String[] properties = reference.getPropertyKeys();
		for (String property : properties) {
			builder.append(LINE_SEPARATOR);
			builder.append("   ");
			builder.append(property);
			builder.append(": ");
			Object value = reference.getProperty(property);
			builder.append(value.getClass().isArray() ? Arrays.toString((Object[]) value) : value);
		}
		return builder.toString();
	}
	
}