package view;

import static C195_main.C195_main.TIMEZONE;
import static C195_main.C195_main.lang;
import static C195_main.C195_main.username;
import dao.DBConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class StatisticsController implements Initializable {

    @FXML
    private Label scheduleYearLabel;
    @FXML
    private Button statisticsUpdate;
    @FXML
    private BarChart<String, Number> apptTypesBarChart;
    @FXML
    private PieChart citiesPieChart;
    
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    LocalDateTime date = LocalDate.now().withDayOfYear(1).atStartOfDay();
    LocalDateTime prevDate = date.minusYears(1);
    LocalDateTime nextDate = date.plusYears(1);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        getStatistics(date);
        apptTypesBarChart.setLegendVisible(false);
    }
    
    private void getStatistics(LocalDateTime date) {
        try (Connection conn = DBConnection.getConnection()) {
            apptTypesBarChart.getData().clear();
            scheduleYearLabel.setText(String.valueOf(date.getYear()));
            XYChart.Series dataSeries = new XYChart.Series();
                        
            LocalDateTime startUTC = date
                .atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            LocalDateTime endUTC = date.plusYears(1)
                .atZone(TIMEZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            
            String apptTypes = "SELECT DISTINCT type "
                             + "FROM appointment "
                             + "WHERE start >= ? AND end < ? AND createdBy = ?;";
        
            try (PreparedStatement ps = conn.prepareStatement(apptTypes)) {
                while (startUTC.isBefore(endUTC)) {
                    String month = lang.getString(startUTC.getMonth().toString()).substring(0, 3);
                    
                    ps.setObject(1, startUTC);
                    ps.setObject(2, startUTC = startUTC.plusMonths(1));
                    ps.setString(3, username);
                    ResultSet rs = ps.executeQuery();
                    
                    int i = 0;                    
                    while(rs.next()) i++;
                    
                    dataSeries.getData().add(new XYChart.Data<>(month, i));
                }
                apptTypesBarChart.getData().add(dataSeries);
            }
            
            String cityTypes = "SELECT city.city, COUNT(*) AS num "
                             + "FROM appointment "
                             + "JOIN customer ON appointment.customerId = customer.customerId "
                             + "JOIN address ON customer.addressId = address.addressId "
                             + "JOIN city ON address.cityId = city.cityId "
                             + "WHERE start >= ? AND end < ? "
                             + "AND appointment.createdBy = ? "
                             + "GROUP BY city "
                             + "ORDER BY \"num\" ASC;";
            
            
            try (PreparedStatement ps = conn.prepareStatement(cityTypes)) {
                startUTC = startUTC.minusYears(1);
                
                ps.setObject(1, startUTC);
                ps.setObject(2, endUTC);
                ps.setString(3, username);
                ResultSet rs = ps.executeQuery();
                
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                while(rs.next()) {
                    pieChartData.add(new PieChart.Data(rs.getString("city"), rs.getInt("num")));
                }
                
                citiesPieChart.setData(pieChartData);
                
                
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void updateStatistics(ActionEvent event) {
        getStatistics(date);
    }
    @FXML
    private void prevSchedYear(ActionEvent event) {
        getStatistics(prevDate);
        date = prevDate;
        prevDate = date.minusYears(1);
        nextDate = date.plusYears(1);
    }
    @FXML
    private void nextSchedYear(ActionEvent event) {
        getStatistics(nextDate);
        date = nextDate;
        prevDate = date.minusYears(1);
        nextDate = date.plusYears(1);
    }
}