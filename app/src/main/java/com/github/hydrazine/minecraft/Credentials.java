package com.github.hydrazine.minecraft;

/**
 * @author xTACTIXzZ
 * <p>
 * This class stores minecraft login credentials.
 */
public class Credentials {
	private final String username, password;

	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * @return The username/email of the minecraft account
	 */
	public String username() {
		return username;
	}

	/**
	 * @return The password of the minecraft account
	 */
	public String password() {
		return password;
	}

}
