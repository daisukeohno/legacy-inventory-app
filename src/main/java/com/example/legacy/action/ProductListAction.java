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
 * 商品(在庫)一覧 + キーワード/低在庫での絞り込み。
 * 検索ロジックがサービス層ではなくAction内(実際はDAO内)にベタ書きされている。
 */
public class ProductListAction extends Action {

    private ProductDao productDao = new ProductDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        String keyword = request.getParameter("keyword");
        boolean lowStockOnly = "true".equals(request.getParameter("lowStockOnly"));

        List<Product> productList = productDao.search(keyword, lowStockOnly);

        request.setAttribute("productList", productList);
        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("lowStockOnly", lowStockOnly);

        return mapping.findForward("success");
    }
}
