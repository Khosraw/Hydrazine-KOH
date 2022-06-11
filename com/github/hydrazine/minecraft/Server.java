package com.github.hydrazine.minecraft;

/**
 * @author xTACTIXzZ
 * <p>
 * This class represents a minecraft server.
 */
public record Server(String host, int port) {

	/**
	 * @return The hostname/ip address of the server
	 */
	@Override
	public String host() {
		return host;
	}

	/**
	 * @return The port of the server
	 */
	@Override
	public int port() {
		return port;
	}

}
