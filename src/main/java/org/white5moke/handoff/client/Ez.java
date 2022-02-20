package org.white5moke.handoff.client;

import io.leonard.Base58;

/**
 * need a good way to display strings of keys
 * as well as be able to convert into usable keys
 */
public class Ez {
    /**
     * moves bytes in and out of compression
     */

    public Ez() {}

    public static Ez getInstance() {
        return new Ez();
    }

    /**
     * makes bytes easier to look at
     * @param stuff
     * @return
     */
    public String ez(byte[] stuff) {

        return Base58.encode(stuff);
    }

    /**
     * makes previously converted ez strings into bytes
     * @param stuff
     * @return
     */
    public byte[] notEz(String stuff) {
        return Base58.decode(stuff);
    }
}
