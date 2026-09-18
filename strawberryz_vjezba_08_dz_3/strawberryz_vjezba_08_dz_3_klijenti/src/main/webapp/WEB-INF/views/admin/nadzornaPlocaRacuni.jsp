<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Nadzorna ploča za račune</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
   <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h1>Nadzorna ploča za račune</h1>
  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/pocetak">Administracijski dio</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/obrisiSveRacune">Obriši sve račune iz liste</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>

  <table>
    <thead>
      <tr>
        <th>R.br.</th>
        <th>Id</th>
        <th>Vrijeme</th>
        <th>Korisnik</th>
        <th>Broj stavki</th>
        <th>Iznos</th>
        <th>Stavke</th>
        <th>Akcija</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${racuni}" varStatus="vs">
        <tr>
          <td class="desno">${vs.index}</td>
          <td class="desno">${r.id()}</td>
          <td>${r.vrijeme()}</td>
          <td>${r.korisnik()}</td>
          <td class="desno">${r.brojStavki()}</td>
          <td class="desno"><fmt:formatNumber value="${r.iznos()}" minFractionDigits="2" maxFractionDigits="2"/></td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/mvc/admin/pregledStavkiRacunaPoruke/${vs.index}">Pregled stavki</a>
          </td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/mvc/admin/obrisiRacunIzListe/${vs.index}">Obriši</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</body>
</html>
