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
     * json to <T>
     *
     * @param json
     * @param businessClass     null for base-class "Integer、Long、Map ... " , other for business-class
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json, Class<T> businessClass) {

        // map object
        Map<String, Object> mapObject = basicJsonReader.parseMap(json);


        if (businessClass == null || mapObject.size()==0) {
            // parse map class, default
            return (T) mapObject;
        } else {
            // parse class (only first level)
            try {
                Object newItem = businessClass.newInstance();
                Field[] fieldList = basicJsonwriter.getAllDeclaredFields(businessClass);
                for (Field field: fieldList) {

                    if (!mapObject.containsKey(field.getName())) {
                        continue;
                    }

                    field.setAccessible(true);
                    field.set(newItem, mapObject.get(field.getName()));
                }

                return (T) newItem;
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse JSON", e);
            }
        }
    }

    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, null);
    }

    /**
     * json to List<T>
     *
     * @param json
     * @param businessClass     null for base-class "Integer、Long、Map ... " , other for business-class
     * @param <T>
     * @return
     */
    public static <T> List<T> parseList(String json, Class<T> businessClass) {

        // list object
        List<Object> listObject = basicJsonReader.parseList(json);

        if (businessClass==null || listObject.size()==0) {
            // parse map class
            return (List<T>) listObject;
        } else {
            // parse class (only first level)

            if (listObject.get(0).getClass() != LinkedHashMap.class) {
                throw new IllegalArgumentException("Cannot parse JSON, custom class must match LinkedHashMap");
            }
            try {
                // all field
                Field[] fieldList = basicJsonwriter.getAllDeclaredFields(businessClass);

                List<Object> newItemList = new ArrayList<>();
                for (Object oldItem: listObject) {

                    // new item
                    Object newItem = businessClass.newInstance();
                    Map<String, Object> originItemMap = (Map<String, Object>) oldItem;


                    // fill field
                    for (Field field: fieldList) {

                        if (!originItemMap.containsKey(field.getName())) {
                            continue;
                        }

                        field.setAccessible(true);
                        field.set(newItem, originItemMap.get(field.getName()));
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

        List<Integer> listInt = parseList("[111,222,33]", null);

    }

}
