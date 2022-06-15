package com.github.hydrazine.module.builtin;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;
import com.github.hydrazine.util.OperatingSystem;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author xTACTIXzZ
 * <p>
 * Combination of 'chat' and 'readchat' module
 */
public class ConsoleClientModule implements Module {

    // Create new file where the configuration will be stored (Same folder as jar file)
    private final File configFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath() + ".module_" + getModuleName() + ".conf");

    // Configuration settings are stored in here
    private final ModuleSettings settings = new ModuleSettings(configFile);

    private final Scanner sc = new Scanner(System.in);

    @Override
    public String getModuleName() {
        return "cclient";
    }

    @Override
    public String getDescription() {
        return "This module lets you send and receive chat messages.";
    }

    @Override
    public void start() {
        // Load settings
        settings.load();

        if (!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null) {
            System.out.println(Hydrazine.errorPrefix + "You have to specify a server to attack (-h)");

            System.exit(1);
        }

        System.out.println(Hydrazine.infoPrefix + "Starting module '" + getModuleName() + "'. Press CTRL + C to exit.");

        System.out.println(Hydrazine.infoPrefix + "Note: You can send a message x amount of times by adding a '%x' to the message. (Without the quotes)");

        Authenticator auth = new Authenticator();
        Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));

        // Server has offline mode enabled
        if (Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser")) {
            String username = Authenticator.getUsername();

            assert username != null;
            MinecraftProtocol protocol = new MinecraftProtocol(username);

            Session client = ConnectionHelper.connect(protocol, server);

            registerListeners(client);

            while (client.isConnected()) {
                doStuff(client, sc);
            }

            sc.close();
        }
        // Server has offline mode disabled
        else if (Hydrazine.settings.hasSetting("credentials")) {
            Credentials creds = Authenticator.getCredentials();
            Session client;

            // Check if auth proxy should be used
            if (Hydrazine.settings.hasSetting("authproxy")) {
                ProxyInfo proxy = Authenticator.getAuthProxy();

                assert creds != null;
                MinecraftProtocol protocol = auth.authenticate(proxy);

                client = ConnectionHelper.connect(protocol, server);
            } else {
                assert creds != null;
                MinecraftProtocol protocol = auth.authenticate();

                client = ConnectionHelper.connect(protocol, server);
            }

            registerListeners(client);

            while (client.isConnected()) {
                doStuff(client, sc);
            }

            sc.close();
        }
        // User forgot to pass the options
        else {
            System.out.println(Hydrazine.errorPrefix + "No client option specified. You have to append one of those switches to the command: -u, -gu or -cr");
        }
    }

    @Override
    public void stop(String cause) {
        if (!settings.getProperty("reconnect").equals("false")) {
            System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);

            StringBuilder s = null;
            for (String a : Hydrazine.arguments) {
                if (s == null) {
                    s = new StringBuilder(a);
                } else {
                    s.append(" ").append(a);
                }
            }

            try {
                Process p = null;

                if (OperatingSystem.isWindows(System.getProperty("os.name"))) {
                    p = Runtime.getRuntime().exec("cmd /c start cmd.exe /k java -jar Hydrazine.jar " + s);

                    System.out.println("Operating System: " + System.getProperty("os.name"));
                } else if (OperatingSystem.isLinux(System.getProperty("os.name"))) {
                    p = Runtime.getRuntime().exec("xterm " + "-hold " + "-e " + "java " + "-jar " + "Hydrazine.jar " + s);

                    System.out.println("Operating System: " + System.getProperty("os.name"));
                } else {
                    System.out.println("Reconnect has not been implemented on macOS yet.");
                }

                try {
                    assert p != null;
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.exit(0);

            System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
        }
    }

    @Override
    public void configure() {
        settings.setProperty("sendDelay", ModuleSettings.askUser("Delay between sending messages: "));
        settings.setProperty("filterColorCodes", String.valueOf(ModuleSettings.askUserYesNo("Filter color codes?")));
        settings.setProperty("reconnect", String.valueOf(ModuleSettings.askUserYesNo("Reconnect automatically?")));

        // Create configuration file if not existing
        if (!configFile.exists()) {
            boolean success = settings.createConfigFile();

            if (!success) {
                return;
            }
        }

        // Store configuration variables
        settings.store();
    }

    /*
     * Registers the listeners in order to read the chat
     */
    private void registerListeners(Session client) {
        client.addListener(new SessionAdapter() {
            @Override
            public void packetReceived(Session session, Packet packet) {
                if (packet instanceof ServerboundChatPacket) {
                    ServerboundChatPacket chatPacket = ((ServerboundChatPacket) packet);

                    // Check if message is a chat message
                    if (settings.containsKey("filterColorCodes") && settings.getProperty("filterColorCodes").equals("true")) {
                        String line = chatPacket.getMessage();

                        String builder = line;
                        // Filter out color codes
                        if (builder.contains("�")) {
                            int count = builder.length() - builder.replace("�", "").length();

                            for (int i = 0; i < count; i++) {
                                int index = builder.indexOf("�");

                                if (index > (-1)) // Check if index is invalid, happens sometimes.
                                {
                                    String buf = builder.substring(index, index + 2);

                                    builder = builder.replace(buf, "");
                                }
                            }

                            System.out.println(Hydrazine.inputPrefix + builder);
                        } else {
                            System.out.println(Hydrazine.inputPrefix + line);
                        }
                    }
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                if (!settings.getProperty("reconnect").equals("true")) {
                    sc.close();

                    stop(event.getReason());

                    System.exit(0);
                } else {
                    stop(event.getReason());

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    start();
                }
            }
        });
    }

    /*
     * Does all of the chatting related things
     */
    private void doStuff(Session client, Scanner sc) {
        int sendDelay = 1000;

        if (configFile.exists()) {
            try {
                sendDelay = Integer.parseInt(settings.getProperty("sendDelay"));
            } catch (Exception e) {
                System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");
            }
        }

        String line = null;

        while (!sc.hasNextLine()) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (sc.hasNextLine()) {
            line = sc.nextLine();
        }

        int sendTime = 1;

        assert line != null;
        if (line.contains("%")) {
            int index = line.indexOf("%");
            String end = line.substring(index);
            String amount = end.replaceFirst("%", "");

            try {
                sendTime = Integer.parseInt(amount);
            } catch (Exception e) {
                //Either %x not at the end of line
                //Or x is not a number
            }

            // Remove "%x" from line
            line = line.substring(0, index);
            line = line.replaceAll("%", "");
        }

        for (int i = 0; i < sendTime; i++) {
            client.send(new ClientboundChatPacket(line));

            try {
                Thread.sleep(sendDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        start();
    }
}