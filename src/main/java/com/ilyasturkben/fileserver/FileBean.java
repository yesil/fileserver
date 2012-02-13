package com.ilyasturkben.fileserver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileBean {
	private static final DateFormat formatter = new SimpleDateFormat(
			"dd-MMM-yy hh:mm:ss");
	private static final String DIR = "dir";
	private static final String FILE = "file";

	private final File file;
	private final URI base;
	private final String label;

	public FileBean(File file, URI base, String label) {
		this.file = file;
		this.base = base;
		this.label = label;
	}

	public String getType() {
		return file.isDirectory() ? DIR : FILE;
	}

	public String getName() {
		return label != null ? label : file.getName();
	}

	public String getLastModifiedDate() {
		return formatter.format(new Date(file.lastModified()));
	}

	public String getUrl() throws URISyntaxException {
		return base.relativize(file.toURI()).getPath();
	}

	public boolean isResource() {
		return file.isDirectory() && file.list() == null;
	}

	public long getSize() {
		boolean isDirectory = file.isDirectory();
		if (isDirectory) {
			String[] list = file.list();
			if (list == null) {
				isDirectory = false;
			} else {
				return list.length;
			}
		}
		return file.length();
	}
}