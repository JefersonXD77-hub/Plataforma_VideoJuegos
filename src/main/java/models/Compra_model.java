package models;

import db.ConexionMySQL;
import dtos.Compra_detalle_dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Compra_model {

    private final Biblioteca_model bibliotecaModel = new Biblioteca_model();
    private final Compra_detalle_model detalleModel = new Compra_detalle_model();

    public static class CompraResult {

        public final boolean ok;
        public final String mensaje;
        public final Integer idCompra;

        public CompraResult(boolean ok, String mensaje, Integer idCompra) {
            this.ok = ok;
            this.mensaje = mensaje;
            this.idCompra = idCompra;
        }

        public static CompraResult ok(int idCompra) {
            return new CompraResult(true, "OK", idCompra);
        }

        public static CompraResult fail(String msg) {
            return new CompraResult(false, msg, null);
        }
    }

   
    public CompraResult comprarVideojuegos(int idUsuario, List<Integer> idsVideojuego) {
        return comprarVideojuegos(idUsuario, idsVideojuego, null);
    }

    public CompraResult comprarVideojuegos(int idUsuario, List<Integer> idsVideojuego, Timestamp fechaCompra) {

        if (idUsuario <= 0) {
            return CompraResult.fail("idUsuario inválido.");
        }
        if (idsVideojuego == null || idsVideojuego.isEmpty()) {
            return CompraResult.fail("Debe enviar al menos un videojuego.");
        }

        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (Integer id : idsVideojuego) {
            if (id != null && id > 0) {
                set.add(id);
            }
        }
        if (set.isEmpty()) {
            return CompraResult.fail("Lista de videojuegos inválida.");
        }
        List<Integer> ids = new ArrayList<>(set);

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) {
                return CompraResult.fail("No se pudo conectar a BD.");
            }

            conn.setAutoCommit(false);

           
            BigDecimal saldo = obtenerSaldo(conn, idUsuario);
            if (saldo == null) {
                conn.rollback();
                return CompraResult.fail("El usuario no tiene cartera.");
            }

            
            List<Compra_detalle_dtos> detalles = new ArrayList<>();
            BigDecimal totalBruto = BigDecimal.ZERO;
            BigDecimal totalComision = BigDecimal.ZERO;
            BigDecimal totalNeto = BigDecimal.ZERO;

            for (Integer idVideojuego : ids) {

                VideojuegoInfo juego = obtenerInfoJuego(conn, idVideojuego);
                if (juego == null) {
                    conn.rollback();
                    return CompraResult.fail("Videojuego no existe. id=" + idVideojuego);
                }
                if (!"ACTIVO".equalsIgnoreCase(juego.estado)) {
                    conn.rollback();
                    return CompraResult.fail("No se puede comprar: videojuego id=" + idVideojuego + " no está ACTIVO.");
                }

                //  verificar edad mínima del usuario vs clasificación del videojuego
                String msgEdad = validarEdadCompra(conn, idUsuario, juego.idClasificacion);
                if (msgEdad != null) {
                    conn.rollback();
                    return CompraResult.fail(msgEdad);
                }

                if (bibliotecaModel.existePropio(conn, idUsuario, idVideojuego)) {
                    conn.rollback();
                    return CompraResult.fail("El usuario ya posee el videojuego id=" + idVideojuego);
                }

                BigDecimal pctComision = obtenerComisionEfectivaEmpresa(conn, juego.idEmpresa);
                if (pctComision == null) pctComision = new BigDecimal("15.00");

                BigDecimal precio = juego.precio;
                if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
                    conn.rollback();
                    return CompraResult.fail("Precio inválido en videojuego id=" + idVideojuego);
                }

                BigDecimal montoComision = precio
                        .multiply(pctComision)
                        .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);

                BigDecimal netoEmpresa = precio.subtract(montoComision);

                totalBruto = totalBruto.add(precio);
                totalComision = totalComision.add(montoComision);
                totalNeto = totalNeto.add(netoEmpresa);

                Compra_detalle_dtos d = new Compra_detalle_dtos();
 
                d.setIdVideojuego(idVideojuego);
                d.setPrecioUnitario(precio);
                d.setPorcentajeComision(pctComision);
                d.setMontoComision(montoComision);
                d.setMontoNetoEmpresa(netoEmpresa);
                detalles.add(d);
            }

            // Validar saldo suficiente por el total
            if (saldo.compareTo(totalBruto) < 0) {
                conn.rollback();
                return CompraResult.fail("Saldo insuficiente para la compra total.");
            }

            // Insert compra
            int idCompra = insertarCompra(conn, idUsuario, fechaCompra, totalBruto, totalComision, totalNeto);

            // Insert detalles
            for (Compra_detalle_dtos d : detalles) {
                d.setIdCompra(idCompra);
            }
            detalleModel.insertarBatch(conn, detalles);

            //  cartera
            descontarSaldo(conn, idUsuario, totalBruto);

            // Insert movimiento cartera 
            insertarMovimientoCompra(conn, idUsuario, idCompra, totalBruto,
                    "Compra videojuegos: " + ids.toString());

            //  Insert biblioteca 
            for (Integer idVideojuego : ids) {
                boolean okBiblio = bibliotecaModel.insertarPropio(conn, idUsuario, idVideojuego);
                if (!okBiblio) {
                    conn.rollback();
                    return CompraResult.fail("No se pudo registrar en biblioteca el juego id=" + idVideojuego);
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return CompraResult.ok(idCompra);

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }
            e.printStackTrace();
            return CompraResult.fail("Error en compra: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (Exception ignored) {
            }
            cx.desconectar(conn);
        }
    }

    private static class VideojuegoInfo {

        int idEmpresa;
        BigDecimal precio;
        String estado;
        int idClasificacion;
    }

    private VideojuegoInfo obtenerInfoJuego(Connection conn, int idVideojuego) throws Exception {
        String sql = "SELECT id_empresa, precio, estado, id_clasificacion FROM videojuego WHERE id_videojuego = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVideojuego);
            try ( ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                VideojuegoInfo v = new VideojuegoInfo();
                v.idEmpresa = rs.getInt("id_empresa");
                v.precio = rs.getBigDecimal("precio");
                v.estado = rs.getString("estado");
                v.idClasificacion = rs.getInt("id_clasificacion");
                return v;
            }
        }
    }

    private BigDecimal obtenerSaldo(Connection conn, int idUsuario) throws Exception {
        String sql = "SELECT saldo FROM cartera WHERE id_usuario = ? FOR UPDATE";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("saldo") : null;
            }
        }
    }

    private BigDecimal obtenerPorcentajeComisionEmpresa(Connection conn, int idEmpresa) throws Exception {
        String sql = "SELECT porcentaje_comision FROM empresa WHERE id_empresa = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("porcentaje_comision") : null;
            }
        }
    }

