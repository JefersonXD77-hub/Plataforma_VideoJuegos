
package models;

import db.ConexionMySQL;
import dtos.Empresa_dtos;
import dtos.Empresa_usuario_dtos;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Empresa_model {

    // Listar todas las empresas
    public List<Empresa_dtos> listarTodas() {
        List<Empresa_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_empresa, nombre, descripcion, id_pais, "
                   + "porcentaje_comision, fecha_registro "
                   + "FROM empresa";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Empresa_model.listarTodas");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Empresa_dtos e = mapRowToDTO(rs);
                lista.add(e);
            }

        } catch (SQLException e) {
            System.out.println("Error en Empresa_model.listarTodas(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Buscar empresa por id
    public Empresa_dtos buscarPorId(int idEmpresa) {
        String sql = "SELECT id_empresa, nombre, descripcion, id_pais, "
                   + "porcentaje_comision, fecha_registro "
                   + "FROM empresa WHERE id_empresa = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Empresa_model.buscarPorId");
                return null;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmpresa);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToDTO(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error en Empresa_model.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return null;
    }

    // Insertar nueva empresa
    public Empresa_dtos insertar(Empresa_dtos empresa) {
        String sql = "INSERT INTO empresa "
                   + "(nombre, descripcion, id_pais, porcentaje_comision) "
                   + "VALUES (?, ?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Empresa_model.insertar");
                return null;
            }

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, empresa.getNombre());
            ps.setString(2, empresa.getDescripcion());
            if (empresa.getIdPais() != null) {
                ps.setInt(3, empresa.getIdPais());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            BigDecimal comision = empresa.getPorcentajeComision();
            if (comision != null) {
                ps.setBigDecimal(4, comision);
            } else {
                ps.setNull(4, java.sql.Types.DECIMAL);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No se insertó empresa");
                return null;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                empresa.setIdEmpresa(rs.getInt(1));
            }

            return empresa;

        } catch (SQLException e) {
            System.out.println("Error en Empresa_model.insertar(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Actualizar empresa (por ejemplo para cambiar comisión o descripción)
    public boolean actualizar(Empresa_dtos empresa) {
        String sql = "UPDATE empresa SET nombre = ?, descripcion = ?, "
                   + "id_pais = ?, porcentaje_comision = ? "
                   + "WHERE id_empresa = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Empresa_model.actualizar");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, empresa.getNombre());
            ps.setString(2, empresa.getDescripcion());
            if (empresa.getIdPais() != null) {
                ps.setInt(3, empresa.getIdPais());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (empresa.getPorcentajeComision() != null) {
                ps.setBigDecimal(4, empresa.getPorcentajeComision());
            } else {
                ps.setNull(4, java.sql.Types.DECIMAL);
            }
            ps.setInt(5, empresa.getIdEmpresa());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Empresa_model.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // (Opcional) registrar vínculo empresa-usuario responsable en tabla empresa_usuario
    public boolean registrarResponsableEmpresa(Empresa_usuario_dtos eu) {
        String sql = "INSERT INTO empresa_usuario "
                   + "(id_empresa, id_usuario, es_responsable) "
                   + "VALUES (?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Empresa_model.registrarResponsableEmpresa");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, eu.getIdEmpresa());
            ps.setInt(2, eu.getIdUsuario());
            ps.setBoolean(3, eu.isEsResponsable());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Empresa_model.registrarResponsableEmpresa(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    private Empresa_dtos mapRowToDTO(ResultSet rs) throws SQLException {
        Empresa_dtos e = new Empresa_dtos();
        e.setIdEmpresa(rs.getInt("id_empresa"));
        e.setNombre(rs.getString("nombre"));
        e.setDescripcion(rs.getString("descripcion"));
        int idPais = rs.getInt("id_pais");
        if (rs.wasNull()) e.setIdPais(null);
        else e.setIdPais(idPais);
        e.setPorcentajeComision(rs.getBigDecimal("porcentaje_comision"));
        e.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return e;
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}

