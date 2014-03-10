package com.prettymuchabigdeal.serializer.except;

public class DifferentVersionException extends Exception {

	private static final long serialVersionUID = 1831415407686671185L;

	public DifferentVersionException(int currentVersion, int dataVersion) {
		super("Recieved data with version: " + dataVersion
				+ "while current class version is: " + currentVersion);
	}

}
