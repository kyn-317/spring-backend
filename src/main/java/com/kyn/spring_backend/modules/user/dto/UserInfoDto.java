package com.kyn.spring_backend.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class UserInfoDto {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPassword;
    private List<UserAuthDto> userAuths;

    private String regrId;
    private LocalDateTime regDt;
    private String amdrId;
    private LocalDateTime amdDt;
}