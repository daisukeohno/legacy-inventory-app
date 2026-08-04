package com.example.legacy.form;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * 商品編集フォーム。
 * price/stockQuantityをString型で受けているのは、HTMLの<html:text>タグから
 * そのままバインドするための典型的な旧Struts流儀(数値変換はAction側で手動実施)。
 */
public class ProductForm extends ActionForm {

    private String id;
    private String sku;
    private String name;
    private String price;
    private String stockQuantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(String stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (sku == null || sku.trim().length() == 0) {
            errors.add("sku", new ActionMessage("error.product.sku.required", (Object) null));
        }
        if (name == null || name.trim().length() == 0) {
            errors.add("name", new ActionMessage("error.product.name.required", (Object) null));
        }
        try {
            double p = Double.parseDouble(price);
            if (p < 0) {
                errors.add("price", new ActionMessage("error.product.price.negative", (Object) null));
            }
        } catch (Exception e) {
            errors.add("price", new ActionMessage("error.product.price.invalid", (Object) null));
        }
        try {
            int s = Integer.parseInt(stockQuantity);
            if (s < 0) {
                errors.add("stockQuantity", new ActionMessage("error.product.stock.negative", (Object) null));
            }
        } catch (Exception e) {
            errors.add("stockQuantity", new ActionMessage("error.product.stock.invalid", (Object) null));
        }
        return errors;
    }
}
