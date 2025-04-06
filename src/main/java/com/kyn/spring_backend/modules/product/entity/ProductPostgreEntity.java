package com.kyn.spring_backend.modules.product.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "PRODUCT", name = "PRODUCT_BAS") 
public class ProductPostgreEntity {
    @Id
    private Long id;
    private String name;
    private String category;
    private String description;
    private String price;
    private String image;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}