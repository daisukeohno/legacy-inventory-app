<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ include file="header.jsp" %>

<h2>商品登録・編集</h2>

<html:errors/>

<html:form action="/productSave">
  <html:hidden property="id"/>
  <table class="formTable">
    <tr>
      <td>SKU</td>
      <td><html:text property="sku" size="20"/></td>
    </tr>
    <tr>
      <td>商品名</td>
      <td><html:text property="name" size="40"/></td>
    </tr>
    <tr>
      <td>単価(円)</td>
      <td><html:text property="price" size="10"/></td>
    </tr>
    <tr>
      <td>在庫数</td>
      <td><html:text property="stockQuantity" size="10"/></td>
    </tr>
  </table>
  <html:submit value="保存"/>
  <a href="productList.do">キャンセル</a>
</html:form>

<%@ include file="footer.jsp" %>
