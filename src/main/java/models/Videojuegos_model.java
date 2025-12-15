package models;

import db.ConexionMySQL;
import dtos.Videojuegos_dtos;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Videojuegos_model {

    public List<Videojuegos_dtos> listarJuegos() {
        List<Videojuegos_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_videojuego, id_empresa, titulo, descripcion, precio, recursos_minimos, id_clasificacion, fecha_lanzamiento, estado, fecha_creacion FROM videojuego";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Cideojuegos_model");
                return lista;
            }

            ps = conectado.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                Videojuegos_dtos atributoVid = new Videojuegos_dtos();

                atributoVid.setIdVideojuego(rs.getInt("id_videojuego"));
                atributoVid.setIdEmpresa(rs.getInt("id_empresa"));
                atributoVid.setTitulo(rs.getString("titulo"));
                atributoVid.setDescripcion(rs.getString("descripcion"));

                BigDecimal precio = rs.getBigDecimal("precio");
                atributoVid.setPrecio(precio);

                atributoVid.setRecursosMinimos(rs.getString("recursos_minimos"));
                atributoVid.setIdClasificacion(rs.getInt("id_clasificacion"));

                Date fechaLanzado = rs.getDate("fecha_lanzamiento");
                atributoVid.setFechaLanzamiento(fechaLanzado);

                atributoVid.setEstado(rs.getString("estado"));

                Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
                atributoVid.setFechaCreacion(fechaCreacion);

                lista.add(atributoVid);

            }

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_odel.listarJuegos(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conectado);
        }

        return lista;

    }

}
