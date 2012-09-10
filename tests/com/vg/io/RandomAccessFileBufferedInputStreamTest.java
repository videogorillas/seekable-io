package com.vg.io;

import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RandomAccessFileBufferedInputStreamTest {

    RandomAccessFileBufferedInputStream in;

    @Before
    public void setup() throws IOException {
        File file = File.createTempFile("raftest", ".dat");
        FileUtils.writeStringToFile(file, "0123456789abcdef");
        // file.deleteOnExit();
        in = new RandomAccessFileBufferedInputStream(new RandomAccessFile(file, "r"));
    }

    @Test
    public void testRead() throws Exception {
        Assert.assertEquals('0', in.read());
        byte[] b = new byte[3];
        Assert.assertEquals(3, in.read(b));
        Assert.assertEquals("123", new String(b));
    }

    @Test
    public void testSkip() throws Exception {
        Assert.assertEquals('0', in.read());
        in.skip(5);
        Assert.assertEquals('6', in.read());
    }

    @Test
    public void testSkipSkip() throws Exception {
        in.skip(2);
        in.skip(3);
        Assert.assertEquals('5', in.read());
    }

    @Test
    public void testSkipReadSkip() throws Exception {
        in.skip(2);
        Assert.assertEquals('2', in.read());
        in.skip(2);
        Assert.assertEquals('5', in.read());
    }

    @Test
    public void testResetSkipReadSkip() throws Exception {
        in.skip(2);
        assertEquals('2', in.read());
        in.skip(2);
        assertEquals('5', in.read());
        in.reset();
        assertEquals('0', in.read());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSkipOutsideFile() throws Exception {
        File file = createTempFile("offset", ".dat");
        file.deleteOnExit();
        writeStringToFile(file, "0123456789");
        InputStream in = new RandomAccessFileBufferedInputStream(file);
        assertEquals(10, in.skip(20));
    }

}
