package com.vg.io;

import java.io.IOException;


public class ZeroPadInputStream extends SeekableInputStream {
    private final long limit;
    private long pos = 0;

    public ZeroPadInputStream(long limit) {
        this.limit = limit;
    }

    @Override
    public long length() {
        return limit;
    }

    @Override
    public long position() {
        return pos;
    }

    @Override
    public long seek(long position) throws IOException {
        checkIdx(position);
        this.pos = position;
        return position();
    }

    @Override
    public int read() throws IOException {
        if (pos < limit) {
            pos++;
            return 0;
        }
        return -1;
    }

    public static ZeroPadInputStream zeropad(long limit) {
        return new ZeroPadInputStream(limit);
    }

}
