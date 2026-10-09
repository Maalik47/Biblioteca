/* ==========================================================================
   Configuracion del frontend
   --------------------------------------------------------------------------
   Selecciona la URL de la API automaticamente:

   - Abriendo desde localhost (o archivo local) -> backend local en 8080
   - Publicado en un hosting -> backend desplegado en Render

   Para usar otro backend publicado, cambia la URL de PRODUCCION de abajo.
   ========================================================================== */

(function () {
    const esLocal = ['localhost', '127.0.0.1', ''].includes(location.hostname);
    const API_LOCAL = 'http://localhost:8080/api';
    const API_PRODUCCION = 'https://biblioteca-0i0a.onrender.com/api';
    window.__API_URL__ = esLocal ? API_LOCAL : API_PRODUCCION;
})();
