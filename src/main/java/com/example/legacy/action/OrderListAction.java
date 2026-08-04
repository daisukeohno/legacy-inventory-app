package com.example.legacy.action;

import com.example.legacy.dao.OrderDao;
import com.example.legacy.model.Order;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class OrderListAction extends Action {

    private OrderDao orderDao = new OrderDao();

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<Order> orderList = orderDao.findAll();
        request.setAttribute("orderList", orderList);

        return mapping.findForward("success");
    }
}
