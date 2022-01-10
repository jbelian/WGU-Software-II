package view;

import static C195_main.C195_main.lang;
import dao.CustomerDao;
import dao.LocationsDao;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Customer;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class CustomersController implements Initializable {
    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    @FXML
    private TableColumn<Customer, String> address1Col;
    @FXML
    private TableColumn<Customer, String> address2Col;
    @FXML
    private TableColumn<Customer, String> postalCol;    
    @FXML
    private TableColumn<Customer, String> cityCol;
    @FXML
    private TableColumn<Customer, String> countryCol;
    
    // TODO Delete column
    @FXML
    private TableColumn<?, ?> deleteCol;
    
    protected static ObservableList<Customer> customerList = FXCollections.observableArrayList();
    protected static List<String> countryList;
        

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // G: Errors will be caught at compile time unlike PropertyValueFactory,
        // which is what I originally used.
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().getPhoneProperty());
        address1Col.setCellValueFactory(cellData -> cellData.getValue().getAddress1Property());
        address2Col.setCellValueFactory(cellData -> cellData.getValue().getAddress2Property());
        postalCol.setCellValueFactory(cellData -> cellData.getValue().getPostalProperty());
        cityCol.setCellValueFactory(cellData -> cellData.getValue().getCityProperty());
        countryCol.setCellValueFactory(cellData -> cellData.getValue().getCountryProperty());
        
        customerList = CustomerDao.getActiveCustomers();
        customersTable.setItems(customerList);
        
        countryList = LocationsDao.initializeCountries();
    }
        
    private void showCustomerInput(Boolean update) {    
        try {      
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/CustomerInput.fxml"));
            loader.setResources(lang);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle(lang.getString("customerDetails"));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            CustomerInputController controller = loader.getController();
            controller.setStage(stage);
            
            // Update selected customer
            if(update == true) {
                controller.setCustomer(customersTable.getSelectionModel().getSelectedItem());
                stage.showAndWait();
            }
            // New customer being added, text fields on input screen left blank
            else {
                stage.showAndWait();
                
                // When the input window is closed, check if a customer was created/saved
                if(controller.returnNewCustomer() != null) {
                    customerList.add(controller.returnNewCustomer());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void addCustomerBtn(ActionEvent event) {
        showCustomerInput(false);
    }
    
    public void updateCustomerBtn(ActionEvent event) {
        if (customersTable.getSelectionModel().getSelectedItem() != null) {
            showCustomerInput(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(lang.getString("noCustomerSelected"));
            alert.showAndWait();
        }
    }
    
    public void deleteCustomerBtn(ActionEvent event) {
        if (customersTable.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(lang.getString("deleteCustomer"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                CustomerDao.deleteCustomer(customersTable.getSelectionModel().getSelectedItem());
                customerList.remove(customersTable.getSelectionModel().getSelectedItem());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(lang.getString("noCustomerSelected"));
            alert.showAndWait();
        }
    }
}