package com.yaoxin.appbase.utils;

import android.os.Debug;
import android.os.Process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

/**
 * Dedicated AES key provider for /customer/yunxinId.
 * Assembled at runtime via XOR from split byte segments; no plaintext key in source.
 */
public final class YunxinIdAesKey {

    private static final byte[] P1 = new byte[]{
            0x31, 0x28, 0x33, 0x35, 0x36, 0x3B, 0x3E, 0x33
    };
    private static final byte[] P2 = new byte[]{
            0x29, 0x29, 0x2A, 0x35, 0x36, 0x36, 0x3F, 0x29
    };
    private static final int MASK = 0x5A;

    private static volatile String cached;

    private YunxinIdAesKey() {
    }

    public static String aesKey() {
        exitIfDebuggedInRelease();
        String key = cached;
        if (key != null) {
            return key;
        }
        byte[] out = new byte[16];
        for (int i = 0; i < 8; i++) {
            out[i] = (byte) ((P1[i] & 0xFF) ^ MASK);
            out[8 + i] = (byte) ((P2[i] & 0xFF) ^ MASK);
        }
        key = new String(out, StandardCharsets.UTF_8);
        cached = key;
        return key;
    }

    private static void exitIfDebuggedInRelease() {
        if (AppProxy.getInstance().isDebug()) {
            return;
        }
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger() || hasTracerPid()) {
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    private static boolean hasTracerPid() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/status"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TracerPid:")) {
                    String value = line.substring("TracerPid:".length()).trim();
                    return Integer.parseInt(value) != 0;
                }
            }
        } catch (Throwable ignore) {
        }
        return false;
    }
}
