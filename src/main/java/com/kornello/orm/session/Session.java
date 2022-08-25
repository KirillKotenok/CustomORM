package com.kornello.orm.session;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Session {
    private DataSource dataSource;
    private Map<Object, Object> sessionCache = new HashMap<>();
    private Map<Object, Object> sessionSnapshot = new HashMap<>();

    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
