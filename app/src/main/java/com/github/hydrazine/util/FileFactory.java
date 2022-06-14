package com.github.hydrazine.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Scanner;

import com.github.hydrazine.Hydrazine;
import com.github.hydrazine.minecraft.Credentials;

/**
 * @author xTACTIXzZ
 * <p>
 * This class takes a file, processes it's content, and finally returns an array of a specific type.
 */
public class FileFactory {
	private File file;

	public FileFactory(File file) {
		this.file = file;
	}
    /**
	 * Uses some deep magic to spawn a proxy array!
	 *
	 * @param type The type of the proxy
	 * @return An array of proxies, extracted from file
	 */
	public Proxy[] getProxies(Proxy.Type type) {
		ArrayList<Proxy> content = new ArrayList<>();
		Scanner s;

		try {
			s = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println(Hydrazine.errorPrefix + file.getAbsolutePath() + " not found!");

			return null;
		}

		while (s.hasNextLine()) {
			String line = s.nextLine();
			String host, port;

			if (line.contains(":")) {
				String[] a = line.split(":");

				host = a[0];
				port = a[1];

				int parsedPort;

				try {
					parsedPort = Integer.parseInt(port);

					Proxy p = new Proxy(type, new InetSocketAddress(host, parsedPort));

					content.add(p);
				} catch (NumberFormatException ignored) {
				}
			}
		}

		s.close();

		return content.toArray(new Proxy[0]);
	}

	/**
	 * Summons an array of credentials from a place you could never imagine!
	 *
	 * @return An array of credentials, extracted from file
	 */
	public Credentials[] getCredentials() {
		ArrayList<Credentials> content = new ArrayList<>();
		Scanner s;

		try {
			s = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println(Hydrazine.errorPrefix + file.getAbsolutePath() + " not found!");

			return null;
		}

		while (s.hasNextLine()) {
			String line = s.nextLine();
			String user, pass;

			if (line.contains(":")) {
				String[] a = line.split(":");

				user = a[0];
				pass = a[1];

				Credentials c = new Credentials(user, pass);

				content.add(c);
			}
		}

		s.close();

		return content.toArray(new Credentials[0]);
	}

	/**
	 * Returns a list of usernames extracted from file
	 *
	 * @return An array of minecraft usernames
	 */
	public String[] getUsernames() {
		ArrayList<String> content = new ArrayList<>();
		Scanner s;

		try {
			s = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println(Hydrazine.errorPrefix + file.getAbsolutePath() + " not found!");

			return null;
		}

		while (s.hasNextLine()) {
			String line = s.nextLine();

			if (line.length() <= 16 && line.length() >= 3) {
				content.add(line);
			}
		}

		s.close();

		return content.toArray(new String[0]);
	}

	/**
	 * @return the file
	 */
	public File file() {
		return file;
	}

}
