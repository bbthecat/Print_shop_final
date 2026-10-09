package com.printflow.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * error ของหน้าเว็บ (Thymeleaf) ที่เกิดจาก URL ผิด เช่น ?sort=foo หรือ /orders/abc
 * ให้แสดงหน้า error 400 แทน 500 (ฝั่ง API ใช้ GlobalExceptionHandler)
 */
@ControllerAdvice(basePackages = "com.printflow.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler({PropertyReferenceException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest() {
        ModelAndView view = new ModelAndView("error");
        view.addObject("status", HttpStatus.BAD_REQUEST.value());
        return view;
    }
}
