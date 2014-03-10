package com.prettymuchabigdeal.serializer.impl;

import java.lang.reflect.Array;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.prettymuchabigdeal.serializer.SerializationMethod;
import com.prettymuchabigdeal.serializer.Serializer;

public class JSONSerilizationMethod implements SerializationMethod<JSONObject> {

	@Override
	public JSONObject newData() {
		return new JSONObject();
	}

	@Override
	public void storePrimitive(JSONObject out, String key, Object value) {
		out.put(key, value);
	}

	@Override
	public void storeSerializeable(JSONObject out, String key, Object object,
			Serializer<JSONObject> serializer) {
		out.put(key, serializer.trySerialize(object));
	}

	@Override
	public void storePrimitiveArray(JSONObject out, String key, Object array) {
		JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < Array.getLength(array); i++)
			jsonArray.put(Array.get(array, i));

		out.put(key, jsonArray);
	}

	@Override
	public void storeSerializeableArray(JSONObject out, String key,
			Object array, Serializer<JSONObject> serializer) {
		JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < Array.getLength(array); i++)
			jsonArray.put(serializer.trySerialize(Array.get(array, i)));

		out.put(key, jsonArray);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getKeys(JSONObject data) {
		return data.keySet();
	}

	@Override
	public Object loadPrimitive(JSONObject in, String key) {
		return in.get(key);
	}

	@Override
	public Object loadSerializeable(JSONObject in, String key,
			Serializer<JSONObject> serializer, Class<?> clazz) {
		return serializer.tryDeserialize(clazz, in.getJSONObject(key));
	}

	@Override
	public Object loadPrimitiveArray(JSONObject in, String key, Class<?> clazz) {
		JSONArray jsonArray = in.getJSONArray(key);
		Object array = Array.newInstance(clazz, jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++)
			Array.set(array, i, jsonArray.get(i));

		return array;
	}

	@Override
	public Object loadSerializeableArray(JSONObject in, String key,
			Serializer<JSONObject> serializer, Class<?> clazz) {
		JSONArray jsonArray = in.getJSONArray(key);
		Object array = Array.newInstance(clazz, jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			Array.set(array, i, serializer.tryDeserialize(clazz, object));
		}
		
		return array;
	}

}
