package com.kornello.orm;

import com.kornello.orm.entity.Product;
import com.kornello.orm.session.Session;
import com.kornello.orm.session.SessionFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Demo {
    public static void main(String[] args) {
        Session session = SessionFactory.createSession(createDataSource());
        var porosiatko_1 = session.find(Product.class, 1L);
        System.out.println(porosiatko_1);
        var porosiatko_2 = session.find(Product.class, 1L);
        System.out.println(porosiatko_2);
        System.out.println(porosiatko_1==porosiatko_2);

        //get product after session close
        session.close();
        var porosiatko_3 = session.find(Product.class, 1L);
        System.out.println(porosiatko_3);
        System.out.println(porosiatko_1==porosiatko_3);
        System.out.println(porosiatko_2==porosiatko_3);
    }

    private static DataSource createDataSource() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/ProductsDatabase");
        dataSource.setUser("postgres");
        dataSource.setPassword("admin");
        return dataSource;
    }
}
