package com.example.legacy.form;

import org.apache.struts.action.ActionForm;

/**
 * 注文作成フォーム。
 * 商品IDと数量をチェックボックス+テキスト入力の並行配列で受け取る、
 * 典型的な旧世代Strutsの複数行入力パターン。
 */
public class OrderForm extends ActionForm {

    private String customerName;
    private String[] productIds = new String[0];
    private String[] quantities = new String[0];

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String[] getProductIds() {
        return productIds;
    }

    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    public String[] getQuantities() {
        return quantities;
    }

    public void setQuantities(String[] quantities) {
        this.quantities = quantities;
    }
}
