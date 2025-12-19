// js/tienda.js

// Cuando el DOM esté listo, configuramos los eventos y cargamos la lista inicial
document.addEventListener('DOMContentLoaded', () => {
  const btnBuscar = document.getElementById('btnBuscar');
  const btnLimpiar = document.getElementById('btnLimpiar');

  // Cargar todos los videojuegos al entrar a la tienda
  cargarVideojuegos();

  // Buscar por texto
  btnBuscar.addEventListener('click', () => {
    const texto = document.getElementById('txtBuscar').value;
    cargarVideojuegos(texto);
  });

  // Limpiar búsqueda y recargar todos
  btnLimpiar.addEventListener('click', () => {
    document.getElementById('txtBuscar').value = '';
    cargarVideojuegos();
  });
});

/**
 * Llama al backend /videojuegos
 * - Si textoBuscar viene con algo => /videojuegos?texto=...
 * - Si no => /videojuegos (listar todos)
 */
function cargarVideojuegos(textoBuscar) {
  const mensaje = document.getElementById('mensaje');
  const tbody = document.getElementById('tablaVideojuegosBody');

  // Limpiamos tabla y mensajes
  tbody.innerHTML = '';
  mensaje.textContent = 'Cargando videojuegos...';

  // Construimos la URL relativa a la app
  let url = 'videojuegos';
  if (textoBuscar && textoBuscar.trim() !== '') {
    url += '?texto=' + encodeURIComponent(textoBuscar.trim());
  }

  fetch(url)
    .then(response => {
      if (!response.ok) {
        throw new Error('Error en la respuesta del servidor: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      // data puede ser null, un objeto, o un array
      // Nuestra API para listas siempre devuelve un array
      // Si no es array, lo transformamos a array con 1 elemento
      let juegos = [];
      if (Array.isArray(data)) {
        juegos = data;
      } else if (data) {
        juegos = [data];
      }

      if (juegos.length === 0) {
        mensaje.textContent = 'No se encontraron videojuegos.';
        return;
      }

      mensaje.textContent = `Se encontraron ${juegos.length} videojuego(s).`;

      // Por cada juego creamos una fila
      juegos.forEach(juego => {
        const tr = document.createElement('tr');

        // Algunos campos tal como vienen del DTO (Gson respeta los nombres)
        const titulo = juego.titulo || '';
        const precio = juego.precio != null ? juego.precio : '';
        const clasificacion = juego.idClasificacion != null ? juego.idClasificacion : '';
        const fechaLanzamiento = juego.fechaLanzamiento || '';
        const estado = juego.estado || '';

        tr.innerHTML = `
          <td>${escapeHtml(titulo)}</td>
          <td>${precio}</td>
          <td>${clasificacion}</td>
          <td>${fechaLanzamiento}</td>
          <td>${estado}</td>
          <td>
            <button class="btn-detalle">Ver detalle</button>
          </td>
        `;

        // Guardamos el juego completo en un atributo para usarlo en el detalle
        tr.dataset.juego = JSON.stringify(juego);

        // Evento del botón "Ver detalle"
        tr.querySelector('.btn-detalle').addEventListener('click', () => {
          const juegoData = JSON.parse(tr.dataset.juego);
          mostrarDetalle(juegoData);
        });

        tbody.appendChild(tr);
      });
    })
    .catch(error => {
      console.error('Error al cargar videojuegos:', error);
      mensaje.textContent = 'Ocurrió un error al cargar los videojuegos.';
    });
}

/**
 * Muestra la información detallada de un juego
 * en el panel #detalleJuego
 */
function mostrarDetalle(juego) {
  const panel = document.getElementById('detalleJuego');
  const lblTitulo = document.getElementById('detalleTitulo');
  const lblDescripcion = document.getElementById('detalleDescripcion');
  const lblRecursos = document.getElementById('detalleRecursos');

  lblTitulo.textContent = juego.titulo || '';
  lblDescripcion.textContent = juego.descripcion || 'Sin descripción';
  lblRecursos.textContent = juego.recursosMinimos || 'No especificado';

  panel.classList.remove('oculto');
}

/**
 * Pequeña utilidad para evitar problemas de XSS
 */
function escapeHtml(text) {
  if (!text) return '';
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}


