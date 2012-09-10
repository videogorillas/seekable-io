package com.vg.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class SeekableInputStream extends InputStream {

    public abstract long seek(long position) throws IOException;

    public abstract long length();

    public abstract long position();

    @Override
    public synchronized void reset() throws IOException {
        seek(0);
    }

    protected void checkIdx(long position) throws IOException {
        long len = length();
        if (position < 0 || position > len) {
            throw new IndexOutOfBoundsException("trying to seek to " + position + " while len is " + len);
        }
    }

    protected final int checkRead(int read) throws IOException {
        if (read != -1) {
            return read;
        }
        long pos = position();
        long len = length();
        if (pos != len) {
            throw new IllegalStateException("position " + pos + " at end of stream != length " + len);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long pos = position();
        long newpos = seek(pos + n);
        return newpos - pos;
    }
}