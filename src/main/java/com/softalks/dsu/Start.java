package com.softalks.dsu;

import static java.util.logging.Level.*;
import static java.util.logging.Level.INFO;
import static org.osgi.framework.FrameworkEvent.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import com.softalks.commons.Throwables;

interface Start extends Objects {

	default int getLevel(Framework framework) {
		return framework.adapt(FrameworkStartLevel.class).getStartLevel();
	}

	default int getLevel(Bundle bundle) {
		return bundle.adapt(BundleStartLevel.class).getStartLevel();
	}

	default void level(int level, Server osgi, String name, List<String> started, boolean stepping) throws Throwable {
		Framework framework = osgi.framework;
		BundleContext context = framework.getBundleContext();
		Logger out = osgi.loggers.get(Start.class);
		String bundles = name + ".zip";
		out.log(FINE, "{0}: leaving", level - 1);
		long time = System.nanoTime();
		out.log(FINE, "{0}: getting metadata from classpath /{1}", with(level, bundles));
		File metadata = new File(osgi.workarea, name + ".properties");
		Properties next = new Properties();
		Properties previous = new Properties();
		if (metadata.exists()) {
			next.load(new FileInputStream(metadata));
			previous.putAll(next);
		}
		try (ZipInputStream input = new ZipInputStream(osgi.resources.get(bundles))) {
			ZipEntry entry = null;
			setInitialBundleLevel(framework, level);
			while ((entry = input.getNextEntry()) != null) {
				String bundle = entry.getName();
				if (started.contains(bundle)) {
					throw new IllegalStateException("Duplicated ZIP entry: " + bundle);
				} else {
					started.add(bundle);	
				}
				out.log(FINE, "{0}: ZIP entry {1}", with(level, bundle));
				String property = previous.getProperty(bundle);
				if (property == null) {
					next.put(bundle, String.valueOf(entry.getCrc()));
					out.log(INFO, "{0}: installing {1}", with(level, bundle));
					Bundle installed = context.getBundle(bundle);
					if (installed != null) {
						installed.uninstall();
					}
					installed = context.installBundle(bundle, new UnclosableInputStream(input));
					installed.start();
					installed.adapt(BundleStartLevel.class).setStartLevel(level);
				} else {
					previous.remove(bundle);
					long installed = Long.parseLong(property);
					long found = entry.getCrc();
					if (installed != found) {
						out.log(INFO, "{0}: changes detected on {1} (CRC installed {2} != found {3}) ==> updating", with(level, bundle, installed, found));
						context.getBundle(bundle).update(new UnclosableInputStream(input));
					}
				}
			}
		}
		for (Object key : previous.keySet()) {
			String bundle = key.toString();
			out.log(Level.INFO, "{0}: uninstalling {0}", new Object[] { level, bundle});
			context.getBundle(bundle).uninstall();
			next.remove(key);
		}
		try (OutputStream file = new FileOutputStream(metadata)) {
			String comment = "Bundles currently installed at start level " + level + " with its corresponding CRC";
			next.store(file, comment);
		}
		if (!stepping) {
			return;
		}
		if (osgi.startLevel == null || level <= osgi.startLevel) {
			CompletableFuture<Throwable> reached = new CompletableFuture<>();
			framework.adapt(FrameworkStartLevel.class).setStartLevel(level, new FrameworkListener() {
				@Override
				public void frameworkEvent(FrameworkEvent event) {
					int type = event.getType();
					if (type == STARTLEVEL_CHANGED) {
						reached.complete(null);
					} else if (type == ERROR) {
						Throwable thrown = event.getThrowable();
						if (thrown == null) {
							thrown = new IllegalStateException();
						}
						reached.complete(thrown);
					}
				}
			});
			Throwable error;
			try {
				error = reached.get();
			} catch (Throwable thrown) {
				error = thrown;
			}
			if (error == null) {
				time = System.nanoTime() - time;
				out.log(INFO, "{0}: reached in {1} milliseconds", with(level, TimeUnit.NANOSECONDS.toMillis(time)));	
			} else {
				out.log(Level.SEVERE, "{0}: could not be reached because of this {1}", with(level, Throwables.string(error)));
				throw error;
			}
		}		
	}

	default void setInitialBundleLevel(Framework bundle, int level) {
		bundle.adapt(FrameworkStartLevel.class).setInitialBundleStartLevel(level);
	}

	default void setLevel(Bundle bundle, int level) {
		bundle.adapt(BundleStartLevel.class).setStartLevel(level);
	}

	default void setLevel(Framework framework, int level) {
		framework.adapt(FrameworkStartLevel.class).setStartLevel(level);
	}
	
}