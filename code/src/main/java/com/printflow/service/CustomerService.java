package com.printflow.service;

import com.printflow.dto.request.CustomerRegisterRequest;
import com.printflow.dto.request.CustomerUpdateRequest;
import com.printflow.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse register(CustomerRegisterRequest request);

    CustomerResponse getById(Long id);

    Page<CustomerResponse> getAll(Pageable pageable);

    CustomerResponse update(Long id, CustomerUpdateRequest request);

    void deactivate(Long id);
}
