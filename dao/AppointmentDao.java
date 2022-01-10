package dao;

import static C195_main.C195_main.TIMEZONE;
import static C195_main.C195_main.userId;
import static C195_main.C195_main.username;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;

/**
 *
 * @author Joseph
 */
public class AppointmentDao {
    public static ObservableList<Appointment> getAppointments(LocalDate start, LocalDate end) {

        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        
        String sql = "SELECT appointmentId, customerId, title, type, start, end, createdBy "
                   + "FROM appointment "
                   + "WHERE start > ? AND end < ? AND createdBy = ?;";
                
        // Add "Time" to Date
        LocalDateTime startLDT = start.atTime(LocalTime.MIN);
        LocalDateTime endLDT = end.atTime(LocalTime.MIN);

        // Datetimes offset to UTC, sent to database as "local" datetime
        LocalDateTime startUTC = startLDT.atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endUTC = endLDT.atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
//        System.out.println( "\nDisplaying datetime:" 
//                          + "\nLDT: " + startLDT + " -- " + endLDT
//                          + "\nUTC: " + startUTC + " -- " + endUTC );
        
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, startUTC);
            ps.setObject(2, endUTC);
            ps.setString(3, username);
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                int appointmentId = rs.getInt("appointmentId");
                int customerId = rs.getInt("customerId");
                String title = rs.getString("title");
                String type = rs.getString("type");
                
                startUTC = rs.getObject("start", LocalDateTime.class);
                endUTC = rs.getObject("end", LocalDateTime.class);
                
                startLDT = startUTC.atZone(ZoneOffset.UTC).withZoneSameInstant(TIMEZONE).toLocalDateTime();                            
                endLDT = endUTC.atZone(ZoneOffset.UTC).withZoneSameInstant(TIMEZONE).toLocalDateTime();
                                
                Appointment appointment = new Appointment(customerId, title, type, startLDT, endLDT);
                appointment.setAppointmentId(appointmentId);
                appointmentList.add(appointment);                
//                System.out.println( "\nAppointment: \"" + title + "\"" 
//                                  + "\nLDT: " + startLDT + " -- " + endLDT
//                                  + "\nUTC: " + startUTC + " -- " + endUTC );
            }
            return appointmentList;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean addAppointment(Appointment appointment) {
        try (Connection conn = DBConnection.getConnection()) {
            String select = "SELECT * FROM appointment "
                          + "WHERE (? < end) and (? > start);";
            
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setObject(1, appointment.getStart().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setObject(2, appointment.getEnd().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return false;
                }
            }
            
            String insert = "INSERT INTO appointment "
                          + "SET customerId = ?, userId = ?, title = ?, "
                          + "description = 'not needed', location = 'not needed', "
                          + "contact = 'not needed', type = ?, url = 'not needed', "
                          + "start = ?, end = ?, createDate = NOW(), createdBy = ?, "
                          + "lastUpdate = NOW(), lastUpdateBy = ?;";
                        
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, appointment.getCustomerId());
                ps.setInt(2, userId);
                ps.setString(3, appointment.getTitle());
                ps.setString(4, appointment.getType());
                ps.setObject(5, appointment.getStart().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setObject(6, appointment.getEnd().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setString(7, username);
                ps.setString(8, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public static boolean updateAppointment(Appointment appointment) {
        try (Connection conn = DBConnection.getConnection()) {
            String select = "SELECT * FROM appointment "
                          + "WHERE (? < end) and (? > start);";
            
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setObject(1, appointment.getStart().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setObject(2, appointment.getEnd().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return false;
                }
            }
            
            
            
            
            String sql = "UPDATE appointment "
                       + "SET title = ?, type = ?, customerId = ?, start = ?, end = ?, "
                       +     "lastUpdate = NOW(), lastUpdateBy = ? "
                       + "WHERE appointmentId = ? and userId = ?;";
                   
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, appointment.getTitle());
                ps.setString(2, appointment.getType());
                ps.setInt(3, appointment.getCustomerId());            
                ps.setObject(4, appointment.getStart().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setObject(5, appointment.getEnd().atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                ps.setString(6, username);
                ps.setInt(7, appointment.getAppointmentId());
                ps.setInt(8, userId);
                ps.executeUpdate();
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public static void deleteAppointment(Appointment appointment) {
        try (Connection conn = DBConnection.getConnection()) {
            
            // Appointment table constrains Customer table
            String deleteAp = "DELETE appointment FROM appointment "
                            + "WHERE appointmentId = ?;";            
            try (PreparedStatement ps = conn.prepareStatement(deleteAp)) {
                ps.setInt(1, appointment.getAppointmentId());
                ps.executeUpdate();
            }       
        }
        
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // A default set of appointments already exist in the database.
    // The assumption is that we won't be adding more types.
    public static ObservableList<String> getAppointmentTypes() {
        ObservableList<String> appointments = FXCollections.observableArrayList();
        
        String sql = "SELECT DISTINCT type "
                   + "FROM appointment;";
        
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while(rs.next()) {
                appointments.add(rs.getString("type"));
            }
            return appointments;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}