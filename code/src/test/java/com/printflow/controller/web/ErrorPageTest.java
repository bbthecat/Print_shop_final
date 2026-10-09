package com.printflow.controller.web;

import com.printflow.config.SecurityConfig;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * หน้า error.html ภาษาไทยแทนหน้า Whitelabel ของ Spring
 */
@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class ErrorPageTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.printflow.service.ServiceCatalogQueryService catalogQueryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notFound_showsThaiErrorPage() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(content().string(containsString("ไม่พบหน้าหรือข้อมูลที่ต้องการ")));
    }

    @Test
    void forbidden_showsThaiErrorPage() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(content().string(containsString("คุณไม่มีสิทธิ์เข้าถึงหน้านี้")));
    }
}
