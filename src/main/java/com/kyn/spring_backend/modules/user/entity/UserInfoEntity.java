package com.kyn.spring_backend.modules.user.entity;

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
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
@Document("USER_INFO")
public class UserInfoEntity extends BaseDocuments {
    @Id
    private ObjectId _id;

    @Field("USER_ID")
    private String userId;

    @Field("USER_NAME")
    private String userName;

    @Field("USER_EMAIL")
    private String userEmail;

    @Field("USER_PASSWORD")
    private String userPassword;

}
