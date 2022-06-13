package com.github.hydrazine.module.builtin;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.Objects;
import java.util.Scanner;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.Session;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;
import com.github.hydrazine.util.OperatingSystem;

/**
 * 
 * @author xTACTIXzZ
 * 
 * Connects a client to a server and lets you send chat messages.
 *
 */
public class ChatModule implements Module
{	
	// Create new file where the configuration will be stored (Same folder as jar file)
	private final File configFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private final ModuleSettings settings = new ModuleSettings(configFile);
	
	private final Scanner sc = new Scanner(System.in);
	
	@Override
	public String getModuleName() 
	{
		return "chat";
	}

	@Override
	public String getDescription() 
	{
		return "Lets you chat on a server.";
	}

	@Override
	public void start()
	{
		// Load settings
		settings.load();
		
		if(!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null)
		{
			System.out.println(Hydrazine.errorPrefix + "You have to specify a server to attack (-h)");
			
			return;
		}
		
		System.out.println(Hydrazine.infoPrefix + "Starting module '" + getModuleName() + "'. Press CTRL + C to exit.");
		
		System.out.println(Hydrazine.infoPrefix + "Note: You can send a message x amount of times by adding a '%x' to the message. (Without the quotes)");
		
		Authenticator auth = new Authenticator();
		Server server = new Server(Hydrazine.settings.getSetting("host"), Integer.parseInt(Hydrazine.settings.getSetting("port")));
		
		// Server has offline mode enabled
		if(Hydrazine.settings.hasSetting("username") || Hydrazine.settings.hasSetting("genuser"))
		{
			String username = Authenticator.getUsername();

			assert username != null;
			MinecraftProtocol protocol = new MinecraftProtocol(username);
			
			Session client = ConnectionHelper.connect(protocol, server);
			
			registerListeners(client);
			
			while(client.isConnected())
			{
				doStuff(client, sc);
			}			
			
			sc.close();
		}
		// Server has offline mode disabled
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials creds = Authenticator.getCredentials();
			Session client;
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = Authenticator.getAuthProxy();

				assert creds != null;
				MinecraftProtocol protocol = auth.authenticate(creds, proxy);
				
				client = ConnectionHelper.connect(protocol, server);
			}
			else
			{
				assert creds != null;
				MinecraftProtocol protocol = auth.authenticate(creds);
				
				client = ConnectionHelper.connect(protocol, server);
			}
			
			registerListeners(client);
			
			while(client.isConnected())
			{
				doStuff(client, sc);
			}			
			
			sc.close();
		}
		// User forgot to pass the options
		else
		{
			System.out.println(Hydrazine.errorPrefix + "No client option specified. You have to append one of those switches to the command: -u, -gu or -cr");
		}
	}

	@Override
	public void stop(String cause)
	{
		if(!settings.getProperty("reconnect").equals("false"))
		{
			System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
				
			StringBuilder s = null;
			for(String a : Hydrazine.arguments)
			{
				if(s == null)
				{
					s = new StringBuilder(a);
				}
				else
				{
					s.append(" ").append(a);
				}						
			}
			
			try
			{
				Process p = null;
				
				if(OperatingSystem.isWindows(System.getProperty("os.name")))
				{
					p = Runtime.getRuntime().exec("cmd /c start cmd.exe /k java -jar Hydrazine.jar " + s);
					
					System.out.println("Operating System: " + System.getProperty("os.name"));
				}
				else if(OperatingSystem.isLinux(System.getProperty("os.name")))
				{
					p = Runtime.getRuntime().exec("xterm " + "-hold " + "-e " + "java " + "-jar " + "Hydrazine.jar " + s);
					
					System.out.println("Operating System: " + System.getProperty("os.name"));
				}
				else
				{
					System.out.println("Reconnect has not been implemented on macOS yet.");
				}
				
				try 
				{
					assert p != null;
					p.waitFor();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				System.exit(0);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.exit(0);
			
			System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
		}
	}

	@Override
	public void configure() 
	{		
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			boolean success = settings.createConfigFile();
			
			if(!success)
			{
				return;
			}
		}
				
		boolean answer = ModuleSettings.askUserYesNo("Send automated message?");
		settings.setProperty("automatedMessages", String.valueOf(answer));
		
		if(answer)
		{
			settings.setProperty("message", ModuleSettings.askUser("Message to send: "));
		}
		
		settings.setProperty("sendDelay", ModuleSettings.askUser("Delay between sending messages: "));
		settings.setProperty("reconnect", String.valueOf(ModuleSettings.askUserYesNo("Reconnect automatically?")));
		
		// Store configuration variables
		settings.store();
	}
	
	private void sendAutomatedMessage(Session client, int sendDelay, String msg)
	{
		try 
		{
			Thread.sleep(sendDelay);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		client.send(new ClientboundChatPacket(msg));
		System.out.print(".");
	}
	
	private void registerListeners(Session client)
	{
		client.addListener(new SessionAdapter() 
		{
			@Override
			public void disconnected(DisconnectedEvent event) 
			{
				if(settings.getProperty("reconnect").equals("false"))
				{
					sc.close();
					
					stop(event.getReason());
				}
				else
				{		
					stop(event.getReason());
				}
			}
		});
	}
	
	/*
	 * Does all the input and chatting
	 */
	private void doStuff(Session client, Scanner sc)
	{					
		int sendDelay = 1000;
		boolean automatedMessage = false;
		String msg = null;
		
		if(configFile.exists())
		{
			try
			{
				sendDelay = Integer.parseInt(settings.getProperty("sendDelay"));
				automatedMessage = Boolean.parseBoolean(settings.getProperty("automatedMessages"));
				
				if(automatedMessage)
				{
					msg = settings.getProperty("message");
				}
					
			}
			catch(Exception e)
			{
				System.out.println(Hydrazine.errorPrefix + "Invalid value in configuration file. Reconfigure the module.");				
			}
		}
		
		if(automatedMessage)
		{
			sendAutomatedMessage(client, sendDelay, msg);
		}
		else
		{
			System.out.print(Hydrazine.inputPrefix);

			String line = sc.nextLine();
				
			int sendTime = 1;
			
			if(line.contains("%"))
			{
				int index = line.indexOf("%");
				String end = line.substring(index);
				String amount = end.replaceFirst("%", "");
							
				try
				{
					sendTime = Integer.parseInt(amount);
				}
				catch(Exception e)
				{
					//Either %x not at the end of line
					//Or x is not a number
				}
				
				// Remove "%x" from line
				line = line.substring(0, index);
				line = line.replaceAll("%", "");
			}
			
			for(int i = 0; i < sendTime; i++)
			{
				client.send(new ClientboundChatPacket(line));
				
				try 
				{
					Thread.sleep(sendDelay);
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run()
	{
		start();
	}

}
