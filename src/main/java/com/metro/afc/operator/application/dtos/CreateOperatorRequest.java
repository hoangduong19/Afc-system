package com.metro.afc.operator.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOperatorRequest(
        @NotBlank(message = "Code không được để trống")
        @Size(max = 50, message = "Code tối đa 50 ký tự")
        String code,

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 255, message = "Tên tối đa 255 ký tự")
        String name
) {
}
