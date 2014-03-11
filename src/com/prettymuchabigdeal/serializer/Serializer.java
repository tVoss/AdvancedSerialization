package com.prettymuchabigdeal.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.prettymuchabigdeal.serializer.Serializable.Ignore;
import com.prettymuchabigdeal.serializer.Serializable.Keep;
import com.prettymuchabigdeal.serializer.except.ClassNotSerializeableException;
import com.prettymuchabigdeal.serializer.except.DifferentVersionException;

public class Serializer<Data> {

	/**
	 * Types of fields as used by {@link SerializationMethod}
	 * 
	 * @author Tyler
	 * 
	 */
	private static enum FieldType {
		PRIMITIVE, SERIALIZEABLE, PRIMITVE_ARRAY, SERIALIZEABLE_ARRAY
	}

	/**
	 * Version string used if version is included in output
	 */
	private static final String VERSION = "serial_version";

	/**
	 * Boxed object versions of primitives
	 */
	private static final Class<?>[] PRIMITIVE_OBJECTS = { Boolean.class,
			Byte.class, Character.class, Short.class, Integer.class,
			Float.class, Long.class, Double.class, String.class };

	/**
	 * The current method used for this serializer
	 */
	private SerializationMethod<Data> mMethod;

	public Serializer(SerializationMethod<Data> method) {
		mMethod = method;
	}

