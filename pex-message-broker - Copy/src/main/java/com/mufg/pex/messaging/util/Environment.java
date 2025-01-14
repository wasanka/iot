package com.mufg.pex.messaging.util;

public class Environment {

    public static <T> T get(String key, T defaultValue) {

        T t = (T) System.getenv(key);

        return t != null ? t : defaultValue;
    }
}
