package com.kornello.orm.sql;

public enum SQLEnumeration {
    INSERT(""),
    SELECT(""),
    UPDATE(""),
    DELETE("");

    private String sql;

    SQLEnumeration(String sql) {
        this.sql = sql;
    }
}
