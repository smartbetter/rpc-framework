package com.example.rpc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    public static Set<Class> getClassFromJar(String jarPath) throws IOException, ClassNotFoundException {
        // 读入jar文件
        JarFile jarFile = new JarFile(jarPath);
        return getClassFromJar(jarFile, "", "");
    }

    public static Set<Class> getClassFromJar(String jarPath, String keyword) throws IOException, ClassNotFoundException {
        // 读入jar文件
        JarFile jarFile = new JarFile(jarPath);
        return getClassFromJar(jarFile, keyword, "");
    }

    public static Set<Class> getClassFromJar(JarFile jarFile, String keyword, String basePakage) throws IOException {
        // 是否递归
        Boolean recursive = true;
        String packageName = basePakage;
        String packageDirName = basePakage.replace('.', '/');
        Enumeration<JarEntry> entries = jarFile.entries();
        Set<Class> classes = new LinkedHashSet<Class>();
        while (entries.hasMoreElements()) {
            try {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                // 如果是以/开头的
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }

                // 如果前半部分和定义的包名相同
                if (name.startsWith(packageDirName)) {
                    int idx = name.lastIndexOf('/');
                    // 如果以"/"结尾 是一个包
                    if (idx != -1) {
                        // 获取包名 把"/"替换成"."
                        packageName = name.substring(0, idx).replace('/', '.');
                    }
                    // 如果可以迭代下去 并且是一个包
                    if ((idx != -1) || recursive) {
                        // 如果是一个.class文件 而且不是目录
                        if (name.endsWith(".class")
                                && !entry.isDirectory()) {
                            //检测entry是否符合要求
                            if (!ClassUtil.checkJarEntry(jarFile, entry, keyword)) {
                                continue;
                            }
                            // 去掉后面的".class" 获取真正的类名
                            String className = name.substring(
                                    packageName.length() + 1, name.length() - 6);
                            try {
                                // 添加到classes
                                Class c = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                                classes.add(c);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    public static boolean checkJarEntry(JarFile jarFile, JarEntry entry, String keyWord) throws IOException {
        if (keyWord == null || keyWord.equals("")) {
            return true;
        }
        InputStream input = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;

        try {
            input = jarFile.getInputStream(entry);
            isr = new InputStreamReader(input);
            reader = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();
            boolean result = false;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                if (sb.indexOf(keyWord) > -1) {
                    result = true;
                }
            }
            return result;
        } finally {
            if (input != null) {
                input.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static Class<?> getClassForName(String name) throws ClassNotFoundException {
        if ("boolean".equals(name)) {
            return Boolean.class;
        } else if ("char".equals(name)) {
            return Character.class;
        } else if ("byte".equals(name)) {
            return Byte.class;
        } else if ("short".equals(name)) {
            return Short.class;
        } else if ("int".equals(name)) {
            return Integer.class;
        } else if ("long".equals(name)) {
            return Long.class;
        } else if ("float".equals(name)) {
            return Float.class;
        } else if ("double".equals(name)) {
            return Double.class;
        } else {
            return Class.forName(name);
        }
    }
}
