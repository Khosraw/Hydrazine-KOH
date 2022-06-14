package com.github.hydrazine.module.builtin;

import java.io.File;
import java.net.Proxy;
import java.util.Objects;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.minecraft.Server;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.ConnectionHelper;

/**
 * 
 * @author xTACTIXzZ
 *
 * Connects a client to a server and reads the chat.
 *
 */
public class ChatReaderModule implements Module
{
	// Create new file where the configuration will be stored (Same folder as jar file)
	private final File configFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here	
	private final ModuleSettings settings = new ModuleSettings(configFile);
	
	@Override
	public String getModuleName() 
	{
		return "readchat";
	}

	@Override
	public String getDescription() 
	{
		return "This module connects to a server and passively reads the chat.";
	}

	@Override
	public void start() 
	{
		// Load settings
		settings.load();
		
		if(!Hydrazine.settings.hasSetting("host") || Hydrazine.settings.getSetting("host") == null)
		{
			System.out.println(Hydrazine.errorPrefix + "You have to specify a server to attack (-h)");
			
			System.exit(1);
		}
		
		System.out.println(Hydrazine.infoPrefix + "Starting module '" + getModuleName() + "'. Press CTRL + C to exit.");
				
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
				try
				{
					Thread.sleep(20);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}	
			
			if(!settings.getProperty("reconnect").equals("false"))
			{
				client.disconnect("");
				
				try 
				{
					Thread.sleep(1500);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				client = ConnectionHelper.connect(protocol, server);
				registerListeners(client);
				
				while(client.isConnected())
				{
					try 
					{
						Thread.sleep(20);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		// Server has offline mode disabled
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
		    Credentials creds = Authenticator.getCredentials();
			Session client;
			MinecraftProtocol protocol;
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = Authenticator.getAuthProxy();

				assert creds != null;
				protocol = auth.authenticate(proxy);

			}
			else
			{
				assert creds != null;
				protocol = auth.authenticate();

			}
			client = ConnectionHelper.connect(protocol, server);

			registerListeners(client);
			
			while(client.isConnected())
			{
				try 
				{
					Thread.sleep(20);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			
			if(!settings.getProperty("reconnect").equals("false"))
			{
				client.disconnect("");
				
				try 
				{
					Thread.sleep(1500);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				client = ConnectionHelper.connect(protocol, server);
				registerListeners(client);
				
				while(client.isConnected())
				{
					try 
					{
						Thread.sleep(20);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
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
		System.out.println(Hydrazine.infoPrefix + "Stopping module " + getModuleName() + ": " + cause);
		
		System.exit(0);
	}

	@Override
	public void configure() 
	{				
		settings.setProperty("registerCommand", ModuleSettings.askUser("Enter register command: "));
		settings.setProperty("loginCommand", ModuleSettings.askUser("Enter login command: "));
		settings.setProperty("commandDelay", ModuleSettings.askUser("Enter the delay between the commands in milliseconds: "));
		settings.setProperty("filterColorCodes", String.valueOf(ModuleSettings.askUserYesNo("Filter color codes?")));
		settings.setProperty("reconnect", String.valueOf(ModuleSettings.askUserYesNo("Reconnect automatically?")));
		
		// Create configuration file if not existing
		if(!configFile.exists())
		{
			boolean success = settings.createConfigFile();
			
			if(!success)
			{
				return;
			}
		}
		
		// Store configuration variables
		settings.store();
	}
	/*
	 * Register listeners
	 */
	private void registerListeners(Session client)
	{
		client.addListener(new SessionAdapter() 
		{
			@Override
			public void packetReceived(Session session, Packet packet) {
				if(packet instanceof ClientboundLoginPacket)
				{
					if(settings.containsKey("loginCommand") && settings.containsKey("registerCommand"))
					{
						if(!(settings.getProperty("loginCommand").isEmpty() && settings.getProperty("registerCommand").isEmpty()))
						{
							// Sleep because there may be a command cooldown
							try 
							{
								Thread.sleep(Integer.parseInt(settings.getProperty("commandDelay")));
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							
							client.send(new ClientboundChatPacket(settings.getProperty("registerCommand")));
							
							// Sleep because there may be a command cooldown
							try 
							{
								Thread.sleep(Integer.parseInt(settings.getProperty("commandDelay")));
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							
							client.send(new ClientboundChatPacket(settings.getProperty("loginCommand")));
						}
				    }                    
				}
				else if(packet instanceof ServerboundChatPacket)
				{
					ServerboundChatPacket chatPacket = ((ServerboundChatPacket) packet);
					
					// Check if message is a chat message
						if(settings.containsKey("filterColorCodes") && settings.getProperty("filterColorCodes").equals("true"))
						{
							String line = chatPacket.getMessage();

							String builder = line;
							// Filter out color codes
							if(builder.contains("�"))
							{
								int count = builder.length() - builder.replace("�", "").length();
								
								for(int i = 0; i < count; i++)
								{
									int index = builder.indexOf("�");
									
									if(index > (-1)) // Check if index is invalid, happens sometimes.
									{		
										String buf = builder.substring(index, index + 2);

										builder = builder.replace(buf, "");
									}
								}
								
								System.out.println(Hydrazine.inputPrefix + builder);
							}
							else
							{
								System.out.println(Hydrazine.inputPrefix + line);
							}
						}
						else
						{
								System.out.println(chatPacket.getMessage());
					}
				}
			}
			
			@Override
			public void disconnected(DisconnectedEvent event) 
			{
				if(settings.getProperty("reconnect").equals("false"))
				{					
					stop(event.getReason());
				}
				else
				{			
					start();
				}
			}
		});
	}

	@Override
	public void run()
	{
		start();
	}
}
