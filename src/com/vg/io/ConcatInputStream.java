package com.vg.io;

import junit.framework.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class ConcatInputStream extends SeekableInputStream {
    private final List<SeekableInputStream> ins;
    private SeekableInputStream in;
    private int idx;

    private long length;
    private long[] offsets;

    public ConcatInputStream(List<SeekableInputStream> ins) throws IOException {
        this.ins = new ArrayList<SeekableInputStream>(ins);
        index();
    }

    public void appendInputStream(SeekableInputStream in) throws IOException {
        this.idx = 0;
        this.ins.add(in);
        index();
    }

    private void index() throws IOException {
        this.length = 0;
        this.offsets = offsets(ins);
        this.in = ins.get(0);
        for (SeekableInputStream seekable : ins) {
            this.length += seekable.length();
        }
    }

    private static long[] offsets(List<SeekableInputStream> ins) throws IOException {
        long[] offsets = new long[ins.size()];
        long offset = 0;
        for (int i = 0; i < ins.size(); i++) {
            offsets[i] = offset;
            offset += ins.get(i).length();
        }
        return offsets;
    }

    @Override
    public int read() throws IOException {
        Assert.assertFalse(closed);
        int r = -1;
        while (r < 0 && in != null) {
            r = in.read();
            if (r < 0) {
                in = nextInput();
            }
        }
        return checkRead(r);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Assert.assertFalse(closed);
        if (len == 0)
            return 0;
        int remaining = len;
        int totalRead = 0;
        while (remaining > 0 && in != null) {
            long r = readTillEOF(b, off + (len - remaining), remaining);
            if (r == 0) {
                in = nextInput();
            }
            remaining -= r;
            totalRead += r;
        }
        return checkRead(totalRead == 0 ? -1 : totalRead);
    }

    int readTillEOF(byte[] b, int off, int len) throws IOException {
        Assert.assertFalse(closed);
        int remaining = len;
        int totalRead = 0;
        while (remaining > 0) {
            int r = in.read(b, off + (len - remaining), remaining);
            if (r < 0)
                break;
            totalRead += r;
            remaining -= r;
        }
        return totalRead;
    }

    private SeekableInputStream nextInput() {
        idx++;
        //        System.out.println(this + " nextInput " + idx);
        if (idx < ins.size())
            return ins.get(idx);
        return null;
    }

    public long length() {
        return length;
    }

    @Override
    public long seek(long position) throws IOException {
        Assert.assertFalse(closed);
        checkIdx(position);

        long remaining = position;
        idx = floorIdx(offsets, position);
        in = ins.get(idx);
        remaining -= offsets[idx];
        SeekableFileInputStream.forceSeek(in, remaining);
        return position();
    }

    public static ConcatInputStream concat(SeekableInputStream... ins) throws IOException {
        return new ConcatInputStream(asList(ins));
    }

    @Override
    public long position() {
        if (in != null) {
            return offsets[idx] + in.position();
        } else {
            return offsets[offsets.length - 1] + ins.get(ins.size() - 1).position();
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        in = ins.get(0);
        idx = 0;
        for (SeekableInputStream s : ins) {
            try {
                s.reset();
            } catch (IOException e) {
                throw e;
            }
        }
        if (0 != position()) {
            throw new IllegalStateException();
        }
    }

    volatile boolean closed = false;

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            for (SeekableInputStream in : ins) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.close();
        }
    }

    public static int floorIdx(long offsets[], long offset) {
        int idx = Arrays.binarySearch(offsets, offset);
        if (idx < 0) {
            return -(idx + 2);
        }
        return idx;
    }
}
