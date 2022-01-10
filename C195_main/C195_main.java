package C195_main;

import dao.DBConnection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import view.LoginController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.MainController;

public class C195_main extends Application {
    /* 
    Welcome to the Very Advanced Calendar Application.
	Credits to the people who made the Blade Runner movie font and the Cow emoji. 
    
    For requirement G, I added inline comments to lambda expressions with // G:
    */
    
    private Stage stage = new Stage();
    
    public static Locale localeDefault = Locale.getDefault();
    
//    public static Locale localeDefault = new Locale("es", "MX");  // Default locale lang test
//    public static Locale localeDefault = new Locale("ar", "EG");  // Egypt, Saturday week start
//    public static Locale localeDefault = new Locale("ko", "KR");  // South Korea, Sunday
//    public static Locale localeDefault = new Locale("en", "GB");  // United Kingdom, Monday
    
    public static final Path PATH = Paths.get("LOG.txt");
    public static ResourceBundle lang = ResourceBundle.getBundle("resources.lang_en");
    public static String username;
    public static int userId;
    
    private static final ZonedDateTime NOW = ZonedDateTime.now();
    public static final ZoneId TIMEZONE = NOW.getZone();
    
    public static void main(String[] args) {
        launch(args);
    }
     
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        this.stage.getIcons().add(new Image("/resources/vaca_icon.png"));
        showLoginScreen();
        System.out.println(localeDefault);    }
    
    public void showLoginScreen() {
        try { 
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(C195_main.class.getResource("/view/Login.fxml"));
            loader.setResources(lang);
            
            Parent root = loader.load();
            
            LoginController controller = loader.getController();
            controller.setLogin(this, stage);
            
            stage.setScene(new Scene(root));
            stage.show();
            
            stage.setResizable(false);
            stage.setTitle("C195 - Very Advanced Calendar Application");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sqlString = "SELECT userId "
                             + "FROM user "
                             + "WHERE userName = ? "
                             +   "AND password = ? "
                             +   "AND active = '1'";
            
            PreparedStatement ps = conn.prepareStatement(sqlString);
            ps.setString(1, username);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("userId");
                this.username = username;
                
                showMainScreen();
                checkForUpcomingAppointment();
            } else {
                if(!Files.exists(PATH)) Files.createFile(PATH);
                String timestamp = ZonedDateTime.now().toString();
                String data = "Login attempt failed: \"" + username + "\" at " 
                            + timestamp.substring(0,10) + " " + timestamp.substring(11)+ "\n";
                Files.write(Paths.get("LOG.txt"), data.getBytes(), StandardOpenOption.APPEND);
                
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(lang.getString("loginFailed"));
                alert.setHeaderText(lang.getString("loginNoMatch"));
                alert.initOwner(stage);
                alert.showAndWait();
                return;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showMainScreen() {
        try {
            Class<? extends C195_main> main = getClass();
            
            FXMLLoader loader = new FXMLLoader(main.getResource("/view/Main.fxml"), lang);
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.setChildren(
                FXMLLoader.load(main.getResource("/view/Calendar.fxml"), lang),
                FXMLLoader.load(main.getResource("/view/Schedule.fxml"), lang),
                FXMLLoader.load(main.getResource("/view/Customers.fxml"), lang),
                FXMLLoader.load(main.getResource("/view/Statistics.fxml"), lang)
            );
            
            stage.setScene(new Scene(root));
            
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
            
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }
    
    public void checkForUpcomingAppointment() {        
        try (Connection conn = DBConnection.getConnection()) {
            LocalDateTime nowUTC = NOW.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            LocalDateTime thenUTC = nowUTC.plusMinutes(15);
            
            String apptSoon = "SELECT appointment.title, customer.customerName "
                            + "FROM appointment JOIN customer "
                            + "ON appointment.customerId = customer.customerId "
                            + "WHERE start >= ? AND start <= ? "
                            +   "AND appointment.createdBy = ?;";

            try (PreparedStatement ps = conn.prepareStatement(apptSoon)) {
                ps.setObject(1, nowUTC);
                ps.setObject(2, thenUTC);
                ps.setString(3, username);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int minutes = (int)Duration.between(nowUTC, thenUTC).toMinutes();
                    String title = rs.getString("title");
                    String customer = rs.getString("customerName");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(lang.getString("appointment") + " " + lang.getString("with") 
                                       + " " + customer);
                    alert.setHeaderText(minutes + " " + lang.getString("minUntil") + " " + title);
                    alert.initOwner(stage);
                    alert.showAndWait();
                    System.out.println(minutes + lang.getString("minUntil") + title);
                }
            }
            
            String apptNow = "SELECT appointment.title, appointment.start, customer.customerName "
                           + "FROM appointment JOIN customer "
                           + "ON appointment.customerId = customer.customerId "
                           + "WHERE start < ? AND end > ? "
                           +   "AND appointment.createdBy = ?;";
            
            try (PreparedStatement ps = conn.prepareStatement(apptNow)) {
                ps.setObject(1, nowUTC);
                ps.setObject(2, nowUTC);
                ps.setString(3, username);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    LocalDateTime apptStartUTC = rs.getObject("start", LocalDateTime.class);
                    int minutes = (int)Duration.between(apptStartUTC, nowUTC).toMinutes();
                    String title = rs.getString("title");
                    String customer = rs.getString("customerName");
                    
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(lang.getString("appointment") + " " + lang.getString("with") 
                                       + " " + customer);
                    alert.setHeaderText(title + " " + lang.getString("started") + " "
                                       + minutes + " " + lang.getString("minutesAgo"));
                    alert.initOwner(stage);
                    alert.showAndWait();
                }
            }
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ObservableList<String> getConsultantList() {
        ObservableList<String> consultantList = FXCollections.observableArrayList();
        
        String sql = "SELECT userName FROM user WHERE active = 1;";
        
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            
            while(rs.next()) consultantList.add(rs.getString("userName"));
            
            return consultantList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}