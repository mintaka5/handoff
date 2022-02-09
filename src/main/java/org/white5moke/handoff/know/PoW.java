package org.white5moke.handoff.know;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * PoW: Simple proof-of-work
 */
public class PoW {
    private int bitsNeeded = 0;
    private int nonce = 0;
    private long timestamp;
    private String hash;

    /**
     * Does it all.
     * @param msg Any random array of data
     * @param numZeroes How many bits to work for?
     */
    public PoW(byte[] msg, int numZeroes) throws NoSuchAlgorithmException {
        setBitsNeeded(numZeroes);
        setTimestamp(Instant.now().toEpochMilli());

        work(msg);

        //System.out.println("Work factor " + nonce);
    }

    /**
     * Do the work. Get a job!
     * @param msg Data
     */
    private void work(byte[] msg) throws NoSuchAlgorithmException {
        String prefix = StringUtils.repeat("0", bitsNeeded);

        String hexMsg = Hex.encodeHexString(msg);
        hash = DigestUtils.sha256Hex(reMessage(hexMsg));
        setHash(hash);

        while(!getHash().startsWith(prefix)) {
            setTimestamp(Instant.now().toEpochMilli());
            nonce += 1;

            setHash(DigestUtils.sha256Hex(reMessage(hexMsg)));
        }
    }

    /**
     * Helper string message repackager
     * @param msg Message characters
     * @return String
     */
    private String reMessage(String msg) {
        StringBuilder sb = new StringBuilder(String.valueOf(timestamp));
        sb.append(msg);
        sb.append(nonce);

        return sb.toString();
    }

    @Override
    public String toString() {
        return getHash();
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        j.put("hash", getHash());
        j.put("work", getNonce());
        j.put("time", getTimestamp());
        j.put("bits", getBitsNeeded());
        return j;
    }

    /*public static void main(String[] args) throws NoSuchAlgorithmException {
        PoW pow1 = new PoW("0000000Hello worlds this is kitty mew mew!".getBytes(StandardCharsets.UTF_8), 4);
        PoW pow2 = new PoW("RAWR me hangry value is soul".getBytes(StandardCharsets.UTF_8), 3);
    }*/

    public int getBitsNeeded() {
        return bitsNeeded;
    }

    public void setBitsNeeded(int bitsNeeded) {
        this.bitsNeeded = bitsNeeded;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}