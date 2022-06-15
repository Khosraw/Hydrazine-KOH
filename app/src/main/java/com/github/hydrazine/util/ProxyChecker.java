package com.github.hydrazine.util;

import com.github.steveice10.packetlib.ProxyInfo;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public class ProxyChecker {

    public static boolean checkAuthProxy(ProxyInfo p) {
        InetSocketAddress addr = (InetSocketAddress) p.getAddress();
        System.setProperty("https.proxyHost", addr.getHostName());
        System.setProperty("https.proxyPort", String.valueOf(addr.getPort()));

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com").openConnection();
            connection.connect();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkSocksProxy(ProxyInfo p) {
        InetSocketAddress addr = (InetSocketAddress) p.getAddress();
        System.setProperty("proxySet", "true");
        System.setProperty("socksProxyHost", addr.getHostName());
        System.setProperty("socksProxyPort", String.valueOf(addr.getPort()));

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com").openConnection();
            connection.connect();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
