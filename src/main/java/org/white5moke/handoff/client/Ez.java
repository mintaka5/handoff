package org.white5moke.handoff.client;

import io.leonard.Base58;

import java.util.zip.DataFormatException;

/**
 * need a good way to display strings of keys
 * as well as be able to convert into usable keys
 */
public class Ez {
    /**
     * moves bytes in and out of compression
     */
    private Compressor5 compressor;

    public Ez() {
        setCompressor(new Compressor5());
    }

    public static Ez getInstance() {
        return new Ez();
    }

    /**
     * makes bytes easier to look at
     * @param stuff
     * @return
     */
    public String ez(byte[] stuff) {
        return Base58.encode(compressor.compress(stuff));
    }

    /**
     * makes previously converted ez strings into bytes
     * @param stuff
     * @return
     * @throws DataFormatException
     */
    public byte[] notEz(String stuff) throws DataFormatException {
        return compressor.decompress(Base58.decode(stuff));
    }

    public Compressor5 getCompressor() {
        return compressor;
    }

    public void setCompressor(Compressor5 compressor) {
        this.compressor = compressor;
    }

    public boolean canDecompress(String str) {
        try {
            compressor.decompress(Base58.decode(str));
        } catch (DataFormatException e) {
            return false;
        }

        return true;
    }
}
