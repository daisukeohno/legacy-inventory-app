package com.example.inventory.web;

import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.web.dto.CustomerResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerRepository.findAll().stream().map(CustomerResponse::of).toList();
    }
}
