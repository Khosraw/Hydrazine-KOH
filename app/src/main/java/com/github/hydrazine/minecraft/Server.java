package com.github.hydrazine.minecraft;

/**
 * @author xTACTIXzZ
 * <p>
 * This class represents a minecraft server.
 */
public class Server {
	private final String host;
	private final int port;

	public Server(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * @return The hostname/ip address of the server
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return The port of the server
	 */
	public int getPort() {
		return port;
	}

}
