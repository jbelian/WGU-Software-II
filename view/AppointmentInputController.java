package view;

import static C195_main.C195_main.lang;
import dao.AppointmentDao;
import dao.CustomerDao;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import static view.CustomersController.customerList;


/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class AppointmentInputController extends CalendarController implements Initializable  {

    @FXML
    private TextField titleField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> meetingComboBox;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<LocalTime> startTimeComboBox;
    @FXML
    private ComboBox<LocalTime> endTimeComboBox;
    @FXML
    private Button deleteBtn;

    private Stage stage;
    
    private Appointment appointment = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startTimeComboBox.setItems(BUSINESS_TIME_LIST);
        endTimeComboBox.setItems(BUSINESS_TIME_LIST);
        customerComboBox.setItems(customerList);
        meetingComboBox.setItems(appointmentTypeList);
        
        startTimeComboBox.setValue(LocalTime.of(8, 0));
        endTimeComboBox.setValue(LocalTime.of(9, 0));
        
        deleteBtn.setVisible(false);
    }
    
    void setStage(Stage stage, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.stage = stage;
        
        startDatePicker.setValue(date);
        endDatePicker.setValue(date);
        if (startTime != null && endTime != null) {
            startTimeComboBox.setValue(startTime);
            endTimeComboBox.setValue(endTime);
        }
    }

    void setAppointment(Appointment appointment) {
        this.appointment = appointment;
                
        titleField.setText(appointment.getTitle());        
        meetingComboBox.setValue(appointment.getType());
        customerComboBox.setValue(CustomerDao.getCustomerById(appointment.getCustomerId()));
        startTimeComboBox.setValue(appointment.getStart().toLocalTime());
        endTimeComboBox.setValue(appointment.getEnd().toLocalTime());
        
        deleteBtn.setVisible(true);
    }
        
    @FXML
    void handleSave() {
        if (customerComboBox.getValue() != null && meetingComboBox.getValue() != null &&
           (titleField.getText() != null && !titleField.getText().trim().isEmpty()) ) {
        
            Customer customer = customerComboBox.getValue();
            String title = titleField.getText().trim();
            LocalDateTime start = startDatePicker.getValue().atTime(startTimeComboBox.getValue());
            LocalDateTime end = endDatePicker.getValue().atTime(endTimeComboBox.getValue());
            String type = meetingComboBox.getValue();
            
            if (start.isBefore(end)) {
                
                if(!start.toLocalTime().isBefore(earliestHour) && !end.toLocalTime().isAfter(latestHour)) {
                    
                    // Add new appointment
                    if (appointment == null) {                        
                        if (AppointmentDao.addAppointment(new Appointment(
                                customer.getId(), title, type, start, end)) == true) {
                            stage.close();
                        } else {
                            handleInfoAlert(lang.getString("appointmentOverlap"));
                        }
                    }
                    // Update this appointment
                    else {
                        appointment.setCustomerId(customer.getId());
                        appointment.setTitle(title);
                        appointment.setType(type);
                        appointment.setStart(start);
                        appointment.setEnd(end);
                        if (AppointmentDao.updateAppointment(appointment) == true)
                            stage.close();
                        else
                            handleInfoAlert(lang.getString("appointmentOverlap"));
                    }
                } else {
                    handleInfoAlert(lang.getString("outsideBusinessHours") 
                                   + earliestHour + "-" + latestHour);
                }
            } else {
                handleInfoAlert(lang.getString("timeImpossible"));
            }            
        } else {
            handleInfoAlert(lang.getString("infoMissing"));
        }
    }
    
    void handleInfoAlert (String string) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(string);
        alert.initOwner(stage);
        alert.showAndWait();
    }
    
    @FXML
    void handleDelete() {
        if(appointment != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(lang.getString("deleteAppointment"));
            alert.initOwner(stage);            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                AppointmentDao.deleteAppointment(appointment);
                stage.close();
            }
        }
    }
}