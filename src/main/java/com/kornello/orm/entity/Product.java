package com.kornello.orm.entity;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Id;
import com.kornello.orm.annotation.Table;
import lombok.Data;

@Table("products")
@Data
public class Product {
    @Id
    private Long id;
    private String name;
    @Column
    private Long price;
}
