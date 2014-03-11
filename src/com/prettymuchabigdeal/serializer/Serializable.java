package com.prettymuchabigdeal.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.prettymuchabigdeal.serializer.except.DifferentVersionException;

/**
 * By annotating a class with <b>Serializable</b>, you are declaring that all
 * member variables that are not either <code>static</code>, <code>final</code>,
 * or <code>transient</code> are also <b>Serializeable</b>, therefore allowing
 * instances of this class to be processed by a {@link Serializer} along with
 * any {@link SerializationMethod} to produce output.<br>
 * <br>
 * <b>Primitives</b>, along with their boxed object representations, and
 * <b>Strings</b> are all considered <b>Serializeable</b> by default.
 * 
 * @author Tyler
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serializable {

	/**
	 * The version of the Object's class, used in
	 * {@link Serializable#includeVersion()}
	 */
	int version() default 0;

	/**
	 * Determines if the version of the class should be included in the
	 * serialized output. If it is included, it is saved as a primitive through
	 * the selected {@link SerializationMethod} with the tag
	 * <code>serial_version</code>.<br>
	 * <br>
	 * If the version is included, and their is a mismatch between versions of
	 * the serialized and runtime class, a {@link DifferentVersionException}
	 * will be thrown.
	 * 
	 */
	boolean includeVersion() default true;

	/**
	 * Determines if member variables are included by default, and annotated
	 * with {@link Ignore} to exclude from output, or if member variables are
	 * excluded by default, and included by being annotated with {@link Keep}.
	 */
	boolean autoIgnore() default false;

	/**
	 * Marks a field where autoIgnore is <b>false</b> to not be included in
	 * output
	 * 
	 * @author Tyler
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Ignore {
	}

	/**
	 * Marks a field where autoIgnore is <b>true</b> to be included in output
	 * 
	 * @author Tyler
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Keep {
	}

}
