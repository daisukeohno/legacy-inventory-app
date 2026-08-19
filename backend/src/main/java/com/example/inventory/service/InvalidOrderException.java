package com.example.inventory.service;

/** 注文内容が業務ルール上不正な場合の例外。HTTP 400 に対応する。 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
