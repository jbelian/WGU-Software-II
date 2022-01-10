package view;

import C195_main.C195_main;
import static C195_main.C195_main.lang;
import static C195_main.C195_main.localeDefault;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class LoginController implements Initializable {

    @FXML
    private Button loginBtn;
    @FXML
    private Label userLabel;
    @FXML
    private Label passLabel;
    @FXML
    private TextField userField;
    @FXML
    private TextField passField;
    
    private C195_main main;
    private Stage stage;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(localeDefault.getLanguage().equals("es")) lang = ResourceBundle.getBundle("resources.lang_es");
        else lang = ResourceBundle.getBundle("resources.lang_en");
        userLabel.setText(lang.getString("username"));
        passLabel.setText(lang.getString("password"));
        loginBtn.setText(lang.getString("login"));
    }
    
    public void setLogin(C195_main main, Stage stage) {
        this.main = main;
        this.stage = stage;
    }

    @FXML
    private void handleLogin() throws IOException {        
        if(userField.getText() == null || userField.getText().trim().isEmpty() ||
           passField.getText() == null || passField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lang.getString("loginFailed"));
            alert.setHeaderText(lang.getString("loginInfoMissing"));
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        } else {
            main.login(userField.getText(), passField.getText());
        }
    }  
}