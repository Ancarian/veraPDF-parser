package org.verapdf.as.io;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface ASInputStream {

	int nPos = -1;

	int read(byte[] buffer, int size) throws IOException;

	int skip(int size) throws IOException;

	void close() throws IOException;

	void reset() throws IOException;

	boolean isCloneable();

	//TODO : clone method

}
