package com.kornello.orm.session;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Id;
import com.kornello.orm.annotation.Table;
import com.kornello.orm.exception.SessionException;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Session {
    private DataSource dataSource;
    private String SELECT_BY_ID_SQL_TEMPLATE = "SELECT * FROM %s WHERE id = ?";
    private String UPDATE_ENTITY_SQL_TEMPLATE = "UPDATE %s SET %s WHERE id = %s";
    private Map<EntityKey<?>, Object> sessionCacheMap = new HashMap<>();
    private Map<EntityKey<?>, Object> sessionSnapshotMap = new HashMap<>();

    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T find(Class<T> entityType, Object id) {
        var entityKey = new EntityKey<T>(entityType, id);
        var entityFromDb = sessionCacheMap.computeIfAbsent(entityKey, this::getEntityFromDb);
        return entityType.cast(entityFromDb);
    }

    @SneakyThrows
    public void update(Object entity) {
        @Cleanup Connection connection = dataSource.getConnection();
        @Cleanup PreparedStatement preparedStatement =
                connection.prepareStatement(prepareUpdateSql(entity));
        preparedStatement.execute();
    }

    private String prepareUpdateSql(Object entity) throws IllegalAccessException {
        Collection<String> updateFieldCollection = new ArrayList<>();
        Object id = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                id = field.get(entity);
            } else {
                var columnName = getColumnName(field);
                var columnValue = field.get(entity).toString();
                if (field.getType().isAssignableFrom(String.class) || field.getType().isAssignableFrom(Character.class)) {
                    updateFieldCollection.add("%s = '%s'".formatted(columnName, columnValue));
                } else {
                    updateFieldCollection.add("%s = %s".formatted(columnName, columnValue));
                }
            }
        }
        if (id == null) {
            throw new SessionException("Cannot update not persistence entity");
        }
        return String.format(UPDATE_ENTITY_SQL_TEMPLATE,
                getTableName(entity.getClass()),
                updateFieldCollection.stream()
                        .collect(Collectors.joining(",")),
                id);
    }


    public void close() {
        sessionCacheMap.entrySet().stream()
                .filter(this::isEntityNotConsistency)
                .map(Map.Entry::getValue)
                .forEach(this::update);
        sessionCacheMap.clear();
        sessionSnapshotMap.clear();
    }

    @SneakyThrows
    private boolean isEntityNotConsistency(Map.Entry<EntityKey<?>, Object> entry) {
        var actualEntity = entry.getValue();
        var databaseRepresentEntity = sessionSnapshotMap.get(entry.getKey());
        return !Arrays.stream(entry.getKey().entityType().getDeclaredFields())
                .allMatch(field ->
                        getEntityFieldData(field, actualEntity).equals(getEntityFieldData(field, databaseRepresentEntity)));
    }

    @SneakyThrows
    private <T> T getEntityFromDb(EntityKey<T> entityKey) {
        @Cleanup Connection connection = dataSource.getConnection();
        @Cleanup PreparedStatement selectStatement = connection.prepareStatement(
                String.format(SELECT_BY_ID_SQL_TEMPLATE, getTableName(entityKey.entityType)));
        selectStatement.setObject(1, entityKey.id());

        ResultSet rs = selectStatement.executeQuery();
        T entity = getEntityFromResultSet(entityKey.entityType(), rs);
        doSnapshot(entityKey, entity);
        return entity;
    }

    @SneakyThrows
    private <T> void doSnapshot(EntityKey<T> entityKey, T entity) {
        Objects.requireNonNull(entity);
        sessionSnapshotMap.put(entityKey, doReplica(entity));
    }

    @SneakyThrows
    private <T> T doReplica(T entity) {
        var replica = entity.getClass().getDeclaredConstructor().newInstance();
        for (Field field : replica.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            field.set(replica, field.get(entity));
        }
        return (T) replica;
    }

    @SneakyThrows
    private Object getEntityFieldData(Field field, Object entity) {
        field.setAccessible(true);
        return field.get(entity);
    }

    @SneakyThrows
    private <T> T getEntityFromResultSet(Class<T> entityType, ResultSet rs) {
        rs.next();
        var entity = entityType.getDeclaredConstructor().newInstance();
        Arrays.stream(entityType.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .forEach(field -> fillEntityFields(field, entity, rs));
        return entity;
    }

    @SneakyThrows
    private void fillEntityFields(Field declaredField, Object entity, ResultSet rs) {
        var columnName = getColumnName(declaredField);
        declaredField.setAccessible(true);
        declaredField.set(entity, rs.getObject(columnName));
    }

    private String getColumnName(Field declaredField) {
        if (declaredField.isAnnotationPresent(Column.class) && !declaredField.getAnnotation(Column.class).value().isBlank()) {
            return declaredField.getAnnotation(Column.class).value();
        } else {
            return declaredField.getName();
        }
    }

    private <T> String getTableName(Class<T> entityType) {
        return entityType.isAnnotationPresent(Table.class) ?
                entityType.getAnnotation(Table.class).value() :
                entityType.getSimpleName().toLowerCase();
    }

    private record EntityKey<T>(Class<T> entityType, Object id) {
    }
}
