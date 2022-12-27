package com.softalks.dsu;

import static java.util.logging.Level.*;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.startlevel.FrameworkStartLevel;

class Event implements FrameworkListener, SynchronousBundleListener, ServiceListener, Objects {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String OSGI_MESSAGE = System.getProperty("{0} from bundle {1}");

	private final Server osgi;

	final Logger framework;
	final Logger bundle;
	final Logger service;

	Event(Server osgi) {
		this.osgi = osgi;
		Loggers loggers = osgi.loggers;
		framework = loggers.get(FrameworkEvent.class);
		bundle = loggers.get(BundleEvent.class);
		service = loggers.get(ServiceEvent.class);
		BundleContext context = osgi.framework.getBundleContext();
		context.addBundleListener(this);
		context.addFrameworkListener(this);
		context.addServiceListener(this);
	}

	private void framework(FrameworkEvent event) {
		int type = event.getType();
		if (type == FrameworkEvent.ERROR) {
			framework.log(SEVERE, OSGI_MESSAGE, event);
		} else if (type == FrameworkEvent.INFO) {
			framework.log(INFO, OSGI_MESSAGE, event);
		} else if (type == FrameworkEvent.PACKAGES_REFRESHED) {
			framework.info(() -> "getType(): PACKAGES_REFRESHED");
		} else if (type == FrameworkEvent.STARTED) {
			framework.info(() -> "getType(): STARTED");
		} else if (type == FrameworkEvent.STARTLEVEL_CHANGED) {
			framework.info(() -> "getType(): STARTLEVEL_CHANGED");
			framework.fine(() -> {
				FrameworkStartLevel frameworkStartLevel = osgi.framework.adapt(FrameworkStartLevel.class);
				return "current framework start level: " + frameworkStartLevel.getStartLevel();
			});
		} else if (type == FrameworkEvent.STOPPED) {
			framework.info(() -> "getType(): STOPPED");
		} else if (type == FrameworkEvent.STOPPED_UPDATE) {
			framework.info(() -> "getType(): STOPPED_UPDATE");
		} else if (type == FrameworkEvent.WAIT_TIMEDOUT) {
			framework.warning(() -> "getType(): WAIT_TIMEDOUT");
		} else if (type == FrameworkEvent.WARNING) {
			framework.log(WARNING, OSGI_MESSAGE, event);
		} else {
			throw new IllegalStateException();
		}
		framework.finer(() -> "getBundle(): " + To.string(event.getBundle()));
		framework.finer(() -> "getThrowable(): " + To.string(event.getThrowable()));
		framework.finest(event::toString);
	}

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		framework(event);
	}

	private void bundle(BundleEvent event) {
		bundle.log(Level.INFO, "{0}: {1}", event);
		bundle.fine(() -> To.string(event.getOrigin()));
		bundle.finest(event::toString);
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		bundle(event);
	}

	private void service(ServiceEvent event) {
		service.log(INFO, "{0} {1}", event);
		service.fine(() -> {
			StringBuilder builder = new StringBuilder();
			builder.append("properties ->");
			ServiceReference<?> reference = event.getServiceReference();
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
		});
		service.finer(() -> event.getSource().toString());
		service.finest(() -> {
			BundleContext context = osgi.framework.getBundleContext();
			ServiceReference<?> reference = event.getServiceReference();
			Object service = context.getService(reference);
			if (service == null) {
				return "No service object was found for this service reference";
			} else {
				Class<?> serviceClass = service.getClass();
				return serviceClass.getName();
			}
		});
		service.finest(event::toString);
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		service(event);
	}

}