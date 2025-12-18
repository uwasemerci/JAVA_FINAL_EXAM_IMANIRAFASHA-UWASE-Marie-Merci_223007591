package com.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {

    

  
    public static Connection getConnection() throws Exception {
       
        Class.forName("com.mysql.cj.jdbc.Driver");

       
        String url = "jdbc:mysql://localhost/ecomerce_automation_system";  // ✅ your DB name
        String user = "root";       // ✅ MySQL username
        String password = "";       // ✅ MySQL password

     
        return DriverManager.getConnection(url, user, password);
    }
}
