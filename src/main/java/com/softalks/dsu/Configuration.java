package com.softalks.dsu;

import static java.util.logging.Level.*;
import static org.osgi.framework.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Constants;

import com.softalks.commons.Client;

class Configuration {

	private static final String USER_HOME = System.getProperty("user.home").replace('\\', '/');
	
	final Server core;
	final Logger out;
	final Map<String, String> settings = new HashMap<String, String>();
	final Map<String, String> packages = new HashMap<String, String>();

	Configuration(Server osgi) {
		this.core = osgi;
		out = osgi.loggers.get(Configuration.class);
		try {
			load(core.resources);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		exports();
		if (get(FRAMEWORK_STORAGE) == null) {
			set(FRAMEWORK_STORAGE, core.workarea + "/framework");
		}
	}

	String get(String key) {
		return settings.get(key);
	}

	String set(String property, Object value) {
		out.log(INFO, Messages.CONFIGURED, new String[] { property, value.toString() });
		return settings.put(property, value.toString());
	}

	void add(String name, String version) {
		packages.put(name, version);
		String message = "{0} {1} to " + Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;
		out.log(Level.FINE, message, new String[] { name, version });
	}

	void configure(String property, String value) {
		if (value.startsWith("-D")) {
			String system = System.getProperty(property);
			if (system == null) {
				if (value.length() > 2 && value.charAt(2) == '|') {
					if (value.length() == 3) {
						throw new Client(out, Messages.MISSING_MANDATORY_CONFIGURATION, property);
					}
					set(property, value.substring(3).replace("${user.home}", USER_HOME));
				}
			} else {
				set(property, system);
			}
		} else {
			set(property, value);
		}
	}

	void exports() {
		StringBuilder value = null;
		for (Entry<String, String> entry : packages.entrySet()) {
			if (value == null) {
				value = new StringBuilder();
			} else {
				value.append(',');
			}
			value.append(entry.getKey());
			value.append(";version=\"");
			value.append(entry.getValue());
			value.append('"');
		}
		set(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, value.toString());
	}

	Map<String, String> configure() throws IOException {
		load(core.resources);
		exports();
		if (get(FRAMEWORK_STORAGE) == null) {
			set(FRAMEWORK_STORAGE, core.workarea + "/framework");
		}
		return settings;
	}

	void load(Resources resources) throws IOException {
		try (InputStream resource = resources.get("configuration.properties")) {
			if (resource != null) {
				Properties properties = new Properties();
				properties.load(resource);
				for (String key : properties.stringPropertyNames()) {
					configure(key, properties.getProperty(key));
				}
			}
		}
		try (InputStream resource = resources.get("packages.properties")) {
			if (resource == null) {
				return;
			}
			Properties packages = new Properties();
			packages.load(resource);
			for (String key : packages.stringPropertyNames()) {
				add(key, packages.getProperty(key));
			}
		}
	}
	
}