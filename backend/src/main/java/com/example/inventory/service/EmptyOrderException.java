package com.example.inventory.service;

/** 数量1以上の有効な明細が1件も無い注文。旧 OrderSaveAction の hasItem チェック相当。 */
public class EmptyOrderException extends RuntimeException {

    public EmptyOrderException() {
        super("少なくとも1つの商品を数量1以上で選択してください。");
    }
}
