package com.softalks.dsu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;

public class Loggers {

	private Map<Class<?>, Logger> map = new HashMap<>();
	private final Logger parent;
	private Properties properties = new Properties();
	
	Loggers(File workarea) {
		try {
			File file = new File("logging.properties");
			if (file.exists()) {
				properties.load(new FileInputStream(new File("logging.properties")));	
			}
		} catch (IOException e1) {
			throw new IllegalStateException();
		}
		parent = Logger.getLogger(getClass().getPackage().getName());
		parent.setUseParentHandlers(false);
		for (Handler handler : parent.getHandlers()) {
			parent.removeHandler(handler);
		}
		try {
			parent.addHandler(new EventHandler(workarea));
		} catch (SecurityException | IOException e) {
			throw new IllegalStateException(e);
		}
		log(Server.class, Start.class, FrameworkEvent.class, BundleEvent.class, ServiceEvent.class, Configuration.class);
	}

	public Logger get(Class<?> source) {
		return map.get(source);
	}
	
	public Logger get(Object source) {
		return get(source.getClass());
	}

	private void log(Class<?>... sources) {
		for (Class<?> source : sources) {
			String name = source.getName();
			Logger result = Logger.getLogger(name);
			result.setParent(parent);
			result.setUseParentHandlers(true);
			String level = (String) properties.get(name + ".level");
			if (level != null) {
				result.setLevel(Level.parse(level));
			}
			map.put(source, result);
		}
	}

}
