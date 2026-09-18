<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Letovi putnika</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Letovi putnika ${putnik}</h2>
  <p>Razdoblje: ${odDatuma} - ${doDatuma}</p>

  <table>
    <thead>
      <tr>
        <th>Br. rezervacije</th>
        <th>Oznaka leta</th>
        <th>Datum polijetanja</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Status</th>
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
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledPutnika">Pregled putnika</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
