package com.kornello.orm.entity;

import com.kornello.orm.annotation.Column;
import com.kornello.orm.annotation.Table;
import lombok.Data;

@Table("markets")
@Data
public class Markets {
    private Long id;
    private String name;
    @Column("sales_product")
    private Long salesProductId;
    private String address;
}
