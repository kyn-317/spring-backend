package com.kyn.spring_backend.modules.product.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.kyn.spring_backend.base.entity.BaseDocuments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "create")
@Document("PRODUCT_BAS")
public class ProductBasEntity extends BaseDocuments {

    @Id
    private ObjectId _id;

    @Field("PRODUCT_NAME")
    private String productName;

    @Field("PRODUCT_CATEGORY")
    private String productCategory;

    @Field("PRODUCT_DESCRIPTION")
    private String productDescription;

    @Field("PRODUCT_SPECIFICATION")
    private String productSpecification;

    @Field("PRODUCT_PRICE")
    private String productPrice;

    @Field("PRODUCT_IMAGE")
    private String productImage;

}
