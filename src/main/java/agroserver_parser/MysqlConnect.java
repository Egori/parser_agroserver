/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agroserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnect {
    
        // init connection object
    private Connection connection;
    // init properties object
    private Properties properties;
    
    private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
    private  String DATABASE_URL = "";

        private static final String USERNAME = "user";
        private static final String PASSWORD = "pass";

    public MysqlConnect(String baseName) {
 
        this.DATABASE_URL = "jdbc:mysql://localhost:3306/" + baseName;
    }
      

    // create properties
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("useServerPrepStmts", "true");
            //properties.setProperty("MaxPooledStatements", MAX_POOL);
          //  properties.setProperty("useUnicode","true");
        properties.setProperty("characterEncoding","UTF-8");
        }
        return properties;
    }

    // connect database
    public Connection connect() {
        if (connection == null) {
            try {
                System.out.println("agroserver.MysqlConnect.connect()");
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    // disconnect database
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}