package view;

import static C195_main.C195_main.lang;
import static C195_main.C195_main.localeDefault;
import dao.AppointmentDao;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Appointment;

/**
 * FXML Controller class
 *
 * @author Joseph
 */
public class CalendarController implements Initializable {

    @FXML
    private Label calendarLabel;
    @FXML
    private ToggleGroup calendarToggle;
    @FXML
    private ToggleButton weekButton;
    @FXML
    private ToggleButton monthButton;
    @FXML
    private GridPane calRootGrid;
    
    // Date variables
    private static final DayOfWeek firstDayOfWeekByLocale = WeekFields.of(localeDefault).getFirstDayOfWeek();
    private static LocalDate selectedDate = LocalDate.now();
    private static LocalDate prevMonthDate = selectedDate.minusMonths(1);
    private static LocalDate nextMonthDate = selectedDate.plusMonths(1);
    private static LocalDate prevWeekDate = selectedDate.minusWeeks(1);
    private static LocalDate nextWeekDate = selectedDate.plusWeeks(1);
    
    protected static final LocalTime earliestHour = LocalTime.of(8, 0); // Business hours
    protected static final LocalTime latestHour = LocalTime.of(17, 0);
    
    // Grids and related
    private static int gridsize = 0;     // Days in monthly view
    private static GridPane headerGrid = new GridPane();
    private static GridPane monthlyGrid = new GridPane();
    private static GridPane weeklyGrid = new GridPane();
    private static GridPane weeklyHourUnitGrid = new GridPane();
    private static AnchorPane headerHolder = new AnchorPane();
    private static HBox weeklyHolder = new HBox();     // Holds weeklyHourUnitGrid and weeklyGrid in week view
    private static ScrollPane weeklyHolderScrollPane = new ScrollPane(); // Holds weeklyHolder in week view
    private static ColumnConstraints colConstraints = new ColumnConstraints(); 
    private static ColumnConstraints hourColConstraints = new ColumnConstraints(40, 40, 40);
    private static RowConstraints rowConstraints = new RowConstraints(25, 25, 25);
    
    // Appointment field data
    protected static final ObservableList<LocalTime> BUSINESS_TIME_LIST = FXCollections.observableArrayList();
    protected static ObservableList<String> appointmentTypeList;
        
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        calRootGrid.add(headerHolder, 0, 0);
        headerHolder.getChildren().add(headerGrid);
        headerHolder.setTopAnchor(headerGrid, 0.0); 
        headerHolder.setLeftAnchor(headerGrid, 0.0); 
        headerHolder.setRightAnchor(headerGrid, 0.0); 
        headerHolder.setBottomAnchor(headerGrid, 0.0);
        headerHolder.getStyleClass().add("calendar-header-style");
        
