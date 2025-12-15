/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionMySQL {

    private static final String URL =
        "jdbc:mysql://localhost:3306/plataforma_videojuegos"
      + "?useSSL=false"
      + "&allowPublicKeyRetrieval=true"
      + "&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "12345";

    public Connection conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conecto = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("Conexión exitosa.");
            return conecto;

        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el driver JDBC de MySQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("Conexión fallida: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void desconectar(Connection c) {
        if (c != null) {
            try {
                c.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
