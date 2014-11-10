package com.gfs.ihub.testCypress;

import static java.lang.reflect.Modifier.FINAL;

import java.lang.reflect.Field;

public final class TestUtil {

    public static void setFieldValue(final Object object, final String fieldName, final Object value)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {
        setFieldValue(getField(object.getClass(), fieldName), object, value);
    }

    static void setFieldValue(final Field field, final Object object, final Object value)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        makeAccessible(field);
        field.set(object, value);
    }

    private static void makeAccessible(final Field field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);

        // In order to set a field that is final we need to remove the final modifier using
        // reflection
        final Field modifiersField = Field.class.getDeclaredField("modifiers"); //$NON-NLS-1$
        modifiersField.setAccessible(true);
        // Use a bitwise AND and the complement of the Modifiers.FINAL field will remove the final
        // modifier
        modifiersField.setInt(field, field.getModifiers() & ~FINAL);
    }

    static Field getField(final Class<?> clazz, final String fieldName) throws SecurityException,
            NoSuchFieldException {

        Class<?> currentClass = clazz;

        while (null != currentClass) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (final NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }

        // If we get here, there is no field of that name anywhere in the class hierarchy. Attempt
        // to access the field one more time to throw the correct error
        return clazz.getDeclaredField(fieldName);
    }
}
