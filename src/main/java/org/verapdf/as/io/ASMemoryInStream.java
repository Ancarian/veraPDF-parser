package org.verapdf.as.io;

import org.verapdf.io.SeekableStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class binds the ASInputStream interface to a memory buffer.
 *
 * @author Sergey Shemyakov
 */
public class ASMemoryInStream extends SeekableStream {

    private int bufferSize;
    private int currentPosition;
    private byte[] buffer;
    private boolean copiedBuffer;

    /**
     * Constructor from byte array. Buffer is copied while initializing
     * ASMemoryInStream.
     *
     * @param buffer byte array containing data.
     */
    public ASMemoryInStream(byte[] buffer) {
        this(buffer, buffer.length);
    }

    /**
     * Constructor from byte array and actual data length. Buffer is copied
     * while initializing ASMemoryInStream.
     *
     * @param buffer     byte array containing data.
     * @param bufferSize actual length of data in buffer.
     */
    public ASMemoryInStream(byte[] buffer, int bufferSize) {
        this(buffer, bufferSize, true);
    }

    /**
     * Constructor from byte array and actual data length. Whether buffer is
     * copied deeply or just reference is copied can be manually set.
     *
     * @param buffer     byte array containing data.
     * @param bufferSize actual length of data in buffer.
     * @param copyBuffer is true if buffer should be copied deeply. Note that if
     *                   it is set into false then internal buffer can be changed
     *                   from outside of this class.
     */
    public ASMemoryInStream(byte[] buffer, int bufferSize, boolean copyBuffer) {
        this.bufferSize = bufferSize;
        this.currentPosition = 0;
        this.copiedBuffer = copyBuffer;
        if (copyBuffer) {
            this.buffer = Arrays.copyOf(buffer, bufferSize);
        } else {
            this.buffer = buffer;
        }
    }

    /**
     * Reads up to size bytes of data into given array.
     *
     * @param buffer is array into which data is read.
     * @param size   is maximal amount of data that can be read.
     * @return actual amount of bytes reas.
     * @throws IOException
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (currentPosition == bufferSize) {
            return -1;
        }
        int available = Math.min(bufferSize - currentPosition, size);
        try {
            System.arraycopy(this.buffer, currentPosition, buffer, 0, available);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't write bytes into passed buffer: too small.");
        }
        currentPosition += available;
        return available;
    }

    /**
     * Reads single byte.
     *
     * @return byte read or -1 if end is reached.
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (currentPosition == bufferSize) {
            return -1;
        }
        return this.buffer[currentPosition++];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int peek() throws IOException {
        if (currentPosition == bufferSize) {
            return -1;
        }
        return this.buffer[currentPosition];
    }

    /**
     * Skips up to size bytes of data.
     *
     * @param size is amount of bytes to skip.
     * @return actual amount of bytes skipped.
     * @throws IOException
     */
    @Override
    public int skip(int size) throws IOException {
        int available = Math.min(bufferSize - currentPosition, size);
        currentPosition += available;
        return available;
    }

    /**
     * Closes stream.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        bufferSize = 0;
        currentPosition = 0;
        buffer = null;
    }

    /**
     * Resets stream.
     *
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        currentPosition = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStreamLength() throws IOException {
        return this.bufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getOffset() throws IOException {
        return this.currentPosition;
    }

    @Override
    public void seek(long offset) throws IOException {
        if (offset < 0 || offset >= this.bufferSize) {
            throw new IOException("Can't seek for offset " + offset + " in ASMemoryInStream");
        }
    }

    /**
     * @return the amount of bytes left in stream.
     */
    public int available() {
        return bufferSize - currentPosition;
    }

    /**
     * @return true if internal buffer was copied deeply.
     */
    public boolean isCopiedBuffer() {
        return copiedBuffer;
    }

    @Override
    public ASInputStream getStream(long startOffset, long length) throws IOException {
        if (startOffset > 0 && startOffset < this.bufferSize &&
                startOffset + length < this.bufferSize) {
            return new ASMemoryInStream(Arrays.copyOfRange(
                    this.buffer, (int) startOffset, (int) (startOffset + length)));
        } else {
            throw new IOException();
        }
    }
}
