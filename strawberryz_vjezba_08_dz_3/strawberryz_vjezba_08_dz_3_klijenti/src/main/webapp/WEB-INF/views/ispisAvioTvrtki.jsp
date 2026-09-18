<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Aviotvrtke" %>
<%@ page import="edu.unizg.foi.nwtis.strawberryz.vjezba_08_dz_3.jpa.entiteti.Zastupnici" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Pregled avio tvrtki</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/javni.css">
    </head>
    <body>
        <c:if test="${korisnik != null}">
            <p class="korisnik-traka">Prijavljen je korisnik: ${korisnik}</p>
        </c:if>
        <h1>REST MVC - Pregled avio tvrtki</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>R.br.</th><th>Tvrtka</th><th>Naziv</th><th>Zastupnici</th></tr>
	<%
	int i=0;
	List<Aviotvrtke> aviotvrtke = (List<Aviotvrtke>) request.getAttribute("aviotvrtke");
	Map<String, List<Zastupnici>> zastupnici = (Map<String, List<Zastupnici>>) request.getAttribute("zastupnici");
	if (aviotvrtke != null) {
	  for (Aviotvrtke at : aviotvrtke) {
	    i++;
	    List<Zastupnici> popisZastupnika = zastupnici != null ? zastupnici.get(at.getTvrtka()) : null;
	    %>
       <tr>
           <td class="desno"><%= i %></td>
           <td><%= at.getTvrtka() %></td>
           <td><%= at.getNaziv() %></td>
           <td>
	    <%
	    if (popisZastupnika == null || popisZastupnika.isEmpty()) {
	      %>-<%
	    } else {
	      %><ul><%
	      for (Zastupnici z : popisZastupnika) {
	        %><li><%= z.getKorisnik() %></li><%
	      }
	      %></ul><%
	    }
	    %>
           </td>
       </tr>
	  <%
	  }
	}
	%>	
        </table>	        
    </body>
</html>