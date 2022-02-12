package org.white5moke.handoff.client;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compressor5 {
    private Inflater unzip;
    private Deflater zip;
    private int originalSize = 0;

    public static final int COMPRESSION_SIZE = 16;

    public Compressor5() {
        setUnzip(new Inflater());
        setZip(new Deflater());
    }

    public byte[] compress(byte[] data) {
        // don't bother with small ones
        if(data.length <= COMPRESSION_SIZE) return data;

        setOriginalSize(data.length);
        getZip().setInput(data);
        getZip().finish();
        byte[] out = new byte[COMPRESSION_SIZE];
        getZip().deflate(out);

        return out;
    }

    public byte[] decompress(byte[] data) throws DataFormatException {
        getUnzip().setInput(data, 0, COMPRESSION_SIZE);
        int origSize = (getOriginalSize() > COMPRESSION_SIZE) ? getOriginalSize() : 65000;
        byte[] res = new byte[origSize];
        getUnzip().inflate(res);

        return res;
    }

    public Inflater getUnzip() {
        return unzip;
    }

    public void setUnzip(Inflater unzip) {
        this.unzip = unzip;
    }

    public Deflater getZip() {
        return zip;
    }

    public void setZip(Deflater zip) {
        this.zip = zip;
    }

    public int getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(int originalSize) {
        this.originalSize = originalSize;
    }
}
