package com.financeapp.view;

import java.lang.reflect.Field;

public class TestUtils {
    public static Object getPrivateField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }
}
