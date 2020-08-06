package com.wjz.wrapper;

import javassist.*;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class IClassGenerator {

    private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0);
    private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<ClassLoader, ClassPool>();

    private String className;
    private String superClassName;
    private List<String> imports;
    private List<String> interfaces;
    private List<String> constructors;
    private List<String> fields;
    private List<String> methods;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public List<String> getConstructors() {
        return constructors;
    }

    public void setConstructors(List<String> constructors) {
        this.constructors = constructors;
    }

    public List<String> getFields() {
        return fields;
    }

    public void addField(String field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
    }

    public List<String> getMethods() {
        return methods;
    }

    public void addMethod(String method) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(method);
    }

    public Class<?> toClass() {
        return toClass(Thread.currentThread().getContextClassLoader(), getClass().getProtectionDomain());
    }

    public Class<?> toClass(ClassLoader cl, ProtectionDomain pd) {
        ClassPool pool = getClassPool(cl);
        long id = CLASS_NAME_COUNTER.getAndIncrement();
        try {
            CtClass scts = superClassName == null ? null : pool.get(superClassName);
            if (className == null) {
                className = (scts == null || Modifier.isPublic(scts.getModifiers()) ? IClassGenerator.class.getName() : superClassName + "$sc") + id;
            }
            CtClass ctClass = pool.makeClass(className);
            // ==> superClassName
            if (scts != null) {
                ctClass.setSuperclass(scts);
            }
            // ==> imports
            if (imports != null) {
                imports.stream().forEach(pool::importPackage);
            }
            // ==> interfaces
            if (interfaces != null) {
                for (String i : interfaces) {
                    ctClass.addInterface(pool.get(i));
                }
            }
            // ==> constructors
            if (constructors != null) {
                for (String c : constructors) {
                    ctClass.addConstructor(CtNewConstructor.make(c, ctClass));
                }
            } else {
                ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
            }
            // ==> fields
            if (fields != null) {
                for (String f : fields) {
                    ctClass.addField(CtField.make(f, ctClass));
                }
            }
            // ==> methods
            if (methods != null) {
                for (String m : methods) {
                    ctClass.addMethod(CtNewMethod.make(m, ctClass));
                }
            }
            return ctClass.toClass(cl, pd);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassPool getClassPool(ClassLoader cl) {
        if (cl == null) {
            return ClassPool.getDefault();
        }
        ClassPool pool = POOL_MAP.get(cl);
        if (pool == null) {
            pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(cl));
            POOL_MAP.put(cl, pool);
        }
        return pool;
    }
}
