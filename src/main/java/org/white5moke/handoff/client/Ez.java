package org.white5moke.handoff.client;

import io.leonard.Base58;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Ez {
    public static byte[] compress(byte[] stuff) throws IOException {
        Deflater comp = new Deflater();
        comp.setLevel(Deflater.BEST_COMPRESSION);
        comp.deflate(stuff);

        return stuff;
    }

    public static byte[] decompress(byte[] stuff) throws DataFormatException {
        Inflater decomp = new Inflater();
        byte[] ns = stuff;
        decomp.inflate(ns);

        return ns;
    }

    public static String ez(byte[] stuff) throws IOException {
        return Base58.encode(compress(stuff));
    }

    public static byte[] notEz(String stuff) throws DataFormatException {
        return decompress(Base58.decode(stuff));
    }
}
