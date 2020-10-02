package com.hss.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
public class SPIExtensionLoader {
    public static <T> T loadExtension(Class<T> clazz) {
        List<T> extensions = findExtensions(clazz);
        if (extensions != null && extensions.size() > 0) {
            if (extensions.size() > 1) {
                throw new IllegalArgumentException(String.format("There is multiple implemention of :%s", clazz.getName()));
            } else {
                return extensions.get(0);
            }
        } else {
            throw new IllegalArgumentException(String.format("There is no implemention of :%s", clazz.getName()));
        }
    }

    private static <T> List<T> findExtensions(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Iterator<T> iterator = ServiceLoader.load(clazz, SPIExtensionLoader.class.getClassLoader()).iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
