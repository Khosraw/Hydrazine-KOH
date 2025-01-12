package com.github.hydrazine.module.builtin;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.FileFactory;
import com.github.hydrazine.util.ProxyChecker;
import com.github.steveice10.packetlib.ProxyInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author xTACTIXzZ
 * <p>
 * This module steals the skin of a minecraft player
 */
public class ProxyCheckerModule implements Module {
    // Create new file where the configuration will be stored (Same folder as jar file)
    private final File configFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath() + ".module_" + getModuleName() + ".conf");

    // Configuration settings are stored in here
    private final ModuleSettings settings = new ModuleSettings(configFile);

    // Output File
    private File outputFile;

    @Override
    public String getModuleName() {
        return "proxychecker";
    }

    @Override
    public String getDescription() {
        return "Checks the online status of the proxies supplied by '-ap' or '-sp'.";
    }

    @Override
    public void start() {
        if (!configFile.exists()) {
            settings.createConfigFile();
        }

        settings.load();

        if (Hydrazine.settings.hasSetting("authproxy")) {
            if (Hydrazine.settings.getSetting("authproxy").contains(":")) {
                ProxyInfo p = Authenticator.getAuthProxy();
                assert p != null;
                boolean isOnline = ProxyChecker.checkAuthProxy(p);
                InetSocketAddress addr = (InetSocketAddress) p.getAddress();

                if (isOnline)
                    System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
                else
                    System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");

                if (settings.containsKey("outputFile")) {
                    BufferedWriter w = null;
                    outputFile = new File(settings.getProperty("outputFile"));

                    try {
                        w = new BufferedWriter(new FileWriter(outputFile, true));
                    } catch (IOException e) {
                        e.printStackTrace();

                        System.exit(1);
                    }

                    try {
                        w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
                        w.newLine();
                        w.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
                }
            } else {
                File authFile = new File(Hydrazine.settings.getSetting("authproxy"));

                if (authFile.exists()) {
                    BufferedWriter w = null;
                    FileFactory authFactory = new FileFactory(authFile);
                    ProxyInfo[] proxies = authFactory.getProxies(ProxyInfo.Type.HTTP);

                    if (settings.containsKey("outputFile")) {
                        outputFile = new File(settings.getProperty("outputFile"));

                        try {
                            w = new BufferedWriter(new FileWriter(outputFile, true));
                        } catch (IOException e) {
                            e.printStackTrace();

                            System.exit(1);
                        }
                    }

                    assert proxies != null;
                    for (ProxyInfo p : proxies) {
                        boolean isOnline = ProxyChecker.checkAuthProxy(p);
                        InetSocketAddress addr = (InetSocketAddress) p.getAddress();

                        if (isOnline)
                            System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
                        else
                            System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");

                        if (settings.containsKey("outputFile")) {
                            try {
                                assert w != null;
                                w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
                                w.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
                        }
                    }

                    if (w != null) {
                        try {
                            w.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println(Hydrazine.errorPrefix + "Invalid value for switch '-ap'");
                }
            }
        } else if (Hydrazine.settings.hasSetting("socksproxy")) {
            if (Hydrazine.settings.getSetting("socksproxy").contains(":")) {
                ProxyInfo p = new ProxyInfo(ProxyInfo.Type.SOCKS5, new InetSocketAddress(Hydrazine.settings.getSetting("socksproxy").split(":")[0], Integer.parseInt(Hydrazine.settings.getSetting("socksproxy").split(":")[1])));
                boolean isOnline = ProxyChecker.checkSocksProxy(p);
                InetSocketAddress addr = (InetSocketAddress) p.getAddress();

                if (isOnline)
                    System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
                else
                    System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");

                if (settings.containsKey("outputFile")) {
                    BufferedWriter w = null;
                    outputFile = new File(settings.getProperty("outputFile"));

                    try {
                        w = new BufferedWriter(new FileWriter(outputFile, true));
                    } catch (IOException e) {
                        e.printStackTrace();

                        System.exit(1);
                    }

                    try {
                        w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
                        w.newLine();
                        w.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
                }
            } else {
                File socksFile = new File(Hydrazine.settings.getSetting("socksproxy"));

                if (socksFile.exists()) {
                    BufferedWriter w = null;
                    FileFactory socksFactory = new FileFactory(socksFile);
                    ProxyInfo[] proxies = socksFactory.getProxies(ProxyInfo.Type.SOCKS5);

                    if (settings.containsKey("outputFile")) {
                        outputFile = new File(settings.getProperty("outputFile"));

                        try {
                            w = new BufferedWriter(new FileWriter(outputFile, true));
                        } catch (IOException e) {
                            e.printStackTrace();

                            System.exit(1);
                        }
                    }

                    assert proxies != null;
                    for (ProxyInfo p : proxies) {
                        boolean isOnline = ProxyChecker.checkSocksProxy(p);
                        InetSocketAddress addr = (InetSocketAddress) p.getAddress();

                        if (isOnline)
                            System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is working");
                        else
                            System.out.println(Hydrazine.infoPrefix + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " doesn't work");

                        if (settings.containsKey("outputFile")) {
                            try {
                                assert w != null;
                                w.write(addr.getAddress().getHostAddress() + ":" + addr.getPort());
                                w.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.out.println(Hydrazine.infoPrefix + "Saved working proxies to: " + outputFile.getAbsolutePath());
                        }
                    }

                    if (w != null) {
                        try {
                            w.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println(Hydrazine.errorPrefix + "Invalid value for switch '-sp'");
                }
            }
        } else {
            System.out.println(Hydrazine.errorPrefix + "Missing proxy option (-ap or -sp)");
        }
    }

    @Override
    public void stop(String cause) {
        System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);

        System.exit(0);
    }

    @Override
    public void configure() {
        String answer = ModuleSettings.askUser("Output file:");

        if (!(answer.isEmpty())) {
            settings.setProperty("outputFile", answer);
        } else {
            settings.remove("outputFile");
        }

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

    @Override
    public void run() {
        start();
    }

}
