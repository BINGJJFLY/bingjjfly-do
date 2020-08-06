package com.wjz.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class IWrapper {

    private static final ConcurrentMap<Class<?>, IWrapper> WRAPPER_MAP = new ConcurrentHashMap<>();
    private static final IWrapper OBJECT_WRAPPER = new IWrapper() {
        @Override
        public String[] getPropertyNames() {
            return new String[0];
        }
    };
    private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);

    public static IWrapper getWrapper(Class<?> c) {
        if (c == Object.class) {
            return OBJECT_WRAPPER;
        }
        return WRAPPER_MAP.computeIfAbsent(c, k -> createWrapper(k));
    }

    private static IWrapper createWrapper(Class<?> c) {
        if (c.isPrimitive()) {
            throw new IllegalArgumentException("Can not create wrapper for primitive type: " + c);
        }
        long id = CLASS_NAME_COUNTER.getAndIncrement();
        Map<String, Class<?>> pns = new HashMap<>();

        for (Field f : c.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            pns.put(f.getName(), f.getType());
        }

        try {
            IClassGenerator cg = new IClassGenerator();
            cg.setClassName((Modifier.isPublic(c.getModifiers()) ? IWrapper.class.getName() : c.getName()) + id);
            cg.setSuperClassName(IWrapper.class.getName());
            cg.addField("public static String[] pns;");
            cg.addMethod("public String[] getPropertyNames() { return pns; }");
            Class<?> wc = cg.toClass();
            wc.getField("pns").set(null, pns.keySet().toArray(new String[0]));
            return (IWrapper) wc.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String[] getPropertyNames();
}
