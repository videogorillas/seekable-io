package com.vg.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class SeekableLimitedInputStreamTest {
    @Test
    public void testStartOffset() throws Exception {
        SeekableByteArrayInputStream bytes = new SeekableByteArrayInputStream(
                new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        SeekableLimitedInputStream limited = new SeekableLimitedInputStream(bytes, 3, 2);
        int r0 = limited.read();
        int r1 = limited.read();
        int r3 = limited.read();
        Assert.assertEquals(3, r0);
        Assert.assertEquals(4, r1);
        Assert.assertEquals(-1, r3);
        limited.close();
    }

    @Test
    public void testRead() throws Exception {
        String string = "1234567890";
        SeekableInputStream in = chunkin(2, 5, string);
        byte[] b = new byte[5];
        Assert.assertEquals(5, in.read(b));
        Assert.assertEquals("34567", new String(b));
    }

    private SeekableInputStream chunkin(long offset, long size, String string) throws IOException {
        return new SeekableLimitedInputStream(seekin(string), offset, size);
        //        return new ChunkInputStream(offset, size, seekin(string));
    }

    @Test
    public void testReadBeyondEnd() throws Exception {
        SeekableInputStream in = chunkin(9, 5, "1234567890");
        byte[] b = new byte[5];
        Assert.assertEquals(1, in.read(b));
        Assert.assertEquals("0", new String(b, 0, 1));
    }

    @Test
    public void testReset() throws Exception {
        SeekableInputStream in = chunkin(2, 5, "1234567890");
        Assert.assertEquals('3', in.read());
        in.reset();
        Assert.assertEquals('3', in.read());
    }

    @Test
    public void testSeek() throws Exception {
        SeekableInputStream in = chunkin(2, 5, "1234567890");
        assertEquals('3', in.read());
        in.seek(2);
        assertEquals('5', in.read());
    }

    public static SeekableInputStream seekin(String string) {
        return new SeekableByteArrayInputStream(string.getBytes());
    }

}
