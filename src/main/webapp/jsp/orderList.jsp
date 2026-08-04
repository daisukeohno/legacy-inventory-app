<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ include file="header.jsp" %>

<h2>注文一覧</h2>

<a href="orderEdit.do">＋新規注文</a>

<table class="dataTable">
  <tr>
    <th>注文番号</th>
    <th>得意先</th>
    <th>注文日</th>
    <th>状態</th>
    <th>明細</th>
    <th>合計金額</th>
  </tr>
  <logic:iterate id="order" name="orderList">
    <tr>
      <td><bean:write name="order" property="id"/></td>
      <td><bean:write name="order" property="customerName"/></td>
      <td><bean:write name="order" property="orderDate"/></td>
      <td><bean:write name="order" property="status"/></td>
      <td>
        <table border="0" cellpadding="0" cellspacing="0">
          <logic:iterate id="item" name="order" property="items">
            <tr>
              <td><bean:write name="item" property="productName"/></td>
              <td>&nbsp;x&nbsp;<bean:write name="item" property="quantity"/></td>
              <td>&nbsp;=&nbsp;<bean:write name="item" property="subtotal"/>円</td>
            </tr>
          </logic:iterate>
        </table>
      </td>
      <td><bean:write name="order" property="totalAmount"/> 円</td>
    </tr>
  </logic:iterate>
</table>

<%@ include file="footer.jsp" %>
