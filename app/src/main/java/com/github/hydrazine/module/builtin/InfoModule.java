package com.github.hydrazine.module.builtin;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.packetlib.Session;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This module retrieves information of a minecraft server
 *
 */
public class InfoModule implements Module
{

	@Override
	public String getModuleName() 
	{
		return "info";
	}

	@Override
	public String getDescription() 
	{
		return "Retrieves information about a minecraft server.";
	}
	
	@Override
	public void start() 
	{
		if(!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null)
		{
			System.out.println(Hydrazine.errorPrefix + "You have to specify a server to get the information from (-h)");
			System.exit(1);
		}
		
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		MinecraftProtocol protocol = new MinecraftProtocol(SubProtocol.STATUS);
        Session client = new Session(server.getHost(), server.getPort(), protocol, new TcpClientSession(server.getHost(), server.getPort(), protocol, new ProxyInfo(ProxyInfo.Type.SOCKS5, new InetSocketAddress(Integer.parseInt(Hydrazine.settings.getSetting("port"))))));
                                
        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (ServerInfoHandler) (session, info) -> {
			System.out.println("Version: " + info.getVersionInfo().getVersionName() + " (" + info.getVersionInfo().getProtocolVersion() + ")");
			System.out.println("Player count: " + info.getPlayerInfo().getOnlinePlayers() + "/" + info.getPlayerInfo().getMaxPlayers());
			System.out.println("Description: " + info.getDescription());
			System.out.println("Icon: " + Arrays.toString(info.getIconPng()));
		});

        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (ServerInfoHandler) (session, info) -> {
					System.out.println("Version: " + info.getVersionInfo().getVersionName() + " (" + info.getVersionInfo().getProtocolVersion() + ")");
					System.out.println("Player count: " + info.getPlayerInfo().getOnlinePlayers() + "/" + info.getPlayerInfo().getMaxPlayers());
					System.out.println("Description: " + info.getDescription());
					System.out.println("Icon: " + Arrays.toString(info.getIconPng()));
				}
        );

        client.connect();

		int hasRetrieved = 0;
		while(hasRetrieved != 2)
        {
            try 
            {
                Thread.sleep(5);
            } 
            catch(InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
        
        client.disconnect(Hydrazine.infoPrefix + "Retrieved server information.");
	}

	@Override
	public void stop(String cause)
	{
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
		
		System.exit(0);
	}

	@Override
	public void configure() 
	{
		System.out.println(Hydrazine.infoPrefix + "This module can't be configured.");
	}

	@Override
	public void run()
	{
		start();
	}
}
