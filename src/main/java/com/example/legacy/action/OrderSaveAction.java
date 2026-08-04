package com.example.legacy.action;

import com.example.legacy.dao.OrderDao;
import com.example.legacy.dao.ProductDao;
import com.example.legacy.form.OrderForm;
import com.example.legacy.model.Order;
import com.example.legacy.model.OrderItem;
import com.example.legacy.model.Product;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 注文保存アクション。
 *
 * 業務ロジック(在庫チェック・在庫引き落とし・合計金額計算)がAction内に直接書かれており、
 * サービス層が存在しない。トランザクション境界もOrderDao.save()とProductDao.decreaseStock()
 * で分断されているため、片方だけ成功する不整合が起こり得る。
 * -> Devinでのマイグレーション時に「サービス層への切り出し」「@Transactionalの導入」の
 *    良いデモポイントになる。
 */
public class OrderSaveAction extends Action {

    private ProductDao productDao = new ProductDao();
    private OrderDao orderDao = new OrderDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        OrderForm orderForm = (OrderForm) form;

        if (orderForm.getCustomerName() == null || orderForm.getCustomerName().trim().length() == 0) {
            request.setAttribute("errorMessage", "得意先名を入力してください。");
            List<Product> availableProducts = productDao.findAll();
            request.setAttribute("availableProducts", availableProducts);
            return mapping.findForward("failure");
        }

        Order order = new Order();
        order.setCustomerName(orderForm.getCustomerName());
        order.setOrderDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        order.setStatus("NEW");

        String[] productIds = orderForm.getProductIds();
        String[] quantities = orderForm.getQuantities();

        boolean hasItem = false;

        for (int i = 0; i < productIds.length; i++) {
            String qtyStr = (i < quantities.length) ? quantities[i] : null;
            if (qtyStr == null || qtyStr.trim().length() == 0) {
                continue;
            }
            int quantity = 0;
            try {
                quantity = Integer.parseInt(qtyStr.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (quantity <= 0) {
                continue;
            }

            int productId = Integer.parseInt(productIds[i]);
            Product product = productDao.findById(productId);
            if (product == null) {
                continue;
            }

            boolean stockOk = productDao.decreaseStock(productId, quantity);
            if (!stockOk) {
                request.setAttribute("errorMessage",
                        "「" + product.getName() + "」の在庫が不足しています(在庫数: " + product.getStockQuantity() + ")。");
                List<Product> availableProducts = productDao.findAll();
                request.setAttribute("availableProducts", availableProducts);
                return mapping.findForward("failure");
            }

            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), quantity));
            hasItem = true;
        }

        if (!hasItem) {
            request.setAttribute("errorMessage", "少なくとも1つの商品を数量1以上で選択してください。");
            List<Product> availableProducts = productDao.findAll();
            request.setAttribute("availableProducts", availableProducts);
            return mapping.findForward("failure");
        }

        orderDao.save(order);

        return mapping.findForward("success");
    }
}
