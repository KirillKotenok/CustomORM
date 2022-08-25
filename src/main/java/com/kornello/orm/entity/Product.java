package com.kornello.orm.entity;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Table;

import java.sql.Date;

@Table("products")
public class Product {
    private Long id;
    private String name;
    private Long price;
    @Column("created_at")
    private Date createAt;
}
