<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/epn.png">
  <title>Actualizar Ruta</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/css/select2.min.css" rel="stylesheet" />
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/index.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/styles/form.css">
</head>
<body>
<a style="position:absolute; top: 2rem; left: 2rem" href="${pageContext.request.contextPath}/GestionServlet?action=gestionRutas">
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24">
    <path fill="currentColor" d="m9 18l-6-6l6-6l1.4 1.4L6.8 11H21v2H6.8l3.6 3.6z"/>
  </svg>
  <p>Volver a la lista de Rutas</p>
</a>

<form action="${pageContext.request.contextPath}/GestionServlet?action=actualizarRuta" method="post">
  <h1>Actualizar Ruta</h1>
  <div class="form-section">
    <input type="hidden" name="rutaId" value="${ruta.id}" />
  </div>

  <div class="form-section">
    <label for="origen">Origen:</label>
    <input type="text" id="origen" name="origen" value="${ruta.origen}" required />
  </div>

  <div class="form-section">
    <label for="destino">Destino:</label>
    <input type="text" id="destino" name="destino" value="${ruta.destino}" required />
  </div>

  <div class="form-section">
    <label for="calles">Selecciona Calles:</label>
    <select name="calles" id="calles" multiple required>
      <c:forEach var="calle" items="${allCalles}">
        <option value="${calle.id}"
                <c:if test="${selectedCalleIds.contains(calle.id)}">selected</c:if>
        >${calle.nombre}</option>
      </c:forEach>
    </select>
  </div>

  <input class="blue-button"  type="submit" value="Actualizar Ruta" />
</form>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/js/select2.min.js"></script>
<script>
  $(document).ready(function() {
    $('#calles').select2({
      placeholder: "Selecciona calles",
      allowClear: true
    });
  });
</script>
</body>
</html>
