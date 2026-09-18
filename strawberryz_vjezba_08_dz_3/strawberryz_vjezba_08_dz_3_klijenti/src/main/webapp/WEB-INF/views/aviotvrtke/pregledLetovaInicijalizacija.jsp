<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Letovi avio tvrtke - inicijalizacija</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Letovi avio tvrtke - inicijalizacija leta</h2>
  <c:if test="${not empty poruka}">
    <p><strong>${poruka}</strong></p>
  </c:if>

  <table>
    <thead>
      <tr>
        <th>Oznaka leta</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Pregled leta na zadani datum</th>
        <th>Inicijalizacija leta (interval datuma)</th>
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
            <form class="inline" method="get" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledStanjaLeta">
              <input type="hidden" name="oznakaLeta" value="${l.oznakaLeta()}">
              <input type="hidden" name="vrijemePolijetanja" value="${l.vrijemePolijetanja()}">
              <input type="hidden" name="polazniAerodrom" value="${l.polazniAerodrom()}">
              <input type="hidden" name="odredisniAerodrom" value="${l.odredisniAerodrom()}">
              <input type="date" lang="hr" name="datum" required>
              <button type="submit">Pregled leta</button>
            </form>
          </td>
          <td>
            <form class="inline" method="post" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/inicijalizirajLet">
              <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
              <input type="hidden" name="oznakaLeta" value="${l.oznakaLeta()}">
              Od: <input type="date" lang="hr" name="odDatuma" required>
              Do: <input type="date" lang="hr" name="doDatuma" required>
              <button type="submit">Inicijaliziraj</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">Avio tvrtke</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
