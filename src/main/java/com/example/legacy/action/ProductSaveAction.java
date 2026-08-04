package com.example.legacy.action;

import com.example.legacy.dao.ProductDao;
import com.example.legacy.form.ProductForm;
import com.example.legacy.model.Product;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 商品の保存(新規/更新)。ActionForm.validate()でチェックが通った後、
 * ここで文字列→数値の変換を再度行っている(重複したパースロジック)。
 */
public class ProductSaveAction extends Action {

    private ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        ProductForm productForm = (ProductForm) form;

        Product product = new Product();
        if (productForm.getId() != null && productForm.getId().trim().length() > 0) {
            product.setId(Integer.parseInt(productForm.getId()));
        }
        product.setSku(productForm.getSku());
        product.setName(productForm.getName());
        product.setPrice(Double.parseDouble(productForm.getPrice()));
        product.setStockQuantity(Integer.parseInt(productForm.getStockQuantity()));

        productDao.save(product);

        return mapping.findForward("success");
    }
}
