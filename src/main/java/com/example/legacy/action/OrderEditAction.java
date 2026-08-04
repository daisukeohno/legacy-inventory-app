package com.example.legacy.action;

import com.example.legacy.dao.ProductDao;
import com.example.legacy.model.Product;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 注文の新規作成フォーム表示。選択可能な商品一覧をリクエスト属性に積む。
 */
public class OrderEditAction extends Action {

    private ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<Product> availableProducts = productDao.findAll();
        request.setAttribute("availableProducts", availableProducts);

        return mapping.findForward("success");
    }
}
