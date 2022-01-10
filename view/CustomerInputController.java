package view;

import static C195_main.C195_main.lang;
import dao.CustomerDao;
import dao.LocationsDao;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Customer;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class CustomerInputController extends CustomersController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField address1Field;
    @FXML
    private TextField address2Field;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField postalField;
    @FXML
    private ComboBox<String> countryComboBox;
    @FXML
    private ComboBox<String> cityComboBox;
    
    private Stage stage;
    
    private Customer customer = null;
        

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        countryComboBox.getItems().setAll(countryList);
    }
        
    void setStage(Stage stage) {
        this.stage = stage;
    }
    
    // Only called when a Customer is being updated; otherwise "this" customer is null
    void setCustomer(Customer customer) {
        this.customer = customer;
        
        // Initial text fields when a Customer is being updated
        nameField.setText(customer.getName());
        address1Field.setText(customer.getAddress1());
        address2Field.setText(customer.getAddress2());
        phoneField.setText(customer.getPhone());
        postalField.setText(customer.getPostal());
        countryComboBox.getSelectionModel().select(customer.getCountry());
        cityComboBox.getItems().setAll(LocationsDao.initializeCities(countryComboBox.getValue()));
        cityComboBox.getSelectionModel().select(customer.getCity());     
    }
    
    @FXML
    public void handleSave() {
        if((nameField.getText() != null && !nameField.getText().trim().isEmpty()) &&
           (phoneField.getText() != null && !phoneField.getText().trim().isEmpty()) && 
           (address1Field.getText() != null && !address1Field.getText().trim().isEmpty()) &&
           (postalField.getText() != null && !postalField.getText().trim().isEmpty()) &&
           address2Field.getText() != null && cityComboBox.getValue() != null && 
           countryComboBox.getValue() != null ) {
        
            String name = nameField.getText().trim();
            String phone = phone = phoneField.getText().trim();
            String address1 = address1Field.getText().trim();
            String address2 = address2Field.getText().trim();
            String postal = postalField.getText().trim();
            String city = cityComboBox.getValue();
            String country = countryComboBox.getValue();

            // Add new Customer
            if (customer == null) {
                customer = new Customer(name, phone, address1, address2, postal, city, country);
                customer.setId(CustomerDao.addCustomer(customer));
            }    
            // Update this Customer
            else {
                customer.setName(name);
                customer.setPhone(phone);
                customer.setAddress1(address1);
                customer.setAddress2(address2);
                customer.setPostal(postal);
                customer.setCity(city);
                customer.setCountry(country);
                CustomerDao.updateCustomer(customer);
            }
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(lang.getString("infoMissing"));
            alert.initOwner(stage);
            alert.showAndWait();
        }
    }
    
    public Customer returnNewCustomer() {
        return customer;
    }

    
    @FXML
    public void handleCancel() {
        stage.close();
    }
    
    // Sets the selection of cities available in a country
    @FXML
    void handleCountry(ActionEvent event) {
        cityComboBox.getItems().clear();
        cityComboBox.getItems().setAll(LocationsDao.initializeCities(countryComboBox.getValue()));
    }
}