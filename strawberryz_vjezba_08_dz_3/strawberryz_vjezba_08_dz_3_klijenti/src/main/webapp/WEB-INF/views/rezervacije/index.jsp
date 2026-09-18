<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>NWTiS - Privatni dio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/privatni.css">
    </head>
    <body>
        <h1>Privatni dio</h1>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/odabirPosluzitelja">Odabir poslužitelja za rezervacije</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledRacuna">Pregled vlastitih računa</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledRezervacija">Pregled rezervacija korisnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pregledAvioTvrtki">Pregled avio tvrtki i letova</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a></li>
        </ul>
    </body>
</html>