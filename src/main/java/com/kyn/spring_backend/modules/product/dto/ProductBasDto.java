package com.kyn.spring_backend.modules.product.dto;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class ProductBasDto {

    private String _id;

    private String productName;

    private String productCategory;

    private String productDescription;

    private String productSpecification;

    private String productPrice;

    private String productImage;

    private LocalDateTime regDt;

    private String regrId;

    private LocalDateTime amdDt;

    private String amdrId;

}
