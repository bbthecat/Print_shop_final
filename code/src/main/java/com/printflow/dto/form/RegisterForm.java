package com.printflow.dto.form;

import com.printflow.dto.request.CustomerRegisterRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class RegisterForm {

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

    private String confirmPassword;

    @NotBlank(message = "กรุณากรอกชื่อ")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 50)
    private String lastName;

    @Size(max = 20)
    private String phoneNumber;

    private String address;

    @AssertTrue(message = "รหัสผ่านไม่ตรงกัน")
    public boolean isPasswordConfirmed() {
        return Objects.equals(password, confirmPassword);
    }

    public CustomerRegisterRequest toRequest() {
        return new CustomerRegisterRequest(username, email, password, firstName, lastName, phoneNumber, address);
    }
}
