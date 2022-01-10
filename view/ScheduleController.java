package view;

import C195_main.C195_main;
import static C195_main.C195_main.TIMEZONE;
import static C195_main.C195_main.username;
import dao.DBConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class ScheduleController implements Initializable {

    @FXML
    private ComboBox<String> consultantComboBox;
    @FXML
    private VBox scheduleHolder;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        consultantComboBox.setItems(C195_main.getConsultantList());
        consultantComboBox.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
           getConsultantSchedule(nv);       // G: Nice and compact, easy to read.
        });
        consultantComboBox.setValue(username);
    }
    
    @FXML
    void refreshSchedule(ActionEvent event) {
        getConsultantSchedule(consultantComboBox.getSelectionModel().getSelectedItem());
    }
    
    private void getConsultantSchedule(String consultant) {        
        try (Connection conn = DBConnection.getConnection()) {
            scheduleHolder.getChildren().clear();
            
            String sql = "SELECT title, start, end, customerName "
                       + "FROM appointment "
                       + "JOIN customer ON appointment.customerId = customer.customerId "
                       + "WHERE appointment.createdBy = ? "
//                       + "AND start >= NOW() "
                       + "ORDER BY start;";
        
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, consultant);

                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    LocalDateTime startLDT = rs.getObject("start", LocalDateTime.class)
                                               .atZone(ZoneOffset.UTC)
                                               .withZoneSameInstant(TIMEZONE)
                                               .toLocalDateTime();                            
                    LocalDateTime endLDT = rs.getObject("end", LocalDateTime.class)
                                             .atZone(ZoneOffset.UTC)
                                             .withZoneSameInstant(TIMEZONE)
                                             .toLocalDateTime();
                    AnchorPane apptPane = newAppointmentPane(
                                              String.valueOf(startLDT.toLocalDate()),
                                              startLDT.toLocalTime() + "-" + endLDT.toLocalTime(),
                                              rs.getString("title"),
                                              rs.getString("customerName")
                                          );
                    if (startLDT.isBefore(LocalDateTime.now())) apptPane.setOpacity(0.65);
                    
                    scheduleHolder.getChildren().add(apptPane);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private AnchorPane newAppointmentPane(String date, String time, String title, String customer) {
        AnchorPane apptPane = new AnchorPane();
        apptPane.getStyleClass().add("schedule-pane-style");
        apptPane.setMinHeight(50);
        
        Label dateLabel = new Label(date);
        apptPane.setTopAnchor(dateLabel, 10.0);
        apptPane.setBottomAnchor(dateLabel, 10.0);
        apptPane.setLeftAnchor(dateLabel, 10.0);
        
        Label timeLabel = new Label(time);
        apptPane.setTopAnchor(timeLabel, 10.0);
        apptPane.setBottomAnchor(timeLabel, 10.0);
        apptPane.setLeftAnchor(timeLabel, 100.0);
        
        Label titleLabel = new Label(title + " (" + customer + ")");
        apptPane.setTopAnchor(titleLabel, 10.0);
        apptPane.setBottomAnchor(titleLabel, 10.0);
        apptPane.setLeftAnchor(titleLabel, 190.0);
                
        apptPane.getChildren().add(dateLabel);
        apptPane.getChildren().add(timeLabel);
        apptPane.getChildren().add(titleLabel);
        
        return apptPane;
    }
}
