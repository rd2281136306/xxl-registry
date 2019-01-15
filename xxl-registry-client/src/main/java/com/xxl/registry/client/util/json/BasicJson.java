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


    @Deprecated
    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, null);
    }

    /**
     * json to <T>
     *
     * @param json
     * @param clazz  "XXX.class" etc, null means "Map.class"
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json, Class<T> clazz) {

        // map object
        Map<String, Object> mapObject = basicJsonReader.parseMap(json);


        if (clazz == null || mapObject.size()==0) {
            // parse map class, default
            return (T) mapObject;
        } else {
            // parse class (only first level)
            try {
                Object objInstance = clazz.newInstance();
                Field[] fieldList = basicJsonwriter.getAllDeclaredFields(clazz);
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
     * json to List<T>
     *
     * @param json
     * @param clazz     clazz  "XXX.class" etc, null means "Map.class"
     * @param <T>
     * @return
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {

        // list object
        List<Object> listObject = basicJsonReader.parseList(json);

        if (clazz==null || listObject.size()==0) {
            // parse map class
            return (List<T>) listObject;
        } else {
            // parse class (only first level)

            if (listObject.get(0).getClass() != LinkedHashMap.class) {
                throw new IllegalArgumentException("Cannot parse JSON, custom class must match LinkedHashMap");
            }
            try {
                // all field
                Field[] fieldList = basicJsonwriter.getAllDeclaredFields(clazz);

                List<Object> newItemList = new ArrayList<>();
                for (Object oldItem: listObject) {

                    // new item
                    Object newItem = clazz.newInstance();
                    Map<String, Object> oldItemMap = (Map<String, Object>) oldItem;


                    // fill field
                    for (Field field: fieldList) {

                        if (!oldItemMap.containsKey(field.getName())) {
                            continue;
                        }

                        field.setAccessible(true);
                        field.set(newItem, oldItemMap.get(field.getName()));
                    }

                    newItemList.add(newItem);
                }
                return (List<T>) newItemList;

            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse JSON", e);
            }

        }

    }


    public static void main(String[] args) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("arr", Arrays.asList("111","222"));
        result.put("float", 1.11f);
        result.put("temp", null);

        String json = toJson(result);
        System.out.println(json);

        Object jsonObj2 = parseMap(json);
        System.out.println(jsonObj2);
    }

}
