<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled rezervacija</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Pregled mojih rezervacija</h2>

  <form method="get" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledRezervacija">
    Od datuma: <input type="date" lang="hr" name="odDatuma" value="${odDatuma}">
    Do datuma: <input type="date" lang="hr" name="doDatuma" value="${doDatuma}">
    <button type="submit">Pretraži</button>
  </form>

  <c:if test="${not otvorenRacun}">
    <p><em>Nemate otvoreni račun - potvrđivanje rezervacija nije moguće dok ne otvorite račun.</em></p>
  </c:if>

  <table border="1">
    <thead>
      <tr>
        <th>Br. rezervacije</th>
        <th>Oznaka leta</th>
        <th>Datum polijetanja</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Status</th>
        <th>Akcija</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${rezervacije}">
        <tr>
          <td>${r.id()}</td>
          <td>${r.sjedalo().let().letPodaci().oznakaLeta()}</td>
          <td>${r.sjedalo().let().datumPolijetanja()}</td>
          <td>${r.sjedalo().let().letPodaci().vrijemePolijetanja()}</td>
          <td>${r.sjedalo().let().letPodaci().polazniAerodrom()}</td>
          <td>${r.sjedalo().let().letPodaci().odredisniAerodrom()}</td>
          <td>${r.statusRezervacije()}</td>
          <td>
            <c:choose>
              <c:when test="${r.statusRezervacije() == 'KREIRANA' and otvorenRacun}">
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/potvrdi/${r.id()}">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="oznakaLeta" value="${r.sjedalo().let().letPodaci().oznakaLeta()}">
                  <input type="hidden" name="idLeta" value="${r.sjedalo().let().id()}">
                  <input type="hidden" name="odDatuma" value="${odDatuma}">
                  <input type="hidden" name="doDatuma" value="${doDatuma}">
                  <button type="submit">Potvrdi</button>
                </form>
              </c:when>
              <c:when test="${r.statusRezervacije() == 'KREIRANA' and not otvorenRacun}">
                <span title="Nemate otvoreni račun">Potvrdi (nedostupno)</span>
              </c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>