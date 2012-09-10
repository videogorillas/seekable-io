package com.vg.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileBufferedInputStream extends SeekableInputStream {

    private final RandomAccessFile raf;
    private final FileInputStream fin;
    private final long startPosition;
    private InputStream cin;
    private long pos;
    private String path;

    public RandomAccessFileBufferedInputStream(File file, long offset) throws IOException {
        this(raf(file, offset));
        this.path = file.getAbsolutePath();
    }

    private static RandomAccessFile raf(File file, long offset) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        if (offset != 0) {
            SeekableFileInputStream.forceSeek(raf, offset);
        }
        return raf;
    }

    public RandomAccessFileBufferedInputStream(File file) throws IOException {
        this(file, 0);
    }

    RandomAccessFileBufferedInputStream(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        this.fin = new FileInputStream(raf.getFD());
        this.startPosition = raf.getFilePointer();
        _reset();
    }

    private long count = 0;

    private void _reset() throws IOException {
        this.pos = raf.getFilePointer() - this.startPosition;
        this.count = 0;
        this.cin = new BufferedInputStream(fin, 65536);
    }

    @Override
    public int read() throws IOException {
        int read = cin.read();
        if (read != -1) {
            count++;
        }
        return checkRead(read);
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = cin.read(b);
        if (read != -1) {
            count += read;
        }
        return checkRead(read);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = cin.read(b, off, len);
        if (read != -1) {
            count += read;
        }
        return checkRead(read);
    }

    @Override
    synchronized public void close() throws IOException {
        if (cin != null) {
            cin.close();
            cin = null;
            raf.close();
        }
    }

    @Override
    public long seek(long position) throws IOException {
        checkIdx(position);
        SeekableFileInputStream.forceSeek(raf, startPosition + position);
        _reset();
        return position();
    }

    public long position() {
        return pos + count;
    }

    @Override
    public long length() {
        try {
            return raf.length() - startPosition;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

}
