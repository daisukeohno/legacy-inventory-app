<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>在庫・注文管理システム(旧)</title>
  <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div id="pageHeader">
  <h1>在庫・注文管理システム</h1>
  <table id="navTable" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td><a href="productList.do">商品(在庫)一覧</a></td>
      <td>|</td>
      <td><a href="orderList.do">注文一覧</a></td>
      <td>|</td>
      <td><a href="orderEdit.do">新規注文</a></td>
    </tr>
  </table>
  <hr>
</div>
