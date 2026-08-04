<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ include file="header.jsp" %>

<h2>商品(在庫)一覧</h2>

<form action="productList.do" method="get">
  キーワード:
  <input type="text" name="keyword" value="<bean:write name="keyword"/>" size="20">
  <input type="checkbox" name="lowStockOnly" value="true"
      <logic:equal name="lowStockOnly" value="true">checked</logic:equal> > 在庫少のみ
  <input type="submit" value="検索">
  <a href="productEdit.do">＋新規商品登録</a>
</form>

<table class="dataTable">
  <tr>
    <th>SKU</th>
    <th>商品名</th>
    <th>単価</th>
    <th>在庫数</th>
    <th>&nbsp;</th>
  </tr>
  <logic:iterate id="product" name="productList">
    <tr>
      <td><bean:write name="product" property="sku"/></td>
      <td><bean:write name="product" property="name"/></td>
      <td><bean:write name="product" property="price"/> 円</td>
      <td>
        <logic:equal name="product" property="lowStock" value="true">
          <span class="lowStock"><bean:write name="product" property="stockQuantity"/> (在庫少)</span>
        </logic:equal>
        <logic:equal name="product" property="lowStock" value="false">
          <bean:write name="product" property="stockQuantity"/>
        </logic:equal>
      </td>
      <td><a href="productEdit.do?id=<bean:write name="product" property="id"/>">編集</a></td>
    </tr>
  </logic:iterate>
</table>

<%@ include file="footer.jsp" %>
