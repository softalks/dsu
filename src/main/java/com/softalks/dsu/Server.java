package com.softalks.dsu;

import static java.util.logging.Level.*;
import static org.osgi.framework.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public final class Server implements BiConsumer<Features, Class<?>>, Consumer<Features>, Start, WorkingStorage, SynchronousBundleListener {

	final List<Consumer<BundleContext>> listeners = new ArrayList<>();
	Integer startLevel;
	Resources resources;
	final File workarea;
	public final Loggers loggers;
	private Configuration configuration;
	private final FrameworkFactory factory;
	Framework framework;
	public final Logger out;

	private boolean stopping = false;

	public Server(Features agent) {
		this((Consumer<BundleContext>)null, "OSGi");
		accept(agent);
	}
	
	public Server(Consumer<BundleContext> listener, String name) {
		this.listeners.add(listener);
		workarea = workingStorage(name);
		loggers = new Loggers(workarea);
		try {
			out = loggers.get(this);
			Iterator<FrameworkFactory> iterator = ServiceLoader.load(FrameworkFactory.class).iterator();
			if (iterator.hasNext()) {
				factory = iterator.next();
			} else {
				loggers.get(Server.class).log(SEVERE, Messages.OSGI_MISSING, FrameworkFactory.class);
				throw new Error();
			}
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} catch (Throwable e) {
			throw (Error) e;
		}
	}

	public final BundleContext getBundleContext() {
		return framework.getBundleContext();
	}
	
	public final void stop() {
		try {
			framework.stop();
			framework.waitForStop(0);
		} catch (BundleException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}
	
	private void configure(String key, String value) {
		configuration.configure(key, value);
	}

	private void start(String... levels) {
		try {
			int level = 1;
			String previous = configuration.set(FRAMEWORK_BEGINNING_STARTLEVEL, level);
			startLevel = previous == null ? null : Integer.parseInt(previous);
			framework = factory.newFramework(configuration.settings);
			framework.start();
			BundleContext context = framework.getBundleContext();
			for (Consumer<BundleContext> listener : listeners) {
				listener.accept(context);				
			}
			new Event(this);
			context.addBundleListener(this);
			List<String> started = new ArrayList<>();
			for (String name : levels) {
				level(++level, this, name, started, false);
			}
			if (startLevel > getLevel(framework)) {
				setLevel(framework, startLevel);
			}
		} catch (RuntimeException e) {
			failing();
			throw (RuntimeException) e;
		} catch (Exception e) {
			failing();
			throw new IllegalStateException(e);
		} catch (Throwable e) {
			failing();
			throw (Error) e;
		}
	}

	private void failing() {
		try {
			if (framework == null || framework.getState() != Framework.ACTIVE) {
				framework.waitForStop(0);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}

	private void stopping() {
		stopping = true;
		out.log(Level.WARNING, "waiting for the OSGi framework to stop...");
	}

	private void stopped() {
		out.log(Level.INFO, "the OSGi framework has been stopped successfully");
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STOPPING && event.getBundle().getBundleId() == 0 && !stopping) {
			stopping();
			Timer timer = new Timer("OSGi clean exit detector");
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (framework.getState() == Bundle.RESOLVED) {
						try {
							new File(workarea, "stopped.ok").createNewFile();
						} catch (IOException e) {
							throw new IllegalStateException(e);
						}
						timer.cancel();
						stopped();
					}
				}
			}, 1000, 2000);
		}
	}

	public void setStartLevel(int level) {
		setLevel(framework, level);
	}

	@Override
	public void accept(Features agent) {
		if (resources == null) {
			resources = new Resources(agent);	
		}
		configuration = new Configuration(this, agent);
		Map<String, Object> map = new HashMap<>();
		List<String> levels = agent.apply(map);
		for (Entry<String, Object> setting : map.entrySet()) {
			Object value = setting.getValue();
			configure(setting.getKey(), value instanceof String ? (String) value : value.toString());
		}
		listeners.add(agent);
		start(levels.toArray(new String[levels.size()]));
	}

	@Override
	public void accept(Features t, Class<?> u) {
		resources = new Resources(u);
		accept(t);
	}

}