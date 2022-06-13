package com.github.hydrazine.util;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.Random;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Server;

/**
 * 
 * @author xTACTIXzZ
 * 
 * This class helps connecting a client to a server. It respects the user-specified settings.
 *
 */
public class ConnectionHelper 
{
	
	public ConnectionHelper()
	{
		
	}
	
	/**
	 * Registers default listeners to a client
	 */
	public static void registerDefaultListeners(Session client)
	{
		client.addListener(new SessionAdapter()
		{

			@Override
			public void connected(ConnectedEvent event)
			{
				System.out.println(Hydrazine.infoPrefix + ((MinecraftProtocol) client.getPacketProtocol()).getProfile().getName() + " connected to the server!");
			}
			
			@Override
			public void disconnected(DisconnectedEvent event) 
			{
				System.out.println(Hydrazine.infoPrefix + "Client disconnected: " + event.getReason());  
			}
		});
	}
	
	/**
	 * Connects a client to a server
	 */
	public static Session connect(MinecraftProtocol protocol, Server server)
	{
		// Check if authenticated successfully
		if(protocol == null)
		{
			return null;
		}
		
		// Check if socks proxy should be used
		if(Hydrazine.settings.hasSetting("socksproxy"))
		{
			Proxy proxy;
			ProxyInfo proxyInfo;
			
			if(Hydrazine.settings.getSetting("socksproxy").contains(":"))
			{
				try
				{
					String[] parts = Hydrazine.settings.getSetting("socksproxy").split(":");
					proxyInfo = new ProxyInfo(ProxyInfo.Type.SOCKS5, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
					proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
				}
				catch(Exception e)
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch -sp");
					
					return null;
				}
			}
			else
			{
				File socksFile = new File(Hydrazine.settings.getSetting("socksproxy"));
				
				if(socksFile.exists())
				{
					Random r = new Random();
					FileFactory socksFactory = new FileFactory(socksFile);
					proxyInfo = Objects.requireNonNull(socksFactory.getProxies(Proxy.Type.SOCKS))[r.nextInt(Objects.requireNonNull(socksFactory.getProxies(Proxy.Type.SOCKS5)).length)];
					proxy = Objects.requireNonNull(socksFactory.getProxies(Proxy.Type.SOCKS))[r.nextInt(Objects.requireNonNull(socksFactory.getProxies(Proxy.Type.SOCKS)).length)];
				}
				else
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch -sp");
					
					return null;
				}
			}
			
			Session client = new Session(server.host(), server.port(), protocol, new TcpClientSession(server.host(), server.port(), protocol, proxyInfo);
			
			registerDefaultListeners(client);
						
			client.connect();
			
			return client;
		}
		else
		{
			Session client = new Session(server.host(), server.port(), protocol, new TcpClientSession(server.host(), server.port(), protocol, new ProxyInfo()));
			
			registerDefaultListeners(client);
						
			client.connect();
			
			return client;
		}
	}

}
