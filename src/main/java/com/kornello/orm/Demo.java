package com.kornello.orm;

import com.kornello.orm.entity.Product;
import com.kornello.orm.session.SessionFactory;
import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Demo {
    @SneakyThrows
    public static void main(String[] args) {

    }

    private static DataSource createDataSource() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/ProductsDatabase");
        dataSource.setUser("postgres");
        dataSource.setPassword("admin");
        return dataSource;
    }
}
