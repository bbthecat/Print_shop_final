package com.printflow.dto.form;

import com.printflow.domain.enums.Role;
import com.printflow.dto.request.AdminUserCreateRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserForm {

    @NotBlank(message = "กรุณากรอกชื่อผู้ใช้")
    @Size(min = 3, max = 50, message = "ชื่อผู้ใช้ต้องยาว 3-50 ตัวอักษร")
    @Pattern(regexp = "^[a-zA-Z0-9_.]*$", message = "ใช้ได้เฉพาะ a-z, 0-9, _ และ .")
    private String username;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 8, max = 100, message = "รหัสผ่านต้องยาวอย่างน้อย 8 ตัวอักษร")
    private String password;

    @NotBlank(message = "กรุณากรอกชื่อ")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 50)
    private String lastName;

    @NotNull(message = "กรุณาเลือก role")
    private Role role = Role.STAFF;

    public AdminUserCreateRequest toRequest() {
        return new AdminUserCreateRequest(username, email, password, firstName, lastName, null, role);
    }
}
