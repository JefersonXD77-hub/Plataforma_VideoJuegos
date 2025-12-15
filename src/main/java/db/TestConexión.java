/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

/**
 *
 * @author aguil
 */
public class TestConexión {

    public static void main(String[] args) {
        ConexionMySQL conexion = new ConexionMySQL();
        conexion.conectar();

    }
}
