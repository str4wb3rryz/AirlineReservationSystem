<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Inicijalizacija letova</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Inicijalizacija letova avio tvrtke</h2>
  <p>Inicijaliziraju se letovi avio tvrtke kojoj ste zastupnik u zadanom intervalu datuma.</p>

  <c:if test="${not empty poruka}">
    <p><strong>${poruka}</strong></p>
  </c:if>

  <form method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/inicijalizacijaLetova">
    <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
    Od datuma: <input type="date" lang="hr" name="odDatuma" value="${odDatuma}" required>
    Do datuma: <input type="date" lang="hr" name="doDatuma" value="${doDatuma}" required>
    <button type="submit">Inicijaliziraj letove</button>
  </form>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">Avio tvrtke</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
