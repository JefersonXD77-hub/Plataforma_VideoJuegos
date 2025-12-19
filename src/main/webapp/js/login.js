// js/login.js

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('formLogin');
  const mensajeError = document.getElementById('mensajeError');

  form.addEventListener('submit', (event) => {
    event.preventDefault(); // evitar refresh

    mensajeError.textContent = '';

    const correo = document.getElementById('correo').value.trim();
    const password = document.getElementById('password').value;

    if (!correo || !password) {
      mensajeError.textContent = 'Debe ingresar correo y contraseña.';
      return;
    }

    const payload = { correo, password };

    fetch('login', {               // → /PlataformaVideoJuegos/login
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
      .then(response => {
        if (!response.ok) {
          return response.json().then(errData => {
            throw new Error(errData.mensaje || 'Error en el login');
          });
        }
        return response.json();
      })
      .then(data => {
        if (!data.ok) {
          mensajeError.textContent = data.mensaje || 'Error en el login.';
          return;
        }

        // Para depurar:
        console.log('Respuesta login:', data);

        const idRol = data.idRol;

        if (idRol === 1) {
          // Admin del sistema
          window.location.href = 'admin.html';
        } else if (idRol === 2) {
          // Usuario empresa (ejemplo)
          window.location.href = 'empresa.html';
        } else {
          // Usuario común
          window.location.href = 'tienda.html';
        }
      })
      .catch(error => {
        console.error('Error en login:', error);
        mensajeError.textContent = error.message || 'No se pudo iniciar sesión.';
      });
  });
});

