
document.addEventListener('DOMContentLoaded', () => {
  const btnLogin    = document.getElementById('btnLogin');
  const btnRegistro = document.getElementById('btnRegistro');
  const btnInvitado = document.getElementById('btnInvitado');

  btnLogin.addEventListener('click', () => {
    window.location.href = 'login.html';
  });

  btnRegistro.addEventListener('click', () => {
    window.location.href = 'registro.html';
  });

  btnInvitado.addEventListener('click', () => {
    window.location.href = 'tienda.html';
  });
});

