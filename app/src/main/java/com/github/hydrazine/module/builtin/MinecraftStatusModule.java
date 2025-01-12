package com.github.hydrazine.module.builtin;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.module.Module;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author xTACTIXzZ
 * <p>
 * This module retrieves the status of all minecraft related services
 */
public class MinecraftStatusModule implements Module {

    @Override
    public String getModuleName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Retrieves the status of all minecraft related services";
    }

    @Override
    public void start() {
        System.out.println(Hydrazine.infoPrefix + "Getting status from https://status.mojang.com/check ...");
        URL url;

        try {
            url = new URL("https://status.mojang.com/check");

            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = br.readLine();

            String[] parts = inputLine.split(","); // 8 parts

            for (String part : parts) {
                String info = part;
                info = info.replace("[", "");
                info = info.replace("]", "");
                info = info.replace("{", "");
                info = info.replace("}", "");
                info = info.replace("\"", "");
                info = info.replace(" ", "");

                String[] subParts = info.split(":"); // 2 parts
                String service = subParts[0];
                String status = subParts[1];

                String ANSI_RESET = "\u001B[0m";
                if (status.equalsIgnoreCase("red")) {
                    if (isUnix()) {
                        String ANSI_RED = "\u001B[31m";
                        status = ANSI_RED + "unavailable" + ANSI_RESET;
                    } else {
                        status = "unavailable";
                    }
                } else if (status.equalsIgnoreCase("yellow")) {
                    if (isUnix()) {
                        String ANSI_YELLOW = "\u001B[33m";
                        status = ANSI_YELLOW + "some issues" + ANSI_RESET;
                    } else {
                        status = "some issues";
                    }
                } else if (status.equalsIgnoreCase("green")) {
                    if (isUnix()) {
                        String ANSI_GREEN = "\u001B[32m";
                        status = ANSI_GREEN + "no issues" + ANSI_RESET;
                    } else {
                        status = "no issues";
                    }
                }

                System.out.println("- [" + service + "]:	" + status);
            }

            br.close();
        } catch (Exception e) {
            stop(e.toString());
        }
    }

    @Override
    public void stop(String cause) {
        System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);

        System.exit(0);
    }

    @Override
    public void configure() {
        System.out.println(Hydrazine.infoPrefix + "This module can't be configured.");
    }

    private boolean isUnix() {
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        return (operatingSystem.contains("nix") || operatingSystem.contains("nux") || operatingSystem.contains("aix"));
    }

    @Override
    public void run() {
        start();
    }

}
