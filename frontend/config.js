/* ==========================================================================
   Configuracion del frontend
   --------------------------------------------------------------------------
   Define la URL de la API del backend.

   - Desarrollo local:  http://localhost:8080/api
   - Produccion:        cambia esta linea por la URL publica del backend,
                        por ejemplo  https://gestion-biblioteca.up.railway.app/api
                        (despues vuelve a subir el frontend al hosting).

   Este archivo se carga ANTES que api.js, por eso el valor queda disponible
   en window.__API_URL__.
   ========================================================================== */

window.__API_URL__ = 'http://localhost:8080/api';
