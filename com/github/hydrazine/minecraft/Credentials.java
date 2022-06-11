package com.github.hydrazine.minecraft;

/**
 * @author xTACTIXzZ
 * <p>
 * This class stores minecraft login credentials.
 */
public record Credentials(String username, String password) {

	/**
	 * @return The username/email of the minecraft account
	 */
	@Override
	public String username() {
		return username;
	}

	/**
	 * @return The password of the minecraft account
	 */
	@Override
	public String password() {
		return password;
	}

}
