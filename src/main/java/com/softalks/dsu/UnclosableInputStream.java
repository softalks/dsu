package com.softalks.dsu;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@linkplain InputStream input stream} wrapper avoiding its wrapped stream
 * to be closed
 */
class UnclosableInputStream extends InputStream {

	private final InputStream backend;

	public UnclosableInputStream(InputStream backend) {
		this.backend = backend;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return backend.read(b);
	}

	@Override
	public int read() throws IOException {
		return backend.read();
	}

}