private BigDecimal obtenerComisionGlobal(Connection conn) throws Exception {
   
    String sql = "SELECT valor_numerico FROM parametro_sistema WHERE clave='COMISION_GLOBAL'";
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            BigDecimal v = rs.getBigDecimal(1);
            return (v == null) ? new BigDecimal("15.00") : v;
        }
    }
    return new BigDecimal("15.00");
}

private BigDecimal obtenerComisionEfectivaEmpresa(Connection conn, int idEmpresa) throws Exception {
    BigDecimal global = obtenerComisionGlobal(conn);
    BigDecimal empresa = obtenerPorcentajeComisionEmpresa(conn, idEmpresa);
    if (empresa == null) return global;
    return empresa.min(global);
}

private String validarEdadCompra(Connection conn, int idUsuario, int idClasificacion) throws Exception {
    Integer edadMin = null;
    String q1 = "SELECT edad_minima FROM clasificacion_edad WHERE id_clasificacion=?";
    try (PreparedStatement ps = conn.prepareStatement(q1)) {
        ps.setInt(1, idClasificacion);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) edadMin = rs.getInt(1);
        }
    }
    if (edadMin == null) {
        return null;
    }

    //  Fecha nacimiento del usuario
    LocalDate nacimiento = null;
    String q2 = "SELECT fecha_nacimiento FROM usuario WHERE id_usuario=?";
    try (PreparedStatement ps = conn.prepareStatement(q2)) {
        ps.setInt(1, idUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                java.sql.Date d = rs.getDate(1);
                if (d != null) nacimiento = d.toLocalDate();
            }
        }
    }
    if (nacimiento == null) return "No se puede verificar la edad: el usuario no tiene fecha de nacimiento.";

    int edad = Period.between(nacimiento, LocalDate.now()).getYears();
    if (edad < edadMin) {
        return "Compra rechazada: el usuario no cumple la edad mínima (" + edadMin + "+).";
    }
    return null;
}


    private int insertarCompra(Connection conn, int idUsuario, Timestamp fechaCompra, BigDecimal bruto, BigDecimal comision, BigDecimal neto) throws Exception {
        String sql = "INSERT INTO compra (id_usuario, fecha, total_bruto, total_comision_plataforma, total_neto_empresas, estado) "
                + "VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?, 'PAGADA')";

        try ( PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setTimestamp(2, fechaCompra);
            ps.setBigDecimal(3, bruto);
            ps.setBigDecimal(4, comision);
            ps.setBigDecimal(5, neto);

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se insertó compra.");
            }

            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new Exception("No se obtuvo id_compra.");
                }
                return rs.getInt(1);
            }
        }
    }

    
    public List<dtos.CompraView_dtos> listarHistorialCompras(int idUsuario) {
    List<dtos.CompraView_dtos> out = new ArrayList<>();

    String sql = ""
        + "SELECT "
        + "  c.id_compra, c.fecha, c.total_bruto, c.total_comision_plataforma, c.total_neto_empresas, c.estado, "
        + "  d.id_detalle, d.id_videojuego, v.titulo, "
        + "  d.precio_unitario, d.porcentaje_comision, d.monto_comision, d.monto_neto_empresa "
        + "FROM compra c "
        + "JOIN compra_detalle d ON d.id_compra = c.id_compra "
        + "JOIN videojuego v ON v.id_videojuego = d.id_videojuego "
        + "WHERE c.id_usuario = ? "
        + "ORDER BY c.fecha DESC, c.id_compra DESC, d.id_detalle ASC";

    ConexionMySQL cx = new ConexionMySQL();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = cx.conectar();
        if (conn == null) return out;

        ps = conn.prepareStatement(sql);
        ps.setInt(1, idUsuario);
        rs = ps.executeQuery();

        // agrupación por id_compra
        java.util.LinkedHashMap<Integer, dtos.CompraView_dtos> map = new java.util.LinkedHashMap<>();

        while (rs.next()) {
            int idCompra = rs.getInt("id_compra");

            dtos.CompraView_dtos compra = map.get(idCompra);
            if (compra == null) {
                compra = new dtos.CompraView_dtos();
                compra.setIdCompra(idCompra);
                compra.setFecha(rs.getTimestamp("fecha"));
                compra.setTotalBruto(rs.getBigDecimal("total_bruto"));
                compra.setTotalComisionPlataforma(rs.getBigDecimal("total_comision_plataforma"));
                compra.setTotalNetoEmpresas(rs.getBigDecimal("total_neto_empresas"));
                compra.setEstado(rs.getString("estado"));
                map.put(idCompra, compra);
            }

            dtos.CompraDetalleView_dtos det = new dtos.CompraDetalleView_dtos();
            det.setIdDetalle(rs.getInt("id_detalle"));
            det.setIdVideojuego(rs.getInt("id_videojuego"));
            det.setTitulo(rs.getString("titulo"));
            det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
            det.setPorcentajeComision(rs.getBigDecimal("porcentaje_comision"));
            det.setMontoComision(rs.getBigDecimal("monto_comision"));
            det.setMontoNetoEmpresa(rs.getBigDecimal("monto_neto_empresa"));

            compra.getDetalles().add(det);
        }

        out.addAll(map.values());

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        cx.desconectar(conn);
    }

    return out;
}

    
    
    private void descontarSaldo(Connection conn, int idUsuario, BigDecimal monto) throws Exception {
        String sql = "UPDATE cartera SET saldo = saldo - ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_usuario = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, monto);
            ps.setInt(2, idUsuario);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se pudo descontar cartera.");
            }
        }
    }

    private void insertarMovimientoCompra(Connection conn, int idUsuario, int idCompra, BigDecimal monto, String descripcion) throws Exception {
        String sql = "INSERT INTO movimiento_cartera (id_usuario, id_compra, tipo, monto, descripcion) "
                + "VALUES (?, ?, 'COMPRA', ?, ?)";

        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idCompra);
            ps.setBigDecimal(3, monto.negate());
            ps.setString(4, descripcion);
            ps.executeUpdate();
        }
    }
}