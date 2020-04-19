package net.a3h.ruuvi.sqlitedb;

import org.apache.commons.lang3.StringUtils;

public class SqliteDbConnectionSystemPropertyConfig {
    public static String getDbPath() {
        return getPropertySafely("sqlite.dbpath");
    }

    private static String getPropertySafely(String key) {
        String result = System.getProperty(key);
        if (StringUtils.isBlank(result)) {
            throw new IllegalArgumentException(String.format("System property %s was empty: %s", key, result));
        }
        return result;
    }
}
