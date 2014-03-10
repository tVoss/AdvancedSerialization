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

	private static enum FieldType {
		PRIMITIVE, SERIALIZEABLE, PRIMITVE_ARRAY, SERIALIZEABLE_ARRAY
	}

	private static final String VERSION = "serial_version";
	private static final Class<?>[] PRIMITIVE_OBJECTS = { Boolean.class,
			Byte.class, Character.class, Short.class, Integer.class,
			Float.class, Long.class, Double.class, String.class };

	private SerializationMethod<Data> mMethod;

	public Serializer(SerializationMethod<Data> method) {
		mMethod = method;
	}

	public Data trySerialize(Object object) {
		try {
			return serialize(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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

	public <T> T tryDeserialize(Class<T> clazz, Data data) {
		try {
			return deserialize(clazz, data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> T deserialize(Class<T> clazz, Data data) throws Exception {
		Serializable annotation = getAnnotation(clazz);

		if (annotation.includeVersion()) {
			int cVersion = annotation.version();
			int dVersion = (int) mMethod.loadPrimitive(data, VERSION);

			if (cVersion != dVersion)
				throw new DifferentVersionException(cVersion, dVersion);
		}

		T t;
		try{
			t = clazz.newInstance();
		}catch(InstantiationException e){
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

	private static Serializable getAnnotation(Class<?> clazz) {
		Serializable s = clazz.getAnnotation(Serializable.class);

		if (s == null)
			throw new ClassNotSerializeableException(clazz);
		else
			return s;
	}

	private static boolean isPrimitive(Class<?> clazz) {
		if (clazz.isPrimitive())
			return true;

		if (Arrays.asList(PRIMITIVE_OBJECTS).contains(clazz))
			return true;

		return false;

	}

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
