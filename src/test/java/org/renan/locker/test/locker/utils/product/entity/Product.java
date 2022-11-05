package org.renan.locker.test.locker.utils.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class Product {

    private Integer id;

    private String name;

    private BigDecimal price;

    public String toString(){
        return String.format("%d:%s",this.getId(), this.getName());
    }

}
