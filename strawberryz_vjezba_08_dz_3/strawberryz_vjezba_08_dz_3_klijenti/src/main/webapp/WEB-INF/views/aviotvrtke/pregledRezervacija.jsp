<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled nepotvrđenih rezervacija</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Pregled nepotvrđenih rezervacija</h2>

  <c:if test="${not otvorenRacun}">
    <p><em>Nemate otvoreni račun - potvrđivanje nije moguće.</em></p>
  </c:if>
  <c:if test="${not putnikOdabran}">
    <p><em>Niste odabrali putnika - potvrđivanje nije moguće.</em>
       <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika">Odaberi putnika</a></p>
  </c:if>
  <c:if test="${putnikOdabran}">
    <p>Odabrani putnik: <strong>${putnikKorisnickoIme}</strong></p>
  </c:if>

  <table border="1">
    <thead>
      <tr>
        <th>Ime i prezime putnika</th>
        <th>Oznaka leta</th>
        <th>Datum polijetanja</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Akcija</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${rezervacije}">
        <tr>
          <td>${putnikIme} ${putnikPrezime}</td>
          <td>${r.sjedalo().let().letPodaci().oznakaLeta()}</td>
          <td>${r.sjedalo().let().datumPolijetanja()}</td>
          <td>${r.sjedalo().let().letPodaci().vrijemePolijetanja()}</td>
          <td>${r.sjedalo().let().letPodaci().polazniAerodrom()}</td>
          <td>${r.sjedalo().let().letPodaci().odredisniAerodrom()}</td>
          <td>
            <c:choose>
              <c:when test="${otvorenRacun and putnikOdabran}">
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/potvrdi/${r.id()}">
                  <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                  <input type="hidden" name="oznakaLeta" value="${r.sjedalo().let().letPodaci().oznakaLeta()}">
                  <input type="hidden" name="idLeta" value="${r.sjedalo().let().id()}">
                  <button type="submit">Potvrdi</button>
                </form>
              </c:when>
              <c:otherwise>Potvrdi (nedostupno)</c:otherwise>
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