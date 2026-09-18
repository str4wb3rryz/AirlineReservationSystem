<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Sjedala u avionu</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
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

  <c:if test="${putnikOdabran}">
    <p>Odabrani putnik: <strong>${putnikKorisnickoIme}</strong></p>
  </c:if>
  <c:if test="${not otvorenRacun}">
    <p><em>Nemate otvoreni račun - rezervacija sjedala nije moguća.</em></p>
  </c:if>
  <c:if test="${not putnikOdabran}">
    <p><em>Niste odabrali putnika - rezervacija sjedala nije moguća.</em>
       <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika">Odaberi putnika</a></p>
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
              <c:when test="${s.statusRezervacije() == 'SLOBODNA' and otvorenRacun and putnikOdabran}">
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/rezervirajSjedalo/${idLeta}">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="razred" value="${s.sjedalo().razred()}">
                  <input type="hidden" name="red" value="${s.sjedalo().red()}">
                  <input type="hidden" name="oznakaSjedala" value="${s.sjedalo().oznakaSjedala()}">
                  <input type="hidden" name="oznakaLeta" value="${s.sjedalo().let().letPodaci().oznakaLeta()}">
                  <input type="hidden" name="datum" value="${s.sjedalo().let().datumPolijetanja()}">
                  <input type="hidden" name="vrijemePolijetanja" value="${s.sjedalo().let().letPodaci().vrijemePolijetanja()}">
                  <input type="hidden" name="polazniAerodrom" value="${s.sjedalo().let().letPodaci().polazniAerodrom()}">
                  <input type="hidden" name="odredisniAerodrom" value="${s.sjedalo().let().letPodaci().odredisniAerodrom()}">
                  <button type="submit">Rezerviraj</button>
                </form>
              </c:when>
              <c:when test="${s.statusRezervacije() == 'SLOBODNA'}">
                <span title="Potreban je otvoreni račun i odabrani putnik">Rezerviraj (nedostupno)</span>
              </c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetova">Pregled letova avio tvrtke</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledRezervacija">Pregled nepotvrđenih rezervacija</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
