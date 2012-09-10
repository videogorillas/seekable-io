package com.vg.io;

import static java.lang.Math.min;

import java.io.IOException;
import java.nio.ByteBuffer;


public class SeekableByteArrayInputStream extends SeekableInputStream {

    private final ByteBuffer buf;

    public SeekableByteArrayInputStream(byte[] array) {
        buf = ByteBuffer.wrap(array);
    }

    @Override
    public long length() {
        return buf.capacity();
    }

    @Override
    public long position() {
        return buf.position();
    }

    @Override
    public long seek(long position) throws IOException {
        return buf.position((int) position).position();
    }

    @Override
    public int read() throws IOException {
        return buf.hasRemaining() ? (buf.get() & 0xff) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (buf.hasRemaining()) {
            int bytesRead = min(buf.remaining(), len);
            buf.get(b, off, bytesRead);
            return bytesRead;
        }
        return -1;
    }

}
