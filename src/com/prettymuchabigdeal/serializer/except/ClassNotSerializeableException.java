package com.prettymuchabigdeal.serializer.except;

public class ClassNotSerializeableException extends RuntimeException {

	private static final long serialVersionUID = -7171961649860176535L;

	public ClassNotSerializeableException(Class<?> clazz) {
		super(clazz.getName());
	}
	
}
