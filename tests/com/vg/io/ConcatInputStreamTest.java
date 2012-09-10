package com.vg.io;

import static com.vg.io.ConcatInputStream.concat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ConcatInputStreamTest {
    @Test
    public void testRead() throws Exception {
        ConcatInputStream in = concat(seekin("123"), seekin("456"));
        int r = 0;
        for (int i = 0; i < 4; i++) {
            r = in.read();
            assertTrue(r != -1);
        }
        assertEquals('4', r);
    }

    @Test
    public void testBlockRead() throws Exception {
        ConcatInputStream in = concat(seekin("123"), seekin("456"), seekin("789"));
        assertEquals(9, in.length());
        byte[] b = new byte[7];
        assertEquals(7, in.read(b));
        assertEquals("1234567", new String(b));
    }

    @Test
    public void testReadAll() throws Exception {
        SeekableInputStream a = chunkin(3, 5, seekin("012hello"));
        SeekableInputStream b = chunkin(2, 6, seekin("01 world"));
        ConcatInputStream in = concat(a, b);
        byte[] byteArray = IOUtils.toByteArray(in);
        assertEquals("hello world", new String(byteArray));
    }

    private SeekableInputStream chunkin(int offset, int size, SeekableInputStream seekin) throws IOException {
        return new SeekableLimitedInputStream(seekin, offset, size);
    }

    @Test
    public void testReadAllSeek() throws Exception {
        SeekableInputStream a = chunkin(3, 5, seekin("012hello"));
        SeekableInputStream b = chunkin(2, 6, seekin("01 world"));
        ConcatInputStream in = concat(a, b);
        byte[] byteArray = IOUtils.toByteArray(in);
        assertEquals("hello world", new String(byteArray));
        in.seek(1);
        assertEquals('e', in.read());
    }

    @Test
    public void testFastChunkSeek() throws Exception {
        SeekableInputStream a = chunkin(3, 5, seekin("012hello"));
        SeekableInputStream b = chunkin(2, 6, seekin("01 world"));
        ConcatInputStream in = concat(a, b);
        in.seek(6);
        assertEquals('w', in.read());
    }

    public static SeekableInputStream seekin(String string) {
        return new SeekableByteArrayInputStream(string.getBytes());
    }
}
