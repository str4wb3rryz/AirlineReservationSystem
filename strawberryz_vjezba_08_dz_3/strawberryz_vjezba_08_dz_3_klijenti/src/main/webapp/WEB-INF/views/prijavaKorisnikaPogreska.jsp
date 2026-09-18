<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Prijava korisnika</title>
     
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    </head>
    <body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <h1>Prijava korisnika greška</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/prijava/prijavaKorisnika">Prijava korisnika</a>
            </li>
           
            <li>
            	Prijava korisnika nije bila uspješna!!
            </li>                     
        </ul>   
    </body>
</html>
