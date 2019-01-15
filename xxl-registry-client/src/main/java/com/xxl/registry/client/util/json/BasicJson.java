package com.xxl.registry.client.util.json;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author xuxueli 2018-11-30
 */
public class BasicJson {


    private static final BasicJsonReader basicJsonReader = new BasicJsonReader();
    private static final BasicJsonwriter basicJsonwriter = new BasicJsonwriter();


    /**
     * object to json
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return basicJsonwriter.toJson(object);
    }


    /**
     * json to Map<String, Object>
     *
     * @param json
     * @return
     */
    @Deprecated
    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, Map.class);
    }

    /**
     * json to Object
     *
     * @param json
     * @param objClass  like "Map.class、XXX.class" etc
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json, Class<T> objClass) {

        // map object
        Map<String, Object> mapObject = basicJsonReader.parseMap(json);

        // parse desc class
        if (objClass == Map.class) {
            return (T) mapObject;
        } else {
            // parse class
            try {
                Object objInstance = objClass.newInstance();
                Field[] fieldList = basicJsonwriter.getAllDeclaredFields(objClass);
                for (Field field: fieldList) {

                    if (!mapObject.containsKey(field.getName())) {
                        continue;
                    }

                    field.setAccessible(true);
                    field.set(objInstance, mapObject.get(field.getName()));
                }

                return (T) objInstance;
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse JSON", e);
            }
        }
    }

    /**
     * json to List<Object>
     *
     * @param json
     * @return
     */
    public static List<Object> parseList(String json) {
        return basicJsonReader.parseList(json);
    }


    public static void main(String[] args) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("arr", Arrays.asList("111","222"));
        result.put("float", 1.11f);

        String json = toJson(result);
        System.out.println(json);

        Object jsonObj2 = parseMap(json);
        System.out.println(jsonObj2);
    }

}
