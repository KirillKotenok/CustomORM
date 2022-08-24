package com.kornello.orm.session;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Table;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Session {
    private final DataSource dataSource;
    private final String SELECT_SQL_TEMPLATE = "SELECT * FROM %s WHERE id = ?";
    private final Map<Object, Object> sessionCache = new HashMap<>();

    @SneakyThrows
    public <T> T find(Class<T> entityType, Object id) {
        if (sessionCache.containsKey(id)) {
            return (T) sessionCache.get(id);
        }

        @Cleanup Connection connection = dataSource.getConnection();
        String selectSql = String.format(SELECT_SQL_TEMPLATE, getTableName(entityType));
        @Cleanup PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setObject(1, id);

        ResultSet resultSet = selectStatement.executeQuery();
        T entityFromDB = createEntityFromResultSet(entityType, resultSet);
        sessionCache.put(id, entityFromDB);
        return entityFromDB;
    }

    @SneakyThrows
    private <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
        resultSet.next();
        var entity = entityType.getConstructor().newInstance();
        for (Field declaredField : entityType.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Column.class)) {
                var name = getColumnName(declaredField);
                var fieldFromDb = resultSet.getObject(name);
                declaredField.setAccessible(true);
                declaredField.set(entity, fieldFromDb);
            }
        }
        return entity;
    }

    public void close() {
        sessionCache.clear();
    }

    private <T> String getTableName(Class<T> entityType) {
        return entityType.getAnnotation(Table.class).name();
    }

    private <T> String getColumnName(Field field) {
        return field.getAnnotation(Column.class).name();
    }
}