        // Month and week buttons
        monthButton.fire();
        monthButton.getStyleClass().remove("radio-button");
        monthButton.getStyleClass().add("toggle-button");
        weekButton.getStyleClass().remove("radio-button");
        weekButton.getStyleClass().add("toggle-button");
        calendarToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldBtn, Toggle newBtn) {
                ((RadioButton)oldBtn).setDisable(false);
                ((RadioButton)newBtn).setDisable(true);
            }
        });
        
        // Calendar always has 7 columns, so constraints are set once; rows can change in monthly view
        rowConstraints.setPercentHeight(100);
        colConstraints.setPercentWidth(100);
        for(int i = 0; i < 7; i++) {      // For 7 days
            monthlyGrid.getColumnConstraints().add(colConstraints);
            weeklyGrid.getColumnConstraints().add(colConstraints);
        }
        for(int i = 0; i < 96; i++) {     // For all 15 minute blocks of time in day
            weeklyGrid.getRowConstraints().add(rowConstraints);
        }
        
        // Weekly view
        weeklyHolderScrollPane.setContent(weeklyHolder);
        weeklyHolderScrollPane.setFitToWidth(true);
        weeklyHolder.getChildren().add(weeklyHourUnitGrid);
        weeklyHolder.getChildren().add(weeklyGrid);
        weeklyHolder.setFillHeight(true);
        weeklyHolder.setHgrow(weeklyGrid, Priority.ALWAYS);

        // Weekly view hours
        weeklyHourUnitGrid.getColumnConstraints().add(hourColConstraints);
        List<LocalTime> times = new ArrayList();
        LocalTime newHour = LocalTime.MIDNIGHT;
        int hourlyRow = 0;
        do {
            weeklyHourUnitGrid.getRowConstraints().add(rowConstraints);
            Pane pane = new Pane();
            Label label = new Label(String.valueOf(newHour));
            pane.setMinWidth(10);
            weeklyHourUnitGrid.add(pane, 0, hourlyRow);
            weeklyHourUnitGrid.add(label, 0, hourlyRow);
            
            pane.getStyleClass().add("calendar-date-pane-style");
            label.getStyleClass().add("hourly-unit-style");
            weeklyHourUnitGrid.setHalignment(label, HPos.CENTER);
            weeklyHourUnitGrid.setValignment(label, VPos.TOP);
            label.setTranslateY(-9);
            label.setTranslateX(-2);
            
            times.add(newHour);
            newHour = newHour.plusHours(1);     
            hourlyRow++;
        } while(newHour != LocalTime.MIDNIGHT);
        
        
        // Styling
        calRootGrid.setMargin(monthlyGrid, new Insets(1,0,0,0));
        monthlyGrid.getStyleClass().add("calendar-grid-background-color");
        monthlyGrid.setHgap(1);
        monthlyGrid.setVgap(1);
        weeklyGrid.getStyleClass().add("calendar-grid-background-color");
        weeklyGrid.setHgap(1);
        weeklyGrid.setVgap(1);
        weeklyHourUnitGrid.getStyleClass().add("calendar-grid-background-color");
        weeklyHourUnitGrid.setHgap(1);
        weeklyHourUnitGrid.setVgap(1);
        weeklyHolder.setMargin(weeklyGrid, new Insets(0,0,0,1));
        weeklyHolderScrollPane.getStyleClass().add(("edge-to-edge"));
        
        // Blocks of time separated by 15 minute intervals within business hours
        LocalTime businessTime = earliestHour;
        do {
            BUSINESS_TIME_LIST.add(businessTime);
            businessTime = businessTime.plusMinutes(15);
        } while(!businessTime.isAfter(latestHour));
  
        // List of strings of appointment types
        appointmentTypeList = AppointmentDao.getAppointmentTypes().sorted();
    }
    
    private void setWeekdayHeaders(List<String> dayOfWeekNumbers) {
        headerGrid.getChildren().clear();
        headerGrid.getColumnConstraints().clear();
        DayOfWeek dayOfWeek = firstDayOfWeekByLocale;

        List<String> dayNameList = new ArrayList();

        do {                
            dayNameList.add(lang.getString(dayOfWeek.toString()));
            dayOfWeek = dayOfWeek.plus(1);
        } while ( (dayOfWeek != firstDayOfWeekByLocale) );
        
        for(int i = 0; i < 7; i++) {
            Pane pane = new Pane();
            Label label = new Label();
            if (weekButton.isSelected()) {
                label.setText(dayOfWeekNumbers.get(i) + " " + dayNameList.get(i).substring(0, 3));
            } else {
                label.setText(dayNameList.get(i));
            }
            headerGrid.setHalignment(label, HPos.CENTER);
            headerGrid.add(pane, i, 0);
            headerGrid.add(label, i, 0);
            headerGrid.getColumnConstraints().add(i, colConstraints);
        }
    }           


    
    private void createWeekPage() {
        LocalDate calendarDate = selectedDate.with(WeekFields.of(localeDefault).dayOfWeek(), 1);
        LocalDate startDate = calendarDate;
        
        // Get appointments within range
        List<Appointment> appointments = AppointmentDao
                .getAppointments(startDate, startDate.plusWeeks(1));
        
        // For each day
        List<String> dayOfWeekNumbers = new ArrayList();
        for(int col = 0; col < 7; col++, calendarDate = calendarDate.plusDays(1)) {
            dayOfWeekNumbers.add(String.valueOf(calendarDate.getDayOfMonth()));
            
            final LocalDate date = calendarDate;
            LocalTime time = LocalTime.MIN;
            
            // Week pane has 96 rows representing 15 minute blocks of time.
            // Add a pane that spans 4 rows and represents a clickable hour for appointments.
            for(int i = 0; i < 24; i++) {
                Pane pane = new Pane();
                weeklyGrid.add(pane, col, i * 4, 1, 4);
                weeklyGrid.setHgrow(pane, Priority.ALWAYS);
                weeklyGrid.setVgrow(pane, Priority.ALWAYS);
                pane.getStyleClass().add("calendar-date-pane-style");

                final LocalTime timeStart = time;
                final LocalTime timeEnd = time.plusHours(1);

                pane.setOnMouseClicked(e -> {  // G: Lambda expression looks cleaner than a named method.
                e.consume();
                if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                    showApptInput(pane, null, date, timeStart, timeEnd);
                    System.out.println( "Pane with date " + date + ", time " 
                                      + timeStart + "-" + timeEnd + " clicked" );
                    }
                });
                time = timeEnd;
            }
                        
            // Collect appointments on this day
            List<Appointment> filteredAppts = appointments.stream()   // G: Conflicting opinions on
                .filter(a -> a.getStart().toLocalDate().equals(date)) // efficiency from stackoverflow;
                .collect(Collectors.toList());                        // regardless it's very readable.
            
            // Add appointment to calendar, spans from start to end times
            final int column = col;
            if(!filteredAppts.isEmpty()) {
                filteredAppts.forEach((Appointment a) -> {          // G: Stylistic choice? I don't think
                    LocalTime startLocalTime = a.getStart().toLocalTime();  // it's more efficient; just
                    LocalTime endLocalTime = a.getEnd().toLocalTime();      // easier to read than a for-
                    Label label = new Label(a.getTitle() + "\n"             // loop, personally. They'll
                                          + startLocalTime + " - "          // compile the same probably.
                                          + endLocalTime);
                    
                    int startRow = (startLocalTime.getHour() * 4) + (startLocalTime.getMinute() / 15);
                    int endRow = (endLocalTime.getHour() * 4) + (endLocalTime.getMinute() / 15);
                    int span = endRow - startRow;
                    if(span < 1) span = 1;  // In case the DB has impossible start/end times
                    
                    VBox pane = new VBox(label);
                    pane.getStyleClass().add("appointment-pane-style");
                    label.getStyleClass().add("appointment-label-style");
                    weeklyGrid.add(pane, column, startRow, 1, span);
                    
                    // Click to edit the appointment with this date and time
                    pane.setOnMouseClicked(e -> {   // G: Answered on line 251
                    e.consume();
                    if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                        showApptInput(pane, a, date, null, null);
                        System.out.println( "Appointment with date " + date + ", time " 
                                          + startLocalTime + "-" + endLocalTime + " clicked" );
                        }
                    });
                });
            }
        }
        
        // Set weekday, month, and year display text
        setWeekdayHeaders(dayOfWeekNumbers);     
        LocalDate endDate = startDate.plusDays(6);
        String startString = lang.getString(String.valueOf(startDate.getMonth()));
        String endString = lang.getString(String.valueOf(endDate.getMonth()));        
        if (startString.equals(endString)) {
            calendarLabel.setText(endString + " " + endDate.getYear());
        }
        else {
            startString = startString.substring(0, 3);
            endString = endString.substring(0, 3) + " " + endDate.getYear();
            if (startDate.getYear() != endDate.getYear()) {
                startString = startString + " " + startDate.getYear();
            }
            calendarLabel.setText(startString + " - " + endString);
        }
    }
    
    // setWeeklyAppts(){} appts created above
    
    private void createMonthPage() {
        // "Offset" represents the ordinal day of the week of the first of the month
        int offset = getOrdinalDay(1);        
        gridsize = (int)(Math.ceil((offset + selectedDate.lengthOfMonth() - 1) / 7) * 7 + 7);        
        LocalDate calendarDate = selectedDate.withDayOfMonth(1).minusDays(offset);
        LocalDate endDate = calendarDate.plusDays(gridsize);
        List<Appointment> appointments = AppointmentDao.getAppointments(calendarDate, endDate);

        boolean isMainStyle = false;
        
        int rows = gridsize / 7;
        monthlyGrid.getRowConstraints().clear();
        for(int row = 0; row < rows; row++) {
            monthlyGrid.getRowConstraints().add(rowConstraints);
            for(int col = 0; col < 7; col++, calendarDate = calendarDate.plusDays(1)) {
                VBox vBox = new VBox();
            
                monthlyGrid.add(vBox, col, row);    // Add panes to calendar grid
                monthlyGrid.setHgrow(vBox, Priority.ALWAYS);
                monthlyGrid.setVgrow(vBox, Priority.ALWAYS);
                
                int day = calendarDate.getDayOfMonth();
                Label label = new Label(" " + String.valueOf(day));
                vBox.getChildren().add(label);

                /* Any tail end day labels of the previous month are given an alternative style. 
                   The first "Day 1" signals the main month, which toggles the label to the main 
                   style. If a second "Day 1" is encountered, toggle back. */
                if(day == 1) isMainStyle ^= true;
                if(isMainStyle == true) label.getStyleClass().add("main-calendar-label-style");
                else label.getStyleClass().add("alt-calendar-label-style");
                vBox.getStyleClass().add("calendar-date-pane-style");

                setMonthlyAppts(vBox, calendarDate, appointments);
            }
        }
        
        // Set weekday, month, and year display text
        setWeekdayHeaders(null);        
        calendarLabel.setText(lang.getString(String.valueOf(selectedDate.getMonth())) 
                             + " " + String.valueOf(selectedDate.getYear()));
    }
        
    private void setMonthlyAppts(VBox rootPane, LocalDate calendarDate, List<Appointment> appts) {        
        // Filter the list for appointments on this day
        final LocalDate date = calendarDate;
        List<Appointment> filteredAppts = appts.stream()
                .filter(a -> a.getStart().toLocalDate().equals(calendarDate)) // G: Answered line 264
                .collect(Collectors.toList());
        
        if(!filteredAppts.isEmpty()) {
            // Add scroll pane to calendar panes with appointments
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setMaxWidth(Double.MAX_VALUE);
            scrollPane.setFitToWidth(true);
            rootPane.getChildren().add(scrollPane);
            
            // Add vbox to scroll pane
            VBox vBox = new VBox();
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            scrollPane.setContent(vBox);
            
            // For each appointment, add to vbox
            filteredAppts.forEach((Appointment a) -> {    // G: Answered line 269
                LocalTime startLocalTime = a.getStart().toLocalTime();
                LocalTime endLocalTime = a.getEnd().toLocalTime();
                Label label = new Label(a.getTitle() + "\n"
                                      + startLocalTime + " - " 
                                      + endLocalTime);
                VBox labelHolder = new VBox(label);
                vBox.getChildren().add(labelHolder);
                labelHolder.getStyleClass().add("appointment-pane-style");
                label.getStyleClass().add("appointment-label-style");
//                label.setTextOverrun(OverrunStyle.CLIP);
                label.setUserData(a);
                
                
                // Double-click an appointment to edit it
                labelHolder.setOnMouseClicked(e -> {    // G: Answered on line 251
                    e.consume();
                    if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                        showApptInput(labelHolder, a, calendarDate, null, null);
                        System.out.println( "Appointment with date " + date + ", time " + startLocalTime 
                                          + "-" + endLocalTime + " clicked" );
                    }
                });
            });
        }
        // Double-click a pane to input an appointment with the selected date
        rootPane.setOnMouseClicked(e -> {       // G: Answered on line 251
            if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                showApptInput(rootPane, null, calendarDate, null, null);
                System.out.println("Pane with date " + date + " clicked");
            }
        });
    }
    
    /* Returns the ordinal number of the day of the week -- which depends on 
       which day the locale starts its weeks.
       https://www.unicode.org/cldr/charts/36/supplemental/territory_information.html */
    private static int getOrdinalDay(int day) {
        LocalDate date = selectedDate.withDayOfMonth(day);
        switch (firstDayOfWeekByLocale) {
            case MONDAY:
                return date.getDayOfWeek().ordinal();
            case SUNDAY:
                return (date.getDayOfWeek().ordinal() + 1) % 7;
            case SATURDAY:
                return (date.getDayOfWeek().ordinal() + 2) % 7;
            case FRIDAY:
                return (date.getDayOfWeek().ordinal() + 3) % 7;
            default:    // Monday, ISO-8601 standard
                return date.getDayOfWeek().ordinal();
        }
    }
    
    public void weekToggled(ActionEvent event) {
        calRootGrid.getChildren().remove(weeklyHolderScrollPane);
        calRootGrid.getChildren().remove(monthlyGrid);
        
        calRootGrid.add(weeklyHolderScrollPane, 0, 1);
        createWeekPage();

        headerHolder.setLeftAnchor(headerGrid, 40.0);
        headerHolder.setRightAnchor(headerGrid, 12.0);
    }
    
    public void monthToggled(ActionEvent event) {
        calRootGrid.getChildren().remove(weeklyHolderScrollPane);
        calRootGrid.getChildren().remove(monthlyGrid);
        
        calRootGrid.add(monthlyGrid, 0, 1);
        createMonthPage();

        headerHolder.setLeftAnchor(headerGrid, 0.0);
        headerHolder.setRightAnchor(headerGrid, 0.0);
    }
    
    public void prevDate(ActionEvent event) {
        if(monthButton.isSelected()) {
            changeDate(prevMonthDate);
            createMonthPage();
        }
        else {
            changeDate(prevWeekDate);
            createWeekPage();
        }
    }

    public void nextDate(ActionEvent event) {
        if(monthButton.isSelected()) {
            changeDate(nextMonthDate);
            createMonthPage();
        }
        else {
            changeDate(nextWeekDate);
            createWeekPage();
        }
    }
    
    private void changeDate(LocalDate date) {
        weeklyGrid.getChildren().clear();
        monthlyGrid.getChildren().clear();
        
        selectedDate = date;
        prevMonthDate = date.minusMonths(1);
        nextMonthDate = date.plusMonths(1);
        prevWeekDate = date.minusWeeks(1);
        nextWeekDate = date.plusWeeks(1);
    }

    private void showApptInput(Node node, Appointment appointment, LocalDate date, 
                               LocalTime timeStart, LocalTime timeEnd) {
        try {      
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/AppointmentInput.fxml"));
            loader.setResources(lang);
            
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(lang.getString("appointment"));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            AppointmentInputController controller = loader.getController();
            controller.setStage(stage, date, timeStart, timeEnd);
            if(appointment != null) {
                controller.setAppointment(appointment);
            }
            
            
            /* Appointment box placement */            
            // The dimensions of the stage can't be calculated until after
            // the stage is shown, so constants are used instead.
            final int APPT_BOX_X = 310;
            final int APPT_BOX_Y = 283;
            // Small offset for placement aesthetics
            final int OFFSET = 8;
            
            BorderPane programPane = (BorderPane)calRootGrid.getParent().getParent();
            Bounds programBounds = programPane.getBoundsInLocal();
            
            Bounds calBounds = calRootGrid.getBoundsInLocal();        
            
            Bounds dayPaneBounds = node.localToScreen(node.getBoundsInLocal());
            
            // Move Appointment box to the left of the selected day pane.
            double x = dayPaneBounds.getMinX() - APPT_BOX_X;
            // If extending left of the program screen, move to the right of the selected day pane.
            if(x < programPane.localToScreen(programBounds).getMinX()) {
                x = dayPaneBounds.getMaxX() + OFFSET;
            } else {x = x - OFFSET;}
            
            // Align Appointment box with the top of the selected day pane.
            double y = dayPaneBounds.getMinY();
            // If extending below the calendar, align the Appt box's bottom with the calendar's.
            double apptBoxBottom = y + APPT_BOX_Y;
            double calRootGridBottom = calRootGrid.localToScreen(calBounds).getMaxY();
            if(apptBoxBottom > calRootGridBottom) {
                y = y + calRootGridBottom - apptBoxBottom - OFFSET;
            } else {y = y + OFFSET;}
            
            stage.setX(x);
            stage.setY(y);
            
            stage.showAndWait();
            // If Appointment box stage dimensions change, get new ones with:
//             System.out.println("x:" + stage.getWidth() + ", y:" + stage.getHeight());
            
            // Date isn't "changing"; this refreshes the page
            if(monthButton.isSelected()) {
                changeDate(selectedDate);
                createMonthPage();
            }
            else {
                changeDate(selectedDate);
                createWeekPage();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}