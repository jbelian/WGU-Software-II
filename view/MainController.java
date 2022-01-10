package view;

import static C195_main.C195_main.TIMEZONE;
import static C195_main.C195_main.lang;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Polygon;


/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class MainController implements Initializable {
    @FXML
    private BorderPane root;
    @FXML
    private AnchorPane calPane;
    @FXML
    private AnchorPane schedPane;
    @FXML
    private AnchorPane custPane;
    @FXML
    private AnchorPane statPane;
    @FXML
    private Polygon sidebarPointer;
    @FXML
    private Label leftStatus;
    @FXML
    private Label rightStatus;
    
    // Children
    private static Node calendar;
    private static Node schedule;
    private static Node customers;
    private static Node statistics;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        leftStatus.setText(lang.getString("timeZone") + TIMEZONE);

        // Alternate timezone style:
        // TIMEZONE.getDisplayName(TextStyle.FULL, localeDefault)
        
        // TODO Display connection acquired/lost/failed
        // rightStatus.setText("Connected to database");
    }
        
    public void showScreen(Node node, AnchorPane pane) {
        if(root.getCenter() != node) {
            root.setCenter(node);
            pane.getChildren().add(sidebarPointer);
        }
    }
    public void showCalendarScreen(MouseEvent event) {
        showScreen(calendar, calPane);
    }
    public void showScheduleScreen(MouseEvent event) {
        showScreen(schedule, schedPane);
    }
    public void showCustomersScreen(MouseEvent event) {
        showScreen(customers, custPane);
    }
    public void showStatisticsScreen(MouseEvent event) {
        showScreen(statistics, statPane);
    }

    public void setChildren(Node calendar, Node schedule, Node customers, Node statistics) {
        this.calendar = calendar;
        this.schedule = schedule;
        this.customers = customers;
        this.statistics = statistics;
        root.setCenter(calendar);
    }
}