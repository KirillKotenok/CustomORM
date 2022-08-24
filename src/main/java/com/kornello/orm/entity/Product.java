package com.kornello.orm.entity;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Table;
import lombok.Data;

@Table(name = "products")
@Data
public class Product {
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private Long price;
}
