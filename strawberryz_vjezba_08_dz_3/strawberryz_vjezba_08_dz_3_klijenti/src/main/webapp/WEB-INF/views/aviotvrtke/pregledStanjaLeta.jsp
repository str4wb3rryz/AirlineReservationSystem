<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Stanje leta</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Podaci leta ${oznakaLeta} na dan ${datum}</h2>
  <c:if test="${not empty poruka}">
    <p><strong>${poruka}</strong></p>
  </c:if>

  <h3>Podaci o letu</h3>
  <table>
    <thead>
      <tr>
        <th>Oznaka leta</th>
        <th>Datum polijetanja</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>${oznakaLeta}</td>
        <td>${datum}</td>
        <td>${vrijemePolijetanja}</td>
        <td>${polazniAerodrom}</td>
        <td>${odredisniAerodrom}</td>
      </tr>
    </tbody>
  </table>

  <c:choose>
    <c:when test="${idLeta != null and imaStanje}">
      <h3>Broj sjedala prema statusima</h3>
      <table>
        <thead>
          <tr>
            <th>Slobodna</th>
            <th>Rezervirana (nepotvrđena)</th>
            <th>Potvrđena</th>
            <th>Zatvorena</th>
            <th>Nevažeća</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>${brojSlobodna}</td>
            <td>${brojKreirana}</td>
            <td>${brojPotvrdena}</td>
            <td>${brojZatvorena}</td>
            <td>${brojNevazeca}</td>
          </tr>
        </tbody>
      </table>

      <h3>Zatvaranje sjedala u zadanom intervalu redova</h3>
      <form method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zatvoriRedoveLeta/${idLeta}">
        <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
        <input type="hidden" name="oznakaLeta" value="${oznakaLeta}">
        <input type="hidden" name="datum" value="${datum}">
        <input type="hidden" name="vrijemePolijetanja" value="${vrijemePolijetanja}">
        <input type="hidden" name="polazniAerodrom" value="${polazniAerodrom}">
        <input type="hidden" name="odredisniAerodrom" value="${odredisniAerodrom}">
        Od reda: <input type="number" name="odReda" min="1" required>
        Do reda: <input type="number" name="doReda" min="1" required>
        <button type="submit">Zatvori sjedala</button>
      </form>
    </c:when>
    <c:otherwise>
      <p><em>Let za zadanu oznaku i datum ne postoji ili stanje nije dostupno.</em></p>
    </c:otherwise>
  </c:choose>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetovaInicijalizacija">Pregled letova (inicijalizacija)</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
