<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Pregled avio tvrtki</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Pregled avio tvrtki</h2>
  <p>Odabrani poslužitelj: <strong>${posluzitelj}</strong></p>

  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Naziv</th>
        <th>Letovi</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="t" items="${avioTvrtke}">
        <tr>
          <td>${t.id()}</td>
          <td>${t.naziv()}</td>
          <td>
            <a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledLetovaAvioTvrtke/${t.id()}">Pregled letova</a>
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
