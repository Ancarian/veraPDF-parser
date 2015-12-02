package org.verapdf.io;

import org.verapdf.as.ASInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Timur Kamalov
 */
public class InternalInputStream implements ASInputStream {

	private final static String READ_ONLY_MODE = "r";

	private RandomAccessFile source;

	public InternalInputStream(final String fileName) throws FileNotFoundException {
		this.source = new RandomAccessFile(fileName, READ_ONLY_MODE);
	}

	public int read(byte[] buffer, int size) throws IOException {
		return this.source.read(buffer, 0, size);
	}

	public int skip(int size) throws IOException {
		return this.source.skipBytes(size);
	}

	public void close() throws IOException {
		this.source.close();
	}

	public void reset() throws IOException {
		this.source.seek(0);
	}

	public boolean isCloneable() {
		return false;
	}

}
