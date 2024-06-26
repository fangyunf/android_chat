package com.yaoxin.appbase.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class BeanToMapUtil {
    public static Map<String, Object> convertToMap(Object bean) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        String json = gson.toJson(bean);
        return gson.fromJson(json, type);
    }

}
