package com.prettymuchabigdeal.serializer;

import java.util.Set;

public interface SerializationMethod<Data> {

	public Data newData();

	public void storePrimitive(Data out, String key, Object value);

	public void storeSerializeable(Data out, String key, Object object,
			Serializer<Data> serializer);

	public void storePrimitiveArray(Data out, String key, Object array);

	public void storeSerializeableArray(Data out, String key, Object array,
			Serializer<Data> serializer);

	public Set<String> getKeys(Data data);

	public Object loadPrimitive(Data in, String key);

	public Object loadSerializeable(Data in, String key,
			Serializer<Data> serializer, Class<?> clazz);

	public Object loadPrimitiveArray(Data in, String key, Class<?> clazz);

	public Object loadSerializeableArray(Data in, String key,
			Serializer<Data> serializer, Class<?> clazz);

}
