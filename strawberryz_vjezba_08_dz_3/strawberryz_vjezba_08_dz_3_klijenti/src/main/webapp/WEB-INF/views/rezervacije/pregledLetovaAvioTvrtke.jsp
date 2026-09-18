<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Letovi avio tvrtke</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Letovi avio tvrtke ${avioTvrtka}</h2>

  <table>
    <thead>
      <tr>
        <th>Oznaka leta</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Pregled stvarnih letova na zadani datum</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="l" items="${letovi}">
        <tr>
          <td>${l.oznakaLeta()}</td>
          <td>${l.vrijemePolijetanja()}</td>
          <td>${l.polazniAerodrom()}</td>
          <td>${l.odredisniAerodrom()}</td>
          <td>
            <form method="get" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledLeta">
              <input type="hidden" name="oznakaLeta" value="${l.oznakaLeta()}">
              <input type="hidden" name="vrijemePolijetanja" value="${l.vrijemePolijetanja()}">
              <input type="hidden" name="polazniAerodrom" value="${l.polazniAerodrom()}">
              <input type="hidden" name="odredisniAerodrom" value="${l.odredisniAerodrom()}">
              <input type="date" lang="hr" name="datum" required>
              <button type="submit">Pregled leta</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledAvioTvrtki">Pregled avio tvrtki</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
