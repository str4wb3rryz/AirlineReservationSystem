<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>NWTiS - Početna stranica</title>
      
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    </head>
    <body>
        <h1>NWTiS - Početna stranica</h1>
        <c:if test="${korisnik != null}">
    <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/noviKorisnik">Dodavanje novog korisnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisAvioTvrtki">Pregled avio tvrtki i zastupnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">Privatni dio</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">Avio tvrtke</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/admin/pocetak">Administracijski dio</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/prijava/prijavaKorisnika">Prijava korisnika</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/prijava/odjavaKorisnika">Odjava korisnika</a></li>
        </ul>
    </body>
</html>