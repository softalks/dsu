package com.softalks.dsu;

import java.io.InputStream;

class Resources {

	private ClassLoader classpath;
	private String base;
	private Class<?> agent;
	
	public Resources(Object agent) {
		this.agent = agent.getClass();
	}
	
	public Resources(Class<?> agent) {
		this.agent = agent;
	}
	
	public Resources(ClassLoader classLoader, String base) {
		this.classpath = classLoader;
		this.base = canonical(base);
	}
	
	public InputStream get(String path) {
		if (agent != null) {
			return agent.getResourceAsStream(path);
		} else {
			return classpath.getResourceAsStream(base + (path.charAt(0) == '/'? path.substring(0) : path));	
		}
	}
	
	private String canonical(String path) {
		String canonicalPath = path.startsWith("/") ? path.substring(1) : path;
		if (!canonicalPath.isEmpty()) {
			canonicalPath = canonicalPath.endsWith("/")? canonicalPath : canonicalPath + '/';
		}
		return canonicalPath;
	}
	
}
