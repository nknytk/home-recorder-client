package com.nknytk.home_recorder_client;

/**
 * Created by nknytk on 14/08/10.
 */
public class Common {
    protected static String PrefKey = "home-recorder-client";
    protected static String SettingNames = "home-recorder-setting-names";
    protected static String AddNewSetting = " + Add home network";
    protected static String ForceCheck = "do-event-check-even-you-are-at-home";
    protected static String SToken = "server-side-token";
    protected static String CToken = "client-side-token";
    protected static String Separator = "##########";
    protected static String DigestRepetition = "digest-repetition";
    protected static String DigestAlgorithm = "SHA256";
    protected static String CurrentServerIP = "current-server-ip-addr";
    protected static Integer UDPRetryIntervalMsec = 20000;
    protected static Integer WebServerPort = 8071;

    protected static String join(String str1, String str2) {
        StringBuffer sb = new StringBuffer();
        sb.append(str1);
        sb.append(Common.Separator);
        sb.append(str2);
        return sb.toString();
    }

    protected static String join(String[] strarr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strarr.length; i++) {
            if (i != 0) sb.append(Common.Separator);
            sb.append(strarr[i]);
        }
        return sb.toString();
    }

    protected static String getURL(String ipaddr, String path, String params) {
        StringBuffer sb = new StringBuffer();
        sb.append("http://");
        sb.append(ipaddr);
        sb.append(":");
        sb.append(String.valueOf(WebServerPort));
        sb.append(path);
        if (params != null) {
            sb.append("?");
            sb.append(params);
        }
        return sb.toString();
    }
}
