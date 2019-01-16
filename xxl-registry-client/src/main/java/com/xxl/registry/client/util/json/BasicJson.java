package com.xxl.registry.client.util.json;

import com.xxl.registry.client.util.FieldReflectionUtil;

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
     * parse json to <T>
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
            // parse business class
            return parseObjectFromMap(mapObject, businessClass);
        }
    }

    /**
     * parse json to map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, null);
    }

    // parse object from map
    private static <T> T parseObjectFromMap(Map<String, Object> mapObject, Class<T> businessClass){
        // parse class (only first level)
        try {
            Object newItem = businessClass.newInstance();
            Field[] fieldList = basicJsonwriter.getDeclaredFields(businessClass);
            for (Field field: fieldList) {

                // valid val
                Object fieldValue = mapObject.get(field.getName());
                if (fieldValue == null) {
                    continue;
                }

                // valid type
                if (field.getType() != fieldValue.getClass()) {
                    fieldValue = FieldReflectionUtil.parseValue(field.getType(), String.valueOf(fieldValue) );
                }

                // field set
                field.setAccessible(true);
                field.set(newItem, fieldValue);
            }

            return (T) newItem;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse JSON", e);
        }
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
            // parse map class, default
            return (List<T>) listObject;
        } else {
            // parse business class
            if (listObject.get(0).getClass() != LinkedHashMap.class) {
                throw new IllegalArgumentException("Cannot parse JSON, custom class must match LinkedHashMap");
            }
            try {
                List<Object> newItemList = new ArrayList<>();
                for (Object oldItem: listObject) {

                    Map<String, Object> oldItemMap = (Map<String, Object>) oldItem;
                    Object newItem = parseObjectFromMap(oldItemMap, businessClass);

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
