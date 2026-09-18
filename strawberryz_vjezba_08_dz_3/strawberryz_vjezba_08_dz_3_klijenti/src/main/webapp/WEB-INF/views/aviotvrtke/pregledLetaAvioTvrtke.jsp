<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Podaci leta</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/aviotvrtke.css">
</head>
<body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
  <h2>Podaci leta ${oznakaLeta}</h2>

  <table>
    <thead>
      <tr>
        <th>Oznaka leta</th>
        <th>Datum polijetanja</th>
        <th>Vrijeme polijetanja</th>
        <th>Polazni aerodrom</th>
        <th>Odredišni aerodrom</th>
        <th>Sjedala</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>${oznakaLeta}</td>
        <td>${datum}</td>
        <td>${vrijemePolijetanja}</td>
        <td>${polazniAerodrom}</td>
        <td>${odredisniAerodrom}</td>
        <td>
          <c:choose>
            <c:when test="${idLeta != null}">
              <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledSjedalaAvioTvrtke/${idLeta}">Pregled sjedala u avionu</a>
            </c:when>
            <c:otherwise>
              <span title="Let za zadani datum ne postoji">Pregled sjedala (nedostupno)</span>
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </tbody>
  </table>

  <ul>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pregledLetova">Pregled letova avio tvrtke</a></li>
    <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
  </ul>
</body>
</html>
