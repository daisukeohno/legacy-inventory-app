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
 * 商品の新規/編集フォーム表示。idパラメータがあれば既存商品をロードする。
 */
public class ProductEditAction extends Action {

    private ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        ProductForm productForm = (ProductForm) form;
        String idParam = request.getParameter("id");

        if (idParam != null && idParam.trim().length() > 0) {
            int id = Integer.parseInt(idParam);
            Product product = productDao.findById(id);
            if (product != null) {
                productForm.setId(String.valueOf(product.getId()));
                productForm.setSku(product.getSku());
                productForm.setName(product.getName());
                productForm.setPrice(String.valueOf(product.getPrice()));
                productForm.setStockQuantity(String.valueOf(product.getStockQuantity()));
            }
        } else {
            productForm.setId("");
            productForm.setSku("");
            productForm.setName("");
            productForm.setPrice("");
            productForm.setStockQuantity("");
        }

        return mapping.findForward("success");
    }
}
