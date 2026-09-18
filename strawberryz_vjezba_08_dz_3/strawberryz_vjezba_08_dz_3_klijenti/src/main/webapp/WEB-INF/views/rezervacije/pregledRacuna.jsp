<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled vlastitih računa</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
</head>
<body>
  <h2>Pregled vlastitih računa</h2>
  <c:if test="${korisnik != null}">
    <p class="korisnik-traka">Prijavljen je korisnik: <strong>${korisnik}</strong></p>
  </c:if>

  <p>
    <c:choose>
      <c:when test="${mozeOtvoritiNovi}">
        <a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/otvoriRacun">Otvori novi račun za kupovinu avio karata</a>
      </c:when>
      <c:otherwise>
        <span title="Već postoji otvoreni račun">Otvori novi račun (nedostupno)</span>
      </c:otherwise>
    </c:choose>
  </p>

  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Vrijeme</th>
        <th>Korisnik</th>
        <th>Broj stavki</th>
        <th>Stvarni broj stavki</th>
        <th>Iznos</th>
        <th>Status</th>
        <th>Stavke</th>
        <th>Akcija</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${racuni}">
        <tr>
          <td class="desno">${r.id}</td>
          <td>${r.vrijeme}</td>
          <td>${r.korisnik}</td>
          <td class="desno">${r.brojStavki}</td>
          <td class="desno">${r.stvarniBrojStavki}</td>
          <td class="desno"><fmt:formatNumber value="${r.iznos}" minFractionDigits="2" maxFractionDigits="2"/></td>
          <td>${r.otvoren ? 'OTVOREN' : 'ZATVOREN'}</td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledStavkiRacuna/${r.id}">Pregled stavki</a>
          </td>
          <td>
            <c:choose>
              <c:when test="${r.otvoren and r.uskladjen}">
                <a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/zatvoriRacun/${r.id}">Zatvori račun</a>
              </c:when>
              <c:when test="${r.otvoren and not r.uskladjen}">
                <span title="Broj stavki nije usklađen">Zatvori (nedostupno)</span>
              </c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">Privatni dio</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
