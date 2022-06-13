package com.github.hydrazine.minecraft;

/**
 * @author xTACTIXzZ
 * <p>
 * This class represents a minecraft server.
 */
public class Server(String host, int port) {

	public Server(String h, int i) {
		h = this.host();
		i = this.port();
	}

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
