
package models;

import db.ConexionMySQL;
import dtos.Categoria_dtos;
import dtos.Videojuego_categoria_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Videojuego_Categoria_model {
 
    public List<Categoria_dtos> listarCategoriasVideojuego(int idVideojuego){
    List<Categoria_dtos> lista = new ArrayList<>();
    
    String sql = "SELECT c.id_categoria, c.nombre, c.descripcion, c.estado FROM videojuego_categoria vc INNER JOIN categoria c ON vc.id_categoria = c.id_categoria WHERE vc.id_videojuego = ?";
        
   ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
    
     try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoCategoria_model.listarCategoriasDeVideojuego");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria_dtos c = new Categoria_dtos();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setEstado(rs.getString("estado"));
                lista.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoCategoria_model.listarCategoriasDeVideojuego(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    
    public boolean reemplazarCategorias(int idVideojuego, List<Integer> idsCategorias) {
        String sqlDelete = "DELETE FROM videojuego_categoria WHERE id_videojuego = ?";
        String sqlInsert = "INSERT INTO videojuego_categoria (id_videojuego, id_categoria) VALUES (?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoCategoria_model.reemplazarCategorias");
                return false;
            }

            conn.setAutoCommit(false);

          
            psDelete = conn.prepareStatement(sqlDelete);
            psDelete.setInt(1, idVideojuego);
            psDelete.executeUpdate();

           
            if (idsCategorias != null) {
                psInsert = conn.prepareStatement(sqlInsert);
                for (Integer idCat : idsCategorias) {
                    psInsert.setInt(1, idVideojuego);
                    psInsert.setInt(2, idCat);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoCategoria_model.reemplazarCategorias(): " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}
            return false;
        } finally {
            try { if (psDelete != null) psDelete.close(); } catch (Exception ex) {}
            try { if (psInsert != null) psInsert.close(); } catch (Exception ex) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }


    public boolean agregarCategoria(Videojuego_categoria_dtos vc) {
        String sql = "INSERT INTO videojuego_categoria (id_videojuego, id_categoria) VALUES (?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoCategoria_model.agregarCategoria");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, vc.getIdVideojuego());
            ps.setInt(2, vc.getIdCategoria());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoCategoria_model.agregarCategoria(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}