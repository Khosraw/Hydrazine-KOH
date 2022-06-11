package com.github.testmodule;

import java.util.Date;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.module.Module;

public class TestModule implements Module
{
	
	@Override
	public String getDescription()
	{
		return "This is a test module, it outputs some test messages.";
	}
	
	@Override
	public String getModuleName() 
	{
		return "testmodule";
	}

	@Override
	public void start() 
	{
		System.out.println(Hydrazine.infoPrefix + "Module started at " + new Date());
		
		stop("finished");
	}

	@Override
	public void stop(String cause) 
	{
		System.out.println(Hydrazine.infoPrefix + "Module stopped at " + new Date() + ": " + cause);
	}

	@Override
	public void configure() 
	{
		System.out.println("Nothing to configure here.");
	}

	@Override
	public void run() 
	{
		start();
	}

}
