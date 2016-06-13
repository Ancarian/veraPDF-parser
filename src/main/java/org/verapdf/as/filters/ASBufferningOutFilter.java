package org.verapdf.as.filters;

import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class ASBufferningOutFilter extends ASOutFilter {

    private int bufferCapacity;
    protected byte [] internalBuffer;
    private int bufferWriter, bufferEnd;

    public ASBufferningOutFilter(ASOutputStream stream) {
        this(stream, ASBufferingInFilter.BF_BUFFER_SIZE);
    }

    public ASBufferningOutFilter(ASOutputStream stream, int bufferCapacity) {
        super(stream);
        this.bufferCapacity = bufferCapacity;
        internalBuffer = new byte[bufferCapacity];
        bufferWriter = 0;
        bufferEnd = bufferCapacity;
    }

    @Override
    public void close() {
        super.close();
    }

    /**
     * @return the index of current write position.
     */
    public int getBufferWriter() {
        return bufferWriter;
    }

    /**
     * @return the index of the end of the buffer.
     */
    public int getBufferEnd() {
        return bufferEnd;
    }

    /**
     * @return the total capacity of buffer.
     */
    public int getBufferCapacity() {
        return bufferCapacity;
    }

    /**
     * @return number of bytes actually present in the buffer.
     */
    public int bufferSize() {
        return bufferWriter;
    }

    /**
     * Stores character to current writer position and increments writer position.
     * For better performance does not check buffer overflow, use with care.
     * @param b is character to be put into buffer.
     */
    public void storeChar(byte b) {
        internalBuffer[bufferWriter++] = b;
    }

    /**
     * Moves buffer writer pointer back by given number of bytes.
     * @param offset is number of bytes on which we should move pointer.
     * @return actual amount of bytes on which pointer was moved.
     */
    public int bufferRewind(int offset) {
        int actualOffset = Math.min(bufferWriter, offset);
        bufferWriter -= actualOffset;
        return actualOffset;
    }
}
