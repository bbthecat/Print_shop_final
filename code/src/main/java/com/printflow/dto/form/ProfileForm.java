package com.printflow.dto.form;

import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "กรุณากรอกชื่อ")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 50)
    private String lastName;

    @Size(max = 20, message = "เบอร์โทรยาวเกินไป")
    private String phoneNumber;

    private String address;

    // เติมค่าเดิมของผู้ใช้ลงฟอร์ม
    public static ProfileForm from(CustomerResponse customer) {
        ProfileForm form = new ProfileForm();
        form.setEmail(customer.email());
        form.setFirstName(customer.firstName());
        form.setLastName(customer.lastName());
        form.setPhoneNumber(customer.phoneNumber());
        form.setAddress(customer.address());
        return form;
    }

    public CustomerUpdateRequest toRequest() {
        return new CustomerUpdateRequest(email, firstName, lastName, phoneNumber, address);
    }
}
