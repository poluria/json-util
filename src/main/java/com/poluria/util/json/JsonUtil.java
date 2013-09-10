package com.poluria.util.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * circular refence 방지.
 * depth 지정 가능.
 *
 * @author poluria
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final String KEY_VALUE_DELIMITER = ":";
    private static final String FIELD_DELIMITER = ", ";
    private static final String STRING_VALUE_MARK = "\"";
    private static final String ITERABLE_START = "[";
    private static final String ITERABLE_END = "]";
    private static final String MAP_START = "{";
    private static final String MAP_END = "}";
    private static final String OBJECT_START = "{";
    private static final String OBJECT_END = "}";
    private static final String KEYWORD_ERROR = "error";
    private static final String KEYWORD_NULL = "null";
    private static final String KEYWORD_OMIT = "omit";
    private static final String KEYWORD_CIRCULAR = "circular";
    private static final int DEFAULT_DEPTH_LIMIT = 3;

    public static String toJson(Object obj) {
        return toJson(obj, DEFAULT_DEPTH_LIMIT);
    }

    public static String toJson(Object obj, int depth) {
        try {
            return toJson(obj, new LinkedList<Object>(), depth, 0);
        } catch (Exception e) {
            logger.error("ToString error. {}", e.getMessage());
            return KEYWORD_ERROR;
        }
    }

    private static String toJson(Object obj, LinkedList<Object> objects, final int depthLimit, int depth) throws IllegalAccessException {
        if (obj == null) {
            return KEYWORD_NULL;
        }

        Class<? extends Object> clazz = obj.getClass();

        if (obj instanceof String) {
            return wrapStringValue((String) obj);
        }
        else if (clazz.isPrimitive() || obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        }
        else if (clazz.isEnum() || obj instanceof Date) {
            return wrapStringValue(obj.toString());
        }
        else if (Iterable.class.isInstance(obj)) {
            StringBuilder sb = new StringBuilder();
            sb.append(ITERABLE_START);
            for (Object each : (Iterable) obj) {
                objects.push(obj);
                sb.append(toJson(each, objects, depthLimit, depth + 1));
                objects.pop();
                sb.append(FIELD_DELIMITER);
            }
            deleteLastDelimiter(sb);
            sb.append(ITERABLE_END);
            return sb.toString();
        }
        else if (Map.class.isInstance(obj)) {
            StringBuilder sb = new StringBuilder();
            sb.append(MAP_START);
            for (Map.Entry each : ((Map<?, ?>) obj).entrySet()) {
                objects.push(obj);
                sb.append(toJson(each.getKey(), objects, depthLimit, depth + 1));
                objects.pop();
                sb.append(KEY_VALUE_DELIMITER);
                objects.push(obj);
                sb.append(toJson(each.getValue(), objects, depthLimit, depth + 1));
                objects.pop();
                sb.append(FIELD_DELIMITER);
            }
            deleteLastDelimiter(sb);
            sb.append(MAP_END);
            return sb.toString();
        }
        else if (depth > depthLimit) {
            return wrapStringValue(KEYWORD_OMIT);
        }
        if (containsReference(obj, objects)) {
            return wrapStringValue(KEYWORD_CIRCULAR);
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(OBJECT_START);

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                sb.append(STRING_VALUE_MARK).append(field.getName()).append(STRING_VALUE_MARK);
                sb.append(KEY_VALUE_DELIMITER);
                objects.push(obj);
                sb.append(toJson(field.get(obj), objects, depthLimit, depth + 1));
                objects.pop();
                sb.append(FIELD_DELIMITER);
            }
            deleteLastDelimiter(sb);
            sb.append(OBJECT_END);
            return sb.toString();
        }
    }

    private static String wrapStringValue(String value) {
        return STRING_VALUE_MARK + value + STRING_VALUE_MARK;
    }

    private static boolean containsReference(Object obj, LinkedList<Object> objects) {
        for (Object each : objects) {
            if (each == obj) {
                return true;
            }
        }
        return false;
    }

    private static void deleteLastDelimiter(StringBuilder sb) {
        int index = sb.lastIndexOf(FIELD_DELIMITER);
        if (index > 0) {
            sb.delete(index, index + FIELD_DELIMITER.length());
        }
    }
}
