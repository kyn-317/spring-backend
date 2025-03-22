package com.kyn.spring_backend.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class ResponseDto<T> {

    private T data;

    private String message;

    private String status;

}
