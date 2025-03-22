package com.kyn.spring_backend.base.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseDocuments {

    @Field("REGR_ID")
    private String regrId;

    @Field("REG_DT")
    private LocalDateTime regDt;

    @Field("AMDR_ID")
    private String amdrId;

    @Field("AMD_DT")
    private LocalDateTime amdDt;

    public void insertDocument(String id) {
        this.regrId = id;
        this.regDt = LocalDateTime.now();
        this.amdrId = id;
        this.amdDt = LocalDateTime.now();
    }

    public void updateDocument(String id) {
        this.amdrId = id;
        this.amdDt = LocalDateTime.now();
    }
}
