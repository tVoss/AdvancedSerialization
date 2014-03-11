package com.prettymuchabigdeal.serializer.except;

import com.prettymuchabigdeal.serializer.Serializable;
import com.prettymuchabigdeal.serializer.Serializer;

/**
 * Thrown when a class is passed through a {@link Serializer}, but isn't
 * annotated with {@link Serializable}
 * 
 * @author Tyler
 * 
 */
public class ClassNotSerializeableException extends RuntimeException {

	private static final long serialVersionUID = -7171961649860176535L;

	public ClassNotSerializeableException(Class<?> clazz) {
		super(clazz.getName());
	}

}