	/**
	 * Attempts to serialize object. Should be annotated with
	 * {@link Serializable}
	 * 
	 * @param object
	 *            object to serialize
	 * @return serialized object in Data format, <b>null</b> if there was an
	 *         error
	 */
	public Data trySerialize(Object object) {
		try {
			return serialize(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Serializes object, not catching any exceptions that occur. Should be
	 * annotated with {@link Serializable}
	 * 
	 * @param object
	 *            object to serialize
	 * @return serialized object in Data format
	 * @throws ClassNotSerializeableException
	 *             if class is not annotated with {@link Serializable}
	 * @throws Exception
	 *             if an unexpected reflection error occurred
	 */
	public Data serialize(Object object) throws Exception {
		Serializable annotation = getAnnotation(object.getClass());

		Map<String, Field> fields = getFields(object.getClass(),
				annotation.autoIgnore());

		Data data = mMethod.newData();

		if (annotation.includeVersion())
			mMethod.storePrimitive(data, VERSION, annotation.version());

		for (Entry<String, Field> e : fields.entrySet()) {

			String name = e.getKey();
			Object value = e.getValue().get(object);

			switch (getFieldType(e.getValue())) {

			case PRIMITIVE:
				mMethod.storePrimitive(data, name, value);
				break;
			case PRIMITVE_ARRAY:
				mMethod.storePrimitiveArray(data, name, value);
				break;
			case SERIALIZEABLE:
				mMethod.storeSerializeable(data, name, value, this);
				break;
			case SERIALIZEABLE_ARRAY:
				mMethod.storeSerializeableArray(data, name, value, this);
				break;

			}

		}

		return data;

	}

	/**
	 * Attempts to deserialize data into class
	 * 
	 * @param clazz
	 *            class of serialized data
	 * @param data
	 *            data to deserialize
	 * @return deserialized object, or <b>null</b> if error occurred
	 */
	public <T> T tryDeserialize(Class<T> clazz, Data data) {
		try {
			return deserialize(clazz, data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Deserializes data into class, throwing any exceptions that occur
	 * 
	 * @param clazz
	 *            class of serialized data
	 * @param data
	 *            data to deserialize
	 * @return deserialized object
	 * @throws ClassNotSerializeableException
	 *             if class is not annotated with {@link Serializable}
	 * @throws DifferentVersionException
	 *             if runtime and serialized class have different versions
	 * @throws Exception
	 *             if an unexpected reflection error occurs
	 */
	public <T> T deserialize(Class<T> clazz, Data data) throws Exception {
		Serializable annotation = getAnnotation(clazz);

		if (annotation.includeVersion()) {
			int cVersion = annotation.version();
			int dVersion = (int) mMethod.loadPrimitive(data, VERSION);

			if (cVersion != dVersion)
				throw new DifferentVersionException(cVersion, dVersion);
		}

		T t;
		try {
			t = clazz.newInstance();
		} catch (InstantiationException e) {
			Constructor<T> ctor = clazz.getConstructor();
			ctor.setAccessible(true);
			t = ctor.newInstance();
		}

		Map<String, Field> fields = getFields(clazz, annotation.autoIgnore());

		for (String key : mMethod.getKeys(data)) {
			if (VERSION.equals(key))
				continue;

			Field field = fields.get(key);
			Class<?> type = field.getType();
			Object value = null;

			switch (getFieldType(field)) {
			case PRIMITIVE:
				value = mMethod.loadPrimitive(data, key);
				break;
			case PRIMITVE_ARRAY:
				value = mMethod.loadPrimitiveArray(data, null, type);
				break;
			case SERIALIZEABLE:
				value = mMethod.loadSerializeable(data, key, this, clazz);
				break;
			case SERIALIZEABLE_ARRAY:
				value = mMethod.loadSerializeableArray(data, key, this, clazz);
				break;
			}

			field.set(t, value);
		}

		return t;

	}

	/**
	 * Gets the Serialization specific type of field
	 * 
	 * @param field
	 *            field to determine type of
	 * @return the appropriate field type
	 */
	private static FieldType getFieldType(Field field) {
		Class<?> type = field.getType();

		if (type.isArray()) {

			if (isPrimitive(type.getComponentType()))
				return FieldType.PRIMITVE_ARRAY;
			else
				return FieldType.SERIALIZEABLE_ARRAY;

		} else {

			if (isPrimitive(type))
				return FieldType.PRIMITIVE;
			else
				return FieldType.SERIALIZEABLE;
		}
	}

	/**
	 * Gets the {@link Serializable} annotation from a class
	 * 
	 * @param clazz
	 *            the class to get the annotation from
	 * @return the <code>Serializable</code> annotation instance
	 * @throws ClassNotSerializeableException
	 *             if class has no <code>Serializeable</code> annotation
	 */
	private static Serializable getAnnotation(Class<?> clazz) {
		Serializable s = clazz.getAnnotation(Serializable.class);

		if (s == null)
			throw new ClassNotSerializeableException(clazz);
		else
			return s;
	}

	/**
	 * Determines if class is if primitive type, boxed or unboxed
	 * 
	 * @param clazz
	 *            class to test
	 * @return <b>true</b> if class is primitive, the class of a boxed
	 *         primitive, or a String
	 */
	private static boolean isPrimitive(Class<?> clazz) {
		if (clazz.isPrimitive())
			return true;

		if (Arrays.asList(PRIMITIVE_OBJECTS).contains(clazz))
			return true;

		return false;

	}

	/**
	 * Raw mapping of fields to field names of class and its superclasses
	 * 
	 * @param clazz
	 *            class to map
	 * @param ignore
	 *            if fields should be ignored by default
	 * @return a map with string keys mapped to the fields they represent
	 */
	private static Map<String, Field> getFields(Class<?> clazz, boolean ignore) {

		Map<String, Field> fields = new HashMap<>();
		if (clazz == null)
			return fields;

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);

			boolean add = true;
			int mod = field.getModifiers();

			add &= !Modifier.isStatic(mod);
			add &= !Modifier.isFinal(mod);
			add &= !Modifier.isTransient(mod);

			if (ignore)
				add &= field.getAnnotation(Keep.class) != null;
			else
				add &= field.getAnnotation(Ignore.class) == null;

			if (add)
				fields.put(field.getName(), field);
		}

		fields.putAll(getFields(clazz.getSuperclass(), ignore));

		return fields;
	}

}
