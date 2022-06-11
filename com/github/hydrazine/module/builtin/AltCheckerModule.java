package com.github.hydrazine.module.builtin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

import org.spacehq.mc.protocol.MinecraftProtocol;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Authenticator;
import com.github.hydrazine.minecraft.Credentials;
import com.github.hydrazine.module.Module;
import com.github.hydrazine.module.ModuleSettings;
import com.github.hydrazine.util.FileFactory;

public class AltCheckerModule implements Module
{

	// Create new file where the configuration will be stored (Same folder as jar file)
	private final File configFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath() + ".module_" + getModuleName() + ".conf");
	
	// Configuration settings are stored in here
	private final ModuleSettings settings = new ModuleSettings(configFile);
	
	// List of valid accounts
	private ArrayList<Credentials> validCredentials;
	
	// Output File
	private File outputFile;
	
	@Override
	public String getModuleName()
	{
		return "altchecker";
	}

	@Override
	public String getDescription()
	{
		return "Cycles through a list of accounts to check if they are able to log in. (Format: username/email:password)";
	}

	@Override
	public void start() 
	{
		if(!configFile.exists())
		{
			settings.createConfigFile();
		}
		
		settings.load();
		
		validCredentials = new ArrayList<>();
		
		Authenticator auth = new Authenticator();
		
		if(Boolean.parseBoolean(settings.getProperty("outputToFile")))
		{
			outputFile = new File(settings.getProperty("outputFile"));
			
			if(!outputFile.exists())
			{
				try
				{
					outputFile.createNewFile();
				}
				catch (IOException e)
				{
					System.out.println(Hydrazine.errorPrefix + "Unable to create config file");
				}
			}
		}
		
		if(settings.containsKey("loadFromFile") && settings.getProperty("loadFromFile").equals("true"))
		{
			File inputFile = new File(settings.getProperty("inputFile"));
			FileFactory factory = new FileFactory(inputFile);
			Credentials[] creds = factory.getCredentials();

			assert creds != null;
			for(Credentials c : creds)
			{
				MinecraftProtocol mp;
				
				if(!Hydrazine.settings.hasSetting("ap"))
				{
					mp = auth.authenticate(c);
				}
				else
				{
					mp = auth.authenticate(c, Authenticator.getAuthProxy());
				}
				
				if(mp != null)
				{
					validCredentials.add(c);
				}
				
				try 
				{
					Thread.sleep(Long.parseLong(settings.getProperty("loginDelay")));
				} 
				catch (NumberFormatException | InterruptedException e)
				{
					stop(e.toString());
				}
			}
			
			System.out.println("\nWorking Accounts:");
			StringBuilder s = new StringBuilder();
			
			for(Credentials c : validCredentials)
			{
				s.append(c.getUsername()).append(":").append(c.getPassword()).append("\n");
				System.out.println(c.getUsername() + ":" + c.getPassword());
			}
			
			if(Boolean.parseBoolean(settings.getProperty("outputToFile")))
			{
				try 
				{
				    Files.write(outputFile.toPath(), s.toString().getBytes(), StandardOpenOption.APPEND);
				    
				    System.out.println("\n" + Hydrazine.infoPrefix + "Saved working accounts to: " + outputFile.getAbsolutePath());
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		else if(Hydrazine.settings.hasSetting("credentials"))
		{
			Credentials c = Authenticator.getCredentials();

			assert c != null;
			MinecraftProtocol mp = auth.authenticate(c);
			
			if(mp != null)
			{
				System.out.println(Hydrazine.infoPrefix + c.getUsername() + ":" + c.getPassword() + " is working");
				
				if(Boolean.parseBoolean(settings.getProperty("outputToFile")))
				{
					String s = c.getUsername() + ":" + c.getPassword();
					
					try 
					{
					    Files.write(outputFile.toPath(), s.getBytes(), StandardOpenOption.APPEND);
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}		
			}
		}
		else
		{
			System.out.println(Hydrazine.errorPrefix + "You have to either configure the module to use a list (-c) or use the -cr switch to check a single account. (Format: username/email:password)");
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
		settings.setProperty("loadFromFile", String.valueOf(ModuleSettings.askUserYesNo("Load accounts from file?")));
		
		if(Boolean.parseBoolean(settings.getProperty("loadFromFile")))
		{
			settings.setProperty("inputFile", ModuleSettings.askUser("File path:"));
			settings.setProperty("loginDelay", String.valueOf(ModuleSettings.askUser("Delay between login attempts (in milliseconds):")));
		}
				
		settings.setProperty("outputToFile", String.valueOf(ModuleSettings.askUserYesNo("Output working accounts to file?")));
		
		if(Boolean.parseBoolean(settings.getProperty("outputToFile")))
		{
			settings.setProperty("outputFile", ModuleSettings.askUser("File path:"));
		}
		
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

	@Override
	public void run() 
	{
		start();
	}

}
