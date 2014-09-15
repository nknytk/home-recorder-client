package com.nknytk.home_recorder_client;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nknytk on 14/08/14.
 */
public class DigestMaker {
    public static byte[] repetitiveDigest(byte[] msg, Integer repetition) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance(Common.DigestAlgorithm);
            byte[] digest = msg;
            for (int i = 0; i < repetition; i++) {
                msgDigest.reset();
                msgDigest.update(digest);
                digest = msgDigest.digest();
            }
            return digest;

        } catch (NoSuchAlgorithmException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }
}
