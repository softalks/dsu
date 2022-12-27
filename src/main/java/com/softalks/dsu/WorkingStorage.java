package com.softalks.dsu;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import com.softalks.commons.Timestamps;

interface WorkingStorage extends Timestamps {

	default File workingStorage(String name) {
		return workingStorage(new File(System.getProperty("user.dir")), name);
	}
	
	default File workingStorage(File root, String name) {
		File base = new File(root, name);
		File workarea = null;
		if (!base.exists()) {
			workarea = new File(base, now());
			workarea.mkdirs();
			return workarea;
		} else if (base.isDirectory()) {
			File[] workareas = base.listFiles(new FilenameFilter() {
				SimpleDateFormat date = dateFormat();
				@Override
				public boolean accept(File container, String name) {
					try {
						date.parse(name);
						return true;
					} catch (ParseException e) {
						return false;
					}
				}
			});
			boolean previous = true;
			Collections.sort(Arrays.asList(workareas), Collections.reverseOrder());
			for (File candidate : workareas) {
				if (workarea == null) {
					workarea = candidate;
				} else if (previous) {
					delete(candidate);
				} else {
					previous = false;
				}
			}
			File ok = new File(workarea, "stopped.ok");
			if (ok.isFile() && ok.delete()) {
				return workarea;
			} else {
				workarea = new File(base, now());
				workarea.mkdir();
				return workarea;
			}
		} else {
			throw new IllegalStateException();
		}
	}

	default void delete(File directory) {
		File[] contents = directory.listFiles();
		if (contents != null) {
			for (File content : contents) {
				if (!Files.isSymbolicLink(content.toPath())) {
					delete(content);
				}
			}
		}
		directory.delete();
	}
	
}