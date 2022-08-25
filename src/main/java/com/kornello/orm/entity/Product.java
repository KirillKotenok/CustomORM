package com.kornello.orm.entity;

import com.kornello.orm.annotation.Table;
import lombok.Data;

@Table(name = "products")
@Data
public class Product {
    private Long id;
    private String name;
    private Long price;
}
