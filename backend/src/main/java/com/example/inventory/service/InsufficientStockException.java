package com.example.inventory.service;

/** 在庫不足を表す業務例外。HTTP 409 に対応する。 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
