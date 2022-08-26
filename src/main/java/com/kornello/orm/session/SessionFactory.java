package com.kornello.orm.session;

import lombok.experimental.UtilityClass;

import javax.sql.DataSource;

@UtilityClass
public class SessionFactory {
    public static Session createSession(DataSource dataSource) {
        return new Session(dataSource);
    }
}
