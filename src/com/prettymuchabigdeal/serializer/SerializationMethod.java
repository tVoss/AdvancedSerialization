package com.prettymuchabigdeal.serializer;

import java.lang.reflect.Array;
import java.util.Set;

/**
 * Describes all methods needed to completely serialize and deserialize data
 * 
 * @author Tyler
 * 
 * @param <Data>
 *            the format of the class data after serialization
 */
public interface SerializationMethod<Data> {

	/**
	 * Initial empty instance of data to be filled with class information
	 * 
	 * @return new instance of implementation's specific data type
	 */
	public Data newData();

	/**
	 * Store a primitive value
	 * 
	 * @param out
	 *            output to store primitive into
	 * @param key
	 *            the name associated with primitive
	 * @param value
	 *            actual value of primitive
	 */
	public void storePrimitive(Data out, String key, Object value);

	/**
	 * Store a serializeable object
	 * 
	 * @param out
	 *            output to store object into
	 * @param key
	 *            the name associated with object
	 * @param object
	 *            actual value of object
	 * @param serializer
	 *            instace of serializer to further serialize object
	 */
	public void storeSerializeable(Data out, String key, Object object,
			Serializer<Data> serializer);

	/**
	 * Store a primitive array
	 * 
	 * @param out
	 *            output to store array into
	 * @param key
	 *            the name associated with array
	 * @param array
	 *            object representation of array
	 * @see Array
	 */
	public void storePrimitiveArray(Data out, String key, Object array);

	/**
	 * Store a serializeable object array
	 * 
	 * @param out
	 *            output to store array into
	 * @param key
	 *            the name associated with array
	 * @param array
	 *            object representation of array
	 * @see Array
	 */
	public void storeSerializeableArray(Data out, String key, Object array,
			Serializer<Data> serializer);

	/**
	 * Set of all top level keys in data
	 * 
	 * @param data
	 *            data to extract keys from
	 * @return set of keys
	 */
	public Set<String> getKeys(Data data);

	/**
	 * @param in
	 *            serialized data
	 * @param key
	 *            the key to load from
	 * @return primitive stored in key
	 */
	public Object loadPrimitive(Data in, String key);

	/**
	 * @param in
	 *            serialized data
	 * @param key
	 *            the key to load from
	 * @param serializer
	 *            instance of serializer to further deserialize data
	 * @param clazz
	 *            expected class of serialized data
	 * @return primitive stored in key
	 */
	public Object loadSerializeable(Data in, String key,
			Serializer<Data> serializer, Class<?> clazz);

	/**
	 * @param in
	 *            serialized array
	 * @param key
	 *            the key to load from
	 * @param clazz
	 *            the class of the primitive in the array
	 * @return array stored in key
	 */
	public Object loadPrimitiveArray(Data in, String key, Class<?> clazz);

	/**
	 * @param in
	 *            serialized array
	 * @param key
	 *            the key to load from
	 * @param serializer
	 *            instance of serializer to further deserialize data
	 * @param clazz
	 *            the class of the object in the array
	 * @return array stored in key
	 */
	public Object loadSerializeableArray(Data in, String key,
			Serializer<Data> serializer, Class<?> clazz);

}
