package com.github.unldenis.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

public class UnsafeUtil {

    private static final Unsafe unsafe = getUnsafe();

    public static Object allocateInstance(Class<?> clazz) throws InstantiationException {
        return unsafe.allocateInstance(clazz);
    }

    public static void throwException(Throwable exception) {
        unsafe.throwException(exception);
    }

    public static <U> U shallowCopy(U obj) throws InstantiationException {
        U instance = (U) unsafe.allocateInstance(obj.getClass());
        long start = toAddress(obj);
        long size = sizeOf(instance);
        long address = toAddress(instance);
        unsafe.copyMemory(start, address, size);
        return (U) fromAddress(address);
    }

    private static long sizeOf(Object o) {
        Class<?> c = o.getClass();
        long maxSize = 0;
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    long offset = unsafe.objectFieldOffset(f);
                    if (offset > maxSize)
                        maxSize = offset;
                }
            }
            c = c.getSuperclass();
        }
        return ((maxSize/8) + 1) * 8;   // padding
    }

    private static long toAddress(Object obj) {
        Object[] array = new Object[] {obj};
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        return normalize(unsafe.getInt(array, baseOffset));
    }

    private static Object fromAddress(long address) {
        Object[] array = new Object[] {null};
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array, baseOffset, address);
        return array[0];
    }

    private static long normalize(int value) {
        if(value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    private static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        }catch (NoSuchFieldException exception){
            throw new NoSuchElementException("Cannot find unsafe field in wrapper class");
        }catch (IllegalAccessException exception){
            throw new UnsupportedOperationException("Access to the unsafe wrapper has been blocked ", exception);
        }
    }

}