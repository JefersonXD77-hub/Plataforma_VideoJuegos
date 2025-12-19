document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("formNuevaEmpresa");
  const msg = document.getElementById("msgEmpresa");


  const ctx = `/${location.pathname.split("/")[1]}`;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msg.textContent = "";
    msg.style.color = "black";

    const empresa = {
      nombre: document.getElementById("empNombre").value.trim(),
      descripcion: document.getElementById("empDescripcion").value.trim(),
      idPais: document.getElementById("empPais").value ? Number(document.getElementById("empPais").value) : null,
      porcentajeComision: document.getElementById("empComision").value ? Number(document.getElementById("empComision").value) : null
    };

    const usuarioResponsable = {
      nombreCompleto: document.getElementById("usrNombre").value.trim(),
      correo: document.getElementById("usrCorreo").value.trim(),
      password: document.getElementById("usrPassword").value,
      fechaNacimiento: document.getElementById("usrFechaNac").value, // YYYY-MM-DD
      telefono: document.getElementById("usrTelefono").value.trim(),
      nickname: document.getElementById("usrNick").value.trim(),
      idPais: empresa.idPais
    };

    const payload = { empresa, usuarioResponsable };

    try {
      const resp = await fetch(`${ctx}/empresas`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin", // IMPORTANTÍSIMO: manda cookie de sesión
        body: JSON.stringify(payload)
      });

      const data = await resp.json().catch(() => ({}));

      if (!resp.ok || !data.ok) {
        throw new Error(data.mensaje || `Error HTTP ${resp.status}`);
      }

      msg.style.color = "green";
      msg.textContent = `OK: Empresa creada (id=${data.idEmpresa}) y responsable (id=${data.idUsuarioResponsable}).`;
      form.reset();

    } catch (err) {
      msg.style.color = "red";
      msg.textContent = err.message || "Error inesperado";
      console.error(err);
    }
  });
});

