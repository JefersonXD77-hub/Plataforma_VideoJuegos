package models;

import db.ConexionMySQL;
import dtos.Videojuegos_dtos;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Videojuegos_model {

    //Método encargado de enlistar todos lo juegos de la tabla videojuego.
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

    //Método para listar los videojuegos de una empresa en específico.
    public List<Videojuegos_dtos> listarPorEmpresa(int idEmpresa) {
        List<Videojuegos_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_videojuego, id_empresa, titulo, descripcion, precio, recursos_minimos, id_clasificacion, fecha_lanzamiento, estado, fecha_creacion FROM videojuego WHERE id_empresa = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener la conexión en Videojuegos_model");
                return lista;

            }

            ps = conectado.prepareStatement(sql);
            ps.setInt(1, idEmpresa);
            rs = ps.executeQuery();

            while (rs.next()) {
                Videojuegos_dtos atributoVid = mapRowToDTO(rs);
                lista.add(atributoVid);
            }

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model en listaPorEMpresa(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conectado, conexionMySQL);
        }
        return lista;
    }

    //Método encargado de insertar un nuevo videojuego.
    public Videojuegos_dtos insertar(Videojuegos_dtos juego) {

        String sql = "INSERT INTO videojuego(id_empresa, titulo, descripcion, precio, recursos_minimos, id_clasificacion, fecha_lanzamiento, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Videojuegos_model.insertar");
                return null;
            }

            ps = conectado.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, juego.getIdEmpresa());
            ps.setString(2, juego.getTitulo());
            ps.setString(3, juego.getDescripcion());
            ps.setBigDecimal(4, juego.getPrecio());
            ps.setString(5, juego.getRecursosMinimos());
            ps.setInt(6, juego.getIdClasificacion());

            if (juego.getFechaLanzamiento() != null) {
                ps.setDate(7, juego.getFechaLanzamiento());
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }

          
            String estado = juego.getEstado();
            if (estado == null || estado.isEmpty()) {
                estado = "ACTIVO";
            }
            ps.setString(8, estado);

            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No se insertó ningún registro en videojuego.");
                return null;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                juego.setIdVideojuego(idGenerado);
            }

            return juego;

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model.insertar(): " + e.getMessage());
            e.printStackTrace();
            return null;
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
    }

    //Actualizar los datos de un videojuego existente, este devuelve un true si se catualizó almenos una fila.
    
    public boolean actualizar(Videojuegos_dtos juego) {

        String sql = "UPDATE videojuego SET id_empresa = ?, titulo = ?, descripcion = ?,precio = ?, recursos_minimos = ?, id_clasificacion = ?, fecha_lanzamiento = ?, estado = ? WHERE id_videojuego = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Videojuegos_model.actualizar");
                return false;
            }

            ps = conectado.prepareStatement(sql);

            ps.setInt(1, juego.getIdEmpresa());
            ps.setString(2, juego.getTitulo());
            ps.setString(3, juego.getDescripcion());
            ps.setBigDecimal(4, juego.getPrecio());
            ps.setString(5, juego.getRecursosMinimos());
            ps.setInt(6, juego.getIdClasificacion());

            if (juego.getFechaLanzamiento() != null) {
                ps.setDate(7, juego.getFechaLanzamiento());
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }

            ps.setString(8, juego.getEstado());
            ps.setInt(9, juego.getIdVideojuego());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conectado);
        }
    }
    
    //Método encargado de cambiar el estado de un videojuego de activo a suspendido.
    public boolean cambiarEstado(int idVideojuego, String nuevoEstado) {
        String sql = "UPDATE videojuego SET estado = ? WHERE id_videojuego = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Videojuegos_model.cambiarEstado");
                return false;
            }

            ps = conectado.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idVideojuego);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model.cambiarEstado(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conectado);
        }
    }
    
    // Método para buscar videojuegos por medio de un texto que solo estan activos.
    public List<Videojuegos_dtos> buscarPorTexto(String texto) {
        List<Videojuegos_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_videojuego, id_empresa, titulo, descripcion, precio, "
                   + "recursos_minimos, id_clasificacion, fecha_lanzamiento, "
                   + "estado, fecha_creacion "
                   + "FROM videojuego "
                   + "WHERE estado = 'ACTIVO' "
                   + "AND (titulo LIKE ? OR descripcion LIKE ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Videojuegos_model.buscarPorTexto");
                return lista;
            }

            ps = conectado.prepareStatement(sql);

            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);

            rs = ps.executeQuery();

            while (rs.next()) {
                Videojuegos_dtos atributoVid = mapRowToDTO(rs);
                lista.add(atributoVid);
            }

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model.buscarPorTexto(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conectado, conexionMySQL);
        }

        return lista;
    }

    //Método encargado de buscar un videojuego en función de su ID.
    public Videojuegos_dtos buscarPorId(int idVideojuego) {
        String sql = "SELECT id_videojuego, id_empresa, titulo, descripcion, precio, recursos_minimos, id_clasificacion, fecha_lanzamiento, estado, fecha_creacion FROM videojuego WHERE id_videojuego = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Videojuegos_model.buscarPorId");
                return null;
            }

            ps = conectado.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToDTO(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error en Videojuegos_model.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conectado, conexionMySQL);
        }

        return null;
    }

    private Videojuegos_dtos mapRowToDTO(ResultSet rs) throws SQLException {
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

        return atributoVid;
    }

    // Metodo que cierra ResultSet, PreparedStatement y Connection.
    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
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
       
        conexionMySQL.desconectar(conn);
    }

}
