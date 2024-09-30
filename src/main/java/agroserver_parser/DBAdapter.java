/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author user
 */
public class DBAdapter {
    
    String dbName = null;
    private static MysqlConnect connectMysql;
    static Connection connection = null;
    
    public DBAdapter(String dbName) {
        this.dbName = dbName;
        connectMysql = new MysqlConnect(dbName);
        
    }
    
    public boolean setEmailToDB(String userPage, String email) {
        boolean result = true;
        if(connection == null){
             connection = connectMysql.connect();
        }
        PreparedStatement ps = null;
        
        try {
            // ? - место вставки нашего значеня
            ps = connection.prepareStatement(
                    "INSERT INTO users SET user_page=?, email=?");
            ps.setString(1, userPage);
            ps.setString(2, email);
        } catch (SQLException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
            // Duplicate entry
        }

        try {
            int executeUpdate = ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            System.err.println("Errror: setCacheUserToDB()");
        }
        
        return result;
        
    }
    
    public String getCacheUserFromDB(String userPage) {
        String result = null;
        if(connection == null){
             connection = connectMysql.connect();
        }
        PreparedStatement ps = null;
        
        try {
            // ? - место вставки нашего значеня
            ps = connection.prepareStatement(
                    "SELECT * FROM users WHERE user_page=?");
            ps.setString(1, userPage);
        } catch (SQLException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResultSet rs;
        try {
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Errror: setCacheUserToDB()");
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, e);
        }     
        
        return result;
        
    }
    

    
    
}
