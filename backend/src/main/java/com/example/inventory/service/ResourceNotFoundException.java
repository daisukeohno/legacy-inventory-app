package com.example.inventory.service;

/** 対象データが存在しない場合の例外。HTTP 404 に対応する。 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
