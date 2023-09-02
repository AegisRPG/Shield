package co.aegisrpg.api.common.utils;


import co.aegisrpg.api.common.AegisAPI;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public ReflectionUtils() {
    }

    public static <T> List<Class<? extends T>> superclassesOf(Class<? extends T> clazz) {
        ArrayList superclasses;
        for(superclasses = new ArrayList(); clazz.getSuperclass() != Object.class; clazz = clazz.getSuperclass()) {
            superclasses.add(clazz);
        }

        superclasses.add(clazz);
        return superclasses;
    }

    public static <T> Set<Class<? extends T>> subTypesOf(Class<T> superclass, String... packages) {
        return getClasses(packages, (subclass) -> {
            if (!Utils.canEnable(subclass)) {
                return false;
            } else {
                return superclass.isInterface() ? subclass.implementsInterface(superclass) : subclass.extendsSuperclass(superclass);
            }
        });
    }

    public static <T> Set<Class<? extends T>> typesAnnotatedWith(Class<? extends Annotation> annotation, String... packages) {
        ScanResult scan = scanPackages(packages).scan();

        Set var3;
        try {
            var3 = (Set)scan.getClassesWithAnnotation(annotation).stream().filter(Utils::canEnable).map(ClassInfo::loadClass).map((clazz) -> {
                return clazz;
            }).collect(Collectors.toSet());
        } catch (Throwable var6) {
            if (scan != null) {
                try {
                    scan.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
            }

            throw var6;
        }

        if (scan != null) {
            scan.close();
        }

        return var3;
    }

    public static Set<Method> methodsAnnotatedWith(final Class<?> clazz, final Class<? extends Annotation> annotation) {
        return new HashSet<Method>() {
            {
                Iterator var3 = ReflectionUtils.getAllMethods(clazz).iterator();

                while(var3.hasNext()) {
                    Method method = (Method)var3.next();
                    method.setAccessible(true);
                    if (method.getAnnotation(annotation) != null) {
                        this.add(method);
                    }
                }

            }
        };
    }

    private static ClassGraph scanPackages(String... packages) {
        ClassGraph scanner = (new ClassGraph()).acceptPackages(packages).enableClassInfo().enableAnnotationInfo().initializeLoadedClasses();
        if (AegisAPI.get().getClassLoader() != null) {
            scanner.overrideClassLoaders(new ClassLoader[]{AegisAPI.get().getClassLoader()});
        }

        return scanner;
    }

    private static <T> Set<Class<? extends T>> getClasses(String[] packages, Predicate<ClassInfo> filter) {
        ScanResult scan = scanPackages(packages).scan();

        Set var3;
        try {
            var3 = (Set)scan.getAllClasses().stream().filter(filter).map(ClassInfo::loadClass).map((clazz) -> {
                return clazz;
            }).collect(Collectors.toSet());
        } catch (Throwable var6) {
            if (scan != null) {
                try {
                    scan.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
            }

            throw var6;
        }

        if (scan != null) {
            scan.close();
        }

        return var3;
    }

    private static Set<Method> getAllMethods(final Class<?> clazz) {
        return new HashSet((new HashMap<String, Method>() {
            {
                Iterator var2 = Utils.reverse(ReflectionUtils.superclassesOf(clazz)).iterator();

                while(var2.hasNext()) {
                    Class<?> clazzx = (Class)var2.next();
                    Method[] var4 = clazzx.getDeclaredMethods();
                    int var5 = var4.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        Method method = var4[var6];
                        this.put(ReflectionUtils.getMethodKey(method), method);
                    }
                }

            }
        }).values());
    }

    private static @NotNull String getMethodKey(Method method) {
        String params = (String)Arrays.stream(method.getParameters()).map((parameter) -> {
            return parameter.getType().getSimpleName();
        }).collect(Collectors.joining(","));
        return "%s(%s)".formatted(method.getName(), params);
    }
}