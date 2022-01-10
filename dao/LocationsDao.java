package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Joseph
 */

// A default set of countries and cities exists in the database; the assumption
// is that we'll only be working in these areas and not adding more.
public class LocationsDao {
        
    public static List<String> initializeCountries() {
        List<String> countries = new ArrayList<>();
        
        String sql = "SELECT country "
                   + "FROM country "
                   + "ORDER BY country";
        
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while(rs.next()) {
                countries.add(rs.getString("country"));
            }
            return countries;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
        
    public static List<String> initializeCities(String country) {
        List<String> cities = new ArrayList<>();
        
        String sql = "SELECT city.city "
                   + "FROM city, country "
                   + "WHERE city.countryId = country.countryId "
                   + "AND country.country = ? "
                   + "ORDER BY city";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    cities.add(rs.getString("city"));
                }
            return cities; 
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
