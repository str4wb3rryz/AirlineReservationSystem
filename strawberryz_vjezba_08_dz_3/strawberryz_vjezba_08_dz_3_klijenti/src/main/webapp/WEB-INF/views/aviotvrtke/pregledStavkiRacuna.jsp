<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Stavke računa zastupnika</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Stavke računa br. ${racun.id}</h2>
  <p>
    Zastupnik: <strong>${racun.korisnik}</strong> |
    Status: <strong>${racun.otvoren ? 'OTVOREN' : 'ZATVOREN'}</strong> |
    Broj stavki (upisano): ${racun.brojStavki} |
    Stvarni broj stavki: ${racun.stvarniBrojStavki}
  </p>

  <p>
    <c:choose>
      <c:when test="${mozeUskladiti}">
        <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/uskladiRacun/${racun.id}">Uskladi broj stavki računa sa stvarnim brojem</a>
      </c:when>
      <c:otherwise>
        <span title="Usklađivanje nije potrebno ili nije dopušteno">Usklađivanje (nedostupno)</span>
      </c:otherwise>
    </c:choose>
  </p>

  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Ime putnika</th>
        <th>Prezime putnika</th>
        <th>Rezervacija</th>
        <th>Iznos</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="s" items="${stavke}">
        <tr>
          <td class="desno">${s.id}</td>
          <td>${s.putnikime}</td>
          <td>${s.putnikiprezime}</td>
          <td>${s.rezervacija}</td>
          <td class="desno"><fmt:formatNumber value="${s.iznos}" minFractionDigits="2" maxFractionDigits="2"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledRacuna">Pregled računa zastupnika</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
