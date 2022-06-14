package com.github.hydrazine.minecraft;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.Random;

import com.github.steveice10.mc.protocol.MinecraftProtocol;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.util.FileFactory;
import com.github.hydrazine.util.UsernameGenerator;

/**
 * 
 * @author xTACTIXzZ
 *
 * This class authenticates the clients
 *
 */
public class Authenticator 
{
	public Authenticator()
	{

	}
	
	/**
	 * Authenticates a client using a proxy
	 * @param proxy The proxy used to authenticate the client
	 * @return A MinecraftProtocol which can be used to create a client object
	 */
	public MinecraftProtocol authenticate(Proxy proxy)
	{
		Authenticator auth = new Authenticator();
		MinecraftProtocol protocol;


		protocol = auth.authenticate(proxy);

		return protocol;
	}
	
	/**
	 * Authenticates a client
	 * @return A MinecraftProtocol which can be used to create a client object
	 */
	public MinecraftProtocol authenticate()
	{
		MinecraftProtocol protocol;
		String username = Authenticator.getUsername();

		assert username != null;
		protocol = new MinecraftProtocol(username);

		return protocol;
	}
	
	/**
	 * @return The auth proxy specified by the user
	 */
	public static Proxy getAuthProxy()
	{
		if(Hydrazine.settings.hasSetting("authproxy"))
		{
			Proxy proxy;
			
			if(Hydrazine.settings.getSetting("authproxy").contains(":"))
			{
				try
				{
					String[] parts = Hydrazine.settings.getSetting("authproxy").split(":");
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
				}
				catch(Exception e)
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch '-ap'");
					
					return null;
				}
			}
			else
			{
				File authFile = new File(Hydrazine.settings.getSetting("authproxy"));
				
				if(authFile.exists())
				{
					Random r = new Random();
					FileFactory authFactory = new FileFactory(authFile);
					proxy = Objects.requireNonNull(authFactory.getProxies(Proxy.Type.HTTP))[r.nextInt(Objects.requireNonNull(authFactory.getProxies(Proxy.Type.HTTP)).length)];
				}
				else
				{
					System.out.println(Hydrazine.errorPrefix + "Invalid value for switch '-ap'");
					
					return null;
				}
			}
			
			return proxy;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return The credentials specified by the user
	 */
	public static Credentials getCredentials()
	{
		if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials creds;
			
			try
			{
				String[] parts = Hydrazine.settings.getSetting("credentials").split(":");
				creds = new Credentials(parts[0], parts[1]);
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value for switch '-cr'");
				
				return null;
			}
			
			return creds;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @return The (generated) username specified by the user
	 */
	public static String getUsername()
	{
		if(Hydrazine.settings.hasSetting("username"))
		{
			return Hydrazine.settings.getSetting("username");
		}
		else if(Hydrazine.settings.hasSetting("genuser"))
		{
			String method = Hydrazine.settings.getSetting("genuser");
			
			UsernameGenerator ug = new UsernameGenerator();
			
			return ug.deliverUsername(method);
		}
		else
		{
			return null;
		}
	}

}
