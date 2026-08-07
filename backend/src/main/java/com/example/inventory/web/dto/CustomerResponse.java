package com.example.inventory.web.dto;

import com.example.inventory.domain.Customer;

public record CustomerResponse(Long id, String name, String email) {

    public static CustomerResponse of(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail());
    }
}
