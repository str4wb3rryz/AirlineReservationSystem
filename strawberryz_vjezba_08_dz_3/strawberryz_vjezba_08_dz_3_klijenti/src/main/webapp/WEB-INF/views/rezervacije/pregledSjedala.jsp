<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Sjedala u avionu</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Sjedala u avionu za let ${idLeta}</h2>

  <p class="legenda">
    <span class="SLOBODNA">Slobodno</span>
    <span class="KREIRANA">Rezervirano (nepotvrđeno)</span>
    <span class="POTVRDENA">Potvrđeno</span>
    <span class="ZATVORENA">Zatvoreno</span>
    <span class="NEVAZECA">Nevažeće</span>
  </p>

  <c:if test="${not otvorenRacun}">
    <p><em>Nemate otvoreni račun - rezervacija sjedala nije moguća dok ne otvorite račun.</em></p>
  </c:if>

  <table>
    <thead>
      <tr>
        <th>Red</th>
        <th>Oznaka sjedala</th>
        <th>Razred</th>
        <th>Status</th>
        <th>Putnik</th>
        <th>Akcija</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="s" items="${sjedala}">
        <tr>
          <td>${s.sjedalo().red()}</td>
          <td>${s.sjedalo().oznakaSjedala()}</td>
          <td>${s.sjedalo().razred()}</td>
          <td class="${s.statusRezervacije()}">${s.statusRezervacije()}</td>
          <td>${s.putnik()}</td>
          <td>
            <c:choose>
              <c:when test="${s.statusRezervacije() == 'SLOBODNA' and otvorenRacun}">
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/rezerviraj/${idLeta}">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="razred" value="${s.sjedalo().razred()}">
                  <button type="submit">Rezerviraj</button>
                </form>
              </c:when>
              <c:when test="${s.statusRezervacije() == 'SLOBODNA' and not otvorenRacun}">
                <span title="Nemate otvoreni račun">Rezerviraj (nedostupno)</span>
              </c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledAvioTvrtki">Pregled avio tvrtki</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledRezervacija">Pregled mojih rezervacija</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
