<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Stavke primljenog računa</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h1>Stavke primljenog računa</h1>
  <c:if test="${racun != null}">
    <p>
      Račun br. <strong>${racun.id()}</strong> |
      Korisnik: <strong>${racun.korisnik()}</strong> |
      Broj stavki: ${racun.brojStavki()} |
      Iznos: <fmt:formatNumber value="${racun.iznos()}" minFractionDigits="2" maxFractionDigits="2"/>
    </p>
  </c:if>

  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Račun</th>
        <th>Ime putnika</th>
        <th>Prezime putnika</th>
        <th>Rezervacija</th>
        <th>Iznos</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="s" items="${stavke}">
        <tr>
          <td class="desno">${s.id()}</td>
          <td class="desno">${s.racunId()}</td>
          <td>${s.putnikIme()}</td>
          <td>${s.putnikPrezime()}</td>
          <td>${s.rezervacija()}</td>
          <td class="desno"><fmt:formatNumber value="${s.iznos()}" minFractionDigits="2" maxFractionDigits="2"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPlocaRacuni">Nadzorna ploča za račune</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
