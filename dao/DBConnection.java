package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Joseph
 */
public class DBConnection {
//    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_USER = "U05KqI";
    private static final String DB_PASS = "53688527578";
    private static final String DB_URL = "jdbc:mysql://3.227.166.251/" + DB_USER;
    
    public static Connection getConnection() {
        try {
            return (Connection)DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e){
            e.printStackTrace();
        } 
        return null;
    }
}
    
//    private static Connection conn;
//    public static void makeConnection() {
//        try {
//            Class.forName(JDBC_DRIVER);
//            conn = (Connection)DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
//            System.out.println("Connection made: " + conn);
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//            System.out.println("No connection made");
//        } 
//    }
//        
//    public static void closeConnection() {
//        try {
//            conn.close();
//            System.out.println("Connection closed");
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Connection failed to close");
//        } 
//    }