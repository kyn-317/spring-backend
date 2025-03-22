package com.kyn.spring_backend.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class UserAuthDto {
    private String id;
    private String userObjectId;
    private String userEmail;
    private String userRole;

    private String regrId;
    private LocalDateTime regDt;
    private String amdrId;
    private LocalDateTime amdDt;
}