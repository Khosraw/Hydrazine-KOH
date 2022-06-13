package com.github.hydrazine.module.builtin;

import java.io.File;
import java.net.Proxy;
import java.util.Objects;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.data.game.values.MessageType;
import org.spacehq.mc.protocol.data.message.TranslationMessage;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

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
			
			MinecraftProtocol protocol = new MinecraftProtocol(username);
			
			Client client = ConnectionHelper.connect(protocol, server);
			
			registerListeners(client);
			
			while(client.getSession().isConnected())
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
				client.getSession().disconnect("");
				
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
				
				while(client.getSession().isConnected())
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
			Client client;
			MinecraftProtocol protocol;
			
			// Check if auth proxy should be used
			if(Hydrazine.settings.hasSetting("authproxy"))
			{
				Proxy proxy = Authenticator.getAuthProxy();

				assert creds != null;
				protocol = auth.authenticate(creds, proxy);

			}
			else
			{
				assert creds != null;
				protocol = auth.authenticate(creds);

			}
			client = ConnectionHelper.connect(protocol, server);

			registerListeners(client);
			
			while(client.getSession().isConnected())
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
				client.getSession().disconnect("");
				
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
				
				while(client.getSession().isConnected())
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
	private void registerListeners(Client client)
	{
		client.getSession().addListener(new SessionAdapter() 
		{
			@Override
			public void packetReceived(PacketReceivedEvent event) 
			{
				if(event.getPacket() instanceof ServerJoinGamePacket) 
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
							
							client.getSession().send(new ClientChatPacket(settings.getProperty("registerCommand")));
							
							// Sleep because there may be a command cooldown
							try 
							{
								Thread.sleep(Integer.parseInt(settings.getProperty("commandDelay")));
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							
							client.getSession().send(new ClientChatPacket(settings.getProperty("loginCommand")));
						}
				    }                    
				}
				else if(event.getPacket() instanceof ServerChatPacket)
				{
					ServerChatPacket packet = ((ServerChatPacket) event.getPacket());
					
					// Check if message is a chat message
					if(packet.getType() != MessageType.NOTIFICATION)
					{         
						if(settings.containsKey("filterColorCodes") && settings.getProperty("filterColorCodes").equals("true"))
						{
							String line = packet.getMessage().getFullText();
							
							if(packet.getMessage() instanceof TranslationMessage)
							{
								TranslationMessage msg = (TranslationMessage) packet.getMessage();
								
								String message = "";
								
								if(msg.getTranslationKey().startsWith("chat.type"))
								{
									message = String.format("<%s> %s", (Object[]) msg.getTranslationParams());
								}
								else if(msg.getTranslationKey().equals("commands.message.display.incoming"))
								{
									message = String.format("[PM] <%s> %s", (Object[]) msg.getTranslationParams());					
								}
								else if(msg.getTranslationKey().startsWith("multiplayer.player"))
								{
									if(msg.getTranslationKey().endsWith("left"))
									{
										message = String.format("%s left the game.", (Object[]) msg.getTranslationParams());							
									}
									else if(msg.getTranslationKey().endsWith("joined"))
									{
										message = String.format("%s joined the game.", (Object[]) msg.getTranslationParams());	
									}
								}
								
								if(!message.equals(""))
								{
									line = message;
								}
							}
															                		
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
							if(packet.getMessage() instanceof TranslationMessage)
							{
								TranslationMessage msg = (TranslationMessage) packet.getMessage();
																
								if(msg.getTranslationKey().startsWith("chat.type"))
								{
									String message = String.format("<%s> %s", (Object[]) msg.getTranslationParams());
									
									System.out.println(message);
								}
								else if(msg.getTranslationKey().equals("commands.message.display.incoming"))
								{
									String message = String.format("[PM] <%s> %s", (Object[]) msg.getTranslationParams());
									
									System.out.println(message);
								}
								else if(msg.getTranslationKey().startsWith("multiplayer.player"))
								{
									if(msg.getTranslationKey().endsWith("left"))
									{
										String message = String.format("%s left the game.", (Object[]) msg.getTranslationParams());
										
										System.out.println(message);
									}
									else if(msg.getTranslationKey().endsWith("joined"))
									{
										String message = String.format("%s joined the game.", (Object[]) msg.getTranslationParams());
										
										System.out.println(message);
									}
								}
							}
							else
							{
								System.out.println(packet.getMessage().getFullText());
							}
						}
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
