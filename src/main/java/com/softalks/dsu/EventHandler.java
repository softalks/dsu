package com.softalks.dsu;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;

class EventHandler extends FileHandler {

	@Override
	public Formatter getFormatter() {
		return new SimpleFormatter() {
			@Override
			public synchronized String format(LogRecord record) {
				Object[] args = record.getParameters();
				args = args == null ? new Object[1] : Arrays.copyOf(args, args.length + 1);
				args[args.length - 1] = "Hello, World!";
				record.setParameters(args);
				return super.format(record);
			}
		};
	}

	public EventHandler(File workarea) throws IOException, SecurityException {
		super(pattern(workarea), true);
		setLevel(Level.FINEST);
	}

	private static String pattern(File workarea) {
		File base = new File(workarea, "log");
		base.mkdir();
		return base.getPath() + "/%u.log";
	}

	@Override
	public synchronized void publish(LogRecord record) {
		String logger = record.getLoggerName();
		if (logger == FrameworkEvent.class.getName()) {
			Object[] args = record.getParameters();
			if (args != null) {
				FrameworkEvent event = (FrameworkEvent) record.getParameters()[0];
				record.setThrown(event.getThrowable());
				record.setParameters(new Object[] { "getType(): " + getType(event), event.getBundle() });
			}
		} else if (logger == BundleEvent.class.getName()) {
			Object[] args = record.getParameters();
			if (args != null) {
				BundleEvent event = (BundleEvent) record.getParameters()[0];
				record.setParameters(new Object[] { "getType(): " + getType(event), event.getBundle() });
			}
		} else if (logger == ServiceEvent.class.getName()) {
			Object[] args = record.getParameters();
			if (args != null) {
				ServiceEvent event = (ServiceEvent) record.getParameters()[0];
				Object objectClass = event.getServiceReference().getProperty(Constants.OBJECTCLASS);
				String[] array = (String[]) objectClass;
				String api = array.length == 1 ? array[0] : Arrays.toString(array);
				record.setParameters(new Object[] { "getType(): " + getType(event), api });
			}
		}
		super.publish(record);
	}

	static String getType(FrameworkEvent event) {
		int type = event.getType();
		if (type == FrameworkEvent.ERROR) {
			return "ERROR";
		} else if (type == FrameworkEvent.WARNING) {
			return "WARNING";
		} else if (type == FrameworkEvent.INFO) {
			return "INFO";
		} else {
			throw new IllegalArgumentException();
		}
	}

	static String getType(BundleEvent event) {
		int id = event.getType();
		if (id == BundleEvent.INSTALLED) {
			return "INSTALLED";
		} else if (id == BundleEvent.LAZY_ACTIVATION) {
			return "LAZY_ACTIVATION";
		} else if (id == BundleEvent.RESOLVED) {
			return "RESOLVED";
		} else if (id == BundleEvent.STARTED) {
			return "STARTED";
		} else if (id == BundleEvent.STARTING) {
			return "STARTING";
		} else if (id == BundleEvent.STOPPED) {
			return "STOPPED";
		} else if (id == BundleEvent.STOPPING) {
			return "STOPPING";
		} else if (id == BundleEvent.UNINSTALLED) {
			return "UNINSTALLED";
		} else if (id == BundleEvent.UNRESOLVED) {
			return "UNRESOLVED";
		} else if (id == BundleEvent.UPDATED) {
			return "UPDATED";
		} else {
			throw new IllegalStateException();
		}
	}

	private String getType(ServiceEvent event) {
		int id = event.getType();
		if (id == ServiceEvent.MODIFIED) {
			return "MODIFIED";
		} else if (id == ServiceEvent.MODIFIED_ENDMATCH) {
			return "MODIFIED_ENDMATCH";
		} else if (event.getType() == ServiceEvent.REGISTERED) {
			return "REGISTERED";
		} else if (id == ServiceEvent.UNREGISTERING) {
			return "UNREGISTERING";
		} else {
			throw new IllegalStateException();
		}
	}

}