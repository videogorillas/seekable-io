package com.vg.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeekableLimitedInputStream extends SeekableInputStream {

    private final long limit;

    private long pos;

    private final SeekableInputStream in;

    private final long startPosition;

    private final AtomicBoolean seeked = new AtomicBoolean(false);

    public SeekableLimitedInputStream(SeekableInputStream in, long limit) {
        this(in, 0, limit);
    }

    /**
     * 
     * @param in
     * @param startOffset
     * @param limit
     *            limit in bytes starting from startOffset
     * @throws IOException
     */
    public SeekableLimitedInputStream(SeekableInputStream in, long startOffset, long limit) {
        this.in = in;
        this.limit = limit < 0 ? 0 : limit;
        this.startPosition = startOffset;
    }

    public int read() throws IOException {
        if (seeked.compareAndSet(false, true)) {
            seek(0);
        }

        if (pos < limit) {
            int read = in.read();
            if (read >= 0) {
                pos++;
            }
            return read;
        }
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (seeked.compareAndSet(false, true)) {
            seek(0);
        }

        if (limit - pos < len) {
            len = (int) (limit - pos);
            if (len <= 0) {
                return -1;
            }
        }

        int res = in.read(b, off, len);
        if (res > 0) {
            pos += res;
        }
        return res;
    }

    @Override
    public long length() {
        return limit;
    }

    @Override
    public long seek(long position) throws IOException {
        seeked.set(true);
        checkIdx(position);
        SeekableFileInputStream.forceSeek(in, startPosition + position);
        this.pos = position;
        return position();
    }

    @Override
    public long position() {
        return pos;
    }

    @Override
    public void reset() throws IOException {
        seeked.set(false);
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

}
