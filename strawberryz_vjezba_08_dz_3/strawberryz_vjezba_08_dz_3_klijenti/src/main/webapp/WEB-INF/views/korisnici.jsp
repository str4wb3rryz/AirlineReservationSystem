<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.Korisnik" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Pregled korisnika</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    </head>
    <body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <h1>REST MVC - Pregled korisnika</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>R.br. <th>Korisnik</th><th>Ime</th><th>Prezime</th><th>Email</th></tr>
	<%
	int i=0;
	List<Korisnik> korisnici = (List<Korisnik>) request.getAttribute("korisnici");
	for(Korisnik k: korisnici) {
	  i++;
	  %>
       <tr><td class="desno"><%= i %></td><td><%= k.korisnik() %></td><td><%= k.ime() %></td><td><%= k.prezime() %></td><td><%= k.email() %></td></tr>	  
	  <%
	}
	%>	
        </table>	        
    </body>
</html>
