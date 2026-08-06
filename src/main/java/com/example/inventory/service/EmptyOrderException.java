package com.example.inventory.service;

/** 有効な明細が0件。旧 OrderSaveAction の "少なくとも1つの商品を..." に相当。 */
public class EmptyOrderException extends RuntimeException {

    public EmptyOrderException() {
        super("少なくとも1つの商品を数量1以上で選択してください。");
    }
}
