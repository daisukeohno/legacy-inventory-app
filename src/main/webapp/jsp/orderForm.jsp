<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ include file="header.jsp" %>

<h2>新規注文</h2>

<logic:present name="errorMessage">
  <div class="errorBox"><bean:write name="errorMessage"/></div>
</logic:present>

<html:form action="/orderSave">
  <table class="formTable">
    <tr>
      <td>得意先名</td>
      <td><html:text property="customerName" size="30"/></td>
    </tr>
  </table>

  <h3>商品を選択</h3>
  <table class="dataTable">
    <tr>
      <th>SKU</th>
      <th>商品名</th>
      <th>単価</th>
      <th>在庫数</th>
      <th>注文数量</th>
    </tr>
    <logic:iterate id="product" name="availableProducts">
      <tr>
        <td><bean:write name="product" property="sku"/></td>
        <td><bean:write name="product" property="name"/></td>
        <td><bean:write name="product" property="price"/> 円</td>
        <td><bean:write name="product" property="stockQuantity"/></td>
        <td>
          <input type="hidden" name="productIds" value="<bean:write name="product" property="id"/>">
          <input type="text" name="quantities" value="0" size="4">
        </td>
      </tr>
    </logic:iterate>
  </table>

  <br>
  <html:submit value="注文確定"/>
  <a href="orderList.do">キャンセル</a>
</html:form>

<%@ include file="footer.jsp" %>
