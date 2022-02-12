package org.white5moke.handoff;


import com.ning.compress.lzf.LZFEncoder;
import io.leonard.Base58;

import java.nio.charset.StandardCharsets;

public class App {
    public App() {
        //new HandoffWindow();
        String temp = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBCP3RHRriPoNymguEtgEgRb6fPTRK3k7uzrOK2pzBTIw==";
        byte[] tmpBs = temp.getBytes(StandardCharsets.UTF_8);
        byte[] compressed =
        System.out.println(Base58.encode(compressed));
    }

    public static void main(String... args) throws Exception {
        App app = new App();
    }
}