package com.logistics.hub.feature.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    private String username;

    @NotBlank(message = "Ho ten khong duoc de trong")
    private String fullName;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 8, message = "Mat khau phai co it nhat 8 ky tu")
    private String password;

    @NotBlank(message = "Vai tro khong duoc de trong")
    private String role;

    private List<Long> assignedDepotIds;
}
