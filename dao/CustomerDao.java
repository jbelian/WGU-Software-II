package dao;

import static C195_main.C195_main.username;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Customer;

/**
 *
 * @author Joseph
 */
public class CustomerDao {   
    public static ObservableList<Customer> getActiveCustomers() {
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        
        String sql = "SELECT customer.customerId, customer.customerName, address.address, address.address2, "
                   +        "address.postalCode, address.phone, city.city, country.country "
                   + "FROM customer "
                   + "INNER JOIN address ON customer.addressId = address.addressId "
                   + "INNER JOIN city ON address.cityId = city.cityId "
                   + "INNER JOIN country ON city.countryId = country.countryId "
                   + "WHERE customer.active = 1;";
        
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while(rs.next()) {
                int id = rs.getInt("customerId");
                String name = rs.getString("customerName");
                String address1 = rs.getString("address");
                String address2 = rs.getString("address2");
                String postal = rs.getString("postalCode");
                String phone = rs.getString("phone");
                String city = rs.getString("city");
                String country = rs.getString("country");
                Customer customer = new Customer(id, name, phone, address1, address2, postal, city, country);
                customerList.add(customer);
            }
            return customerList;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Customer getCustomerById(int id) {
        String sql = "SELECT customer.customerName, address.address, address.address2, "
                   +        "address.postalCode, address.phone, city.city, country.country "
                   + "FROM customer "
                   + "INNER JOIN address ON customer.addressId = address.addressId "
                   + "INNER JOIN city ON address.cityId = city.cityId "
                   + "INNER JOIN country ON city.countryId = country.countryId "
                   + "WHERE customer.active = 1 "
                   + "AND customer.customerId = ?;";

        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String name = rs.getString("customerName");
                String address1 = rs.getString("address");
                String address2 = rs.getString("address2");
                String postal = rs.getString("postalCode");
                String phone = rs.getString("phone");
                String city = rs.getString("city");
                String country = rs.getString("country");
                return new Customer(id, name, phone, address1, address2, postal, city, country);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
        
    public static int addCustomer(Customer customer) {
        try (Connection conn = DBConnection.getConnection()) {
            // Add Address
            String insertAd = "INSERT INTO address "
                            + "SET address = ?, address2 = ?, postalCode = ?, "
                            +     "phone = ?, createDate = NOW(), createdBy = ?, "
                            +     "lastUpdate = NOW(), lastUpdateBy = ?, "
                            +     "cityId = (SELECT cityId "
                            +               "FROM city "
                            +               "WHERE city = ?);";
            
            try (PreparedStatement ps = conn.prepareStatement(insertAd)) {
                ps.setString(1, customer.getAddress1());
                ps.setString(2, customer.getAddress2());
                ps.setString(3, customer.getPostal());
                ps.setString(4, customer.getPhone());
                ps.setString(5, username);
                ps.setString(6, username);
                ps.setString(7, customer.getCity());
                ps.executeUpdate();
            }
            
            // Add Customer
            String insertCu = "INSERT INTO customer "
                            + "SET customerName = ?, addressId = LAST_INSERT_ID(), "
                            +     "active = 1, createDate = NOW(), createdBy = ?, "
                            +     "lastUpdate = NOW(), lastUpdateBy = ?;";
                            
            try (PreparedStatement ps = conn.prepareStatement(insertCu)) {
                ps.setString(1, customer.getName());
                ps.setString(2, username);
                ps.setString(3, username);
                ps.executeUpdate();
            }
            
            // Return the Customer ID that was auto-incremented in the database
            ResultSet rs = conn.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
            rs.next();
            return rs.getInt("LAST_INSERT_ID()");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static void updateCustomer(Customer customer) {        
        String update = "UPDATE customer AS cu, address AS ad "
                      + "SET cu.customerName = ?, cu.lastUpdate = NOW(), cu.lastUpdateBy = ?, "
                      +     "ad.address = ?, ad.address2 = ?, ad.postalCode = ?, "
                      +     "ad.phone = ?, ad.lastUpdate = NOW(), ad.lastUpdateBy = ? "
                      + "WHERE cu.addressId = ad.addressId "
                      + "AND cu.customerId = ?;";
                   
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(update)) {
            
            ps.setString(1, customer.getName());
            ps.setString(2, username);
            ps.setString(3, customer.getAddress1());
            ps.setString(4, customer.getAddress2());
            ps.setString(5, customer.getPostal());
            ps.setString(6, customer.getPhone());
            ps.setString(7, username);
            ps.setInt(8, customer.getId());
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteCustomer(Customer customer) {
        try (Connection conn = DBConnection.getConnection()) {
            int id = customer.getId();
            
            // Appointment table constrains Customer table
            String deleteAp = "DELETE appointment FROM appointment, customer "
                            + "WHERE appointment.customerId = customer.customerId "
                            + "AND customer.customerId = ?;";            
            try (PreparedStatement ps = conn.prepareStatement(deleteAp)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            
            // Customer and Address tables free to be deleted
            String deleteCu = "DELETE address, customer FROM address, customer "
                            + "WHERE address.addressId = customer.addressId "
                            + "AND customer.customerId = ?;";
            try (PreparedStatement ps = conn.prepareStatement(deleteCu)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }            
        }
        
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}