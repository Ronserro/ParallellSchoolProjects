package View;

import Model.*;
import Model.Observer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.time.YearMonth;
import java.util.*;

public class Schema extends AnchorPane implements Observer {
    @FXML Button next, previous, createWorkshift;
    @FXML GridPane monthGrid, weekGrid;
    @FXML AnchorPane dayView, monthView, weekView, workshiftPane;
    @FXML ComboBox<String> viewSelector;
    @FXML Label currentFormatInfo;
    @FXML ListView listOfWorkshifts;

    private int dateIndex;
    private Date currentIndex;
    private String mode = "";

    public Schema() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Schema.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        generateDate();
        generateComboBox();
        generateLabels();
        generateButtons();
    }

    private void generateLabels(){
        switch (mode){
            case "Dag":
                currentFormatInfo.setText(currentIndex.toString());
                break;
            case "Vecka":
                Calendar.getInstance().setTime(currentIndex);
                currentFormatInfo.setText("Vecka " + Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
                break;
            case "Månad":
                currentFormatInfo.setText(currentIndex.getYear() + "/" + currentIndex.getMonth());
        }
    }

    private void generateDate(){
        currentIndex = new Date(OurCalendar.getInstance().getWorkday(0).DATE);
        dateIndex = 0;
    }

    private void generateComboBox(){
        viewSelector.getItems().clear();
        viewSelector.getItems().add("Månad");
        viewSelector.getItems().add("Vecka");
        viewSelector.getItems().add("Dag");
        viewSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                mode = newValue;
                switch (newValue) {
                    case "Månad":
                        monthView.toFront();
                        monthView.setVisible(true);
                        dayView.setVisible(false);
                        weekView.setVisible(false);
                        workshiftPane.setVisible(false);
                        updateMonth();
                        break;
                    case "Vecka":
                        weekView.toFront();
                        weekView.setVisible(true);
                        dayView.setVisible(false);
                        monthView.setVisible(false);
                        workshiftPane.setVisible(false);
                        updateWeek();
                        break;
                    case "Dag":
                        dayView.toFront();
                        dayView.setVisible(true);
                        weekView.setVisible(false);
                        monthView.setVisible(false);
                        workshiftPane.setVisible(false);
                        updateDay();
                        break;
                }
            }
        });
    }

    public void next(){
        switch (mode){
            case "Dag":
                this.dateIndex++;
                updateDay();
                break;
            case "Vecka":
                this.dateIndex+=7;
                updateWeek();
                break;
            case "Månad":
                this.dateIndex+= YearMonth.of(new Date(OurCalendar.getInstance().getWorkday(dateIndex).DATE).getYear(), new Date(OurCalendar.getInstance().getWorkday(dateIndex).DATE).getMonth()).lengthOfMonth();
                updateMonth();
                break;
        }
    }
    public void previous(){
        switch (mode){
            case "Dag":
                this.dateIndex--;
                updateDay();
                break;
            case "Vecka":
                this.dateIndex-=7;
                updateWeek();
                break;
            case "Månad":
                this.dateIndex-= YearMonth.of(new Date(OurCalendar.getInstance().getWorkday(dateIndex).DATE).getYear(), new Date(OurCalendar.getInstance().getWorkday(dateIndex).DATE).getMonth()).lengthOfMonth();
                updateMonth();
                break;
        }
    }

    private void updateMonth(){
        currentIndex = new Date(OurCalendar.getInstance().getWorkday(dateIndex).DATE);
        int daysInMonth = YearMonth.of(currentIndex.getYear(), currentIndex.getMonth()).lengthOfMonth();
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(currentIndex);
        int startingDay;
        switch (tmp.get(Calendar.DAY_OF_WEEK)){
            case 1:
                startingDay = 6;
                break;
            case 2:
                startingDay = 0;
                break;
            case 3:
                startingDay = 1;
                break;
            case 4:
                startingDay = 2;
                break;
            case 5:
                startingDay = 3;
                break;
            case 6:
                startingDay = 4;
                break;
            case 7:
                startingDay = 5;
                break;
            default: startingDay = 0;
        }
        OurCalendar tmpCalendar = OurCalendar.getInstance();
        int anotherIndex = 0, thirdIndex = 0;
        for (int i=startingDay; i<daysInMonth+startingDay; i++){
            if (i%7 == 0)
                thirdIndex++;
            monthGrid.add(new DayScheduleView(OurCalendar.getInstance().getWorkday(dateIndex+anotherIndex)), i%7, thirdIndex);
            anotherIndex++;
        }
    }
    private void updateWeek(){
        int index = OurCalendar.getInstance().getWorkday(dateIndex).getDayOfWeekOffset();
        int secondIndex = 0;
        for (int i = -index; i<=7-index; i++){
            weekGrid.add(new DayScheduleView(OurCalendar.getInstance().getWorkday(i+dateIndex)), 0, secondIndex);
        }
    }
    private void updateDay(){
        OurCalendar.getInstance().getWorkday(dateIndex).setWorkDay();
        listOfWorkshifts.getItems().clear();
        for (Department d : Admin.getInstance().getDepartments()){
            for (WorkShift w : OurCalendar.getInstance().getWorkday(dateIndex).getWorkShifts(d))
                listOfWorkshifts.getItems().add(new SchemaWorkshift(w, d.getColor(), d.getName()));
        }
    }

    private void generateButtons(){
        next.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                next();
            }
        });
        previous.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                previous();
            }
        });
        createWorkshift.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                workshiftPane = new CreateShiftView();
                workshiftPane.setVisible(true);
                workshiftPane.toFront();
            }
        });
    }

    private void sortEmployeesAlphabetically(List<Employee> employees){
        employees.sort(Comparator.comparing(Employee::getName));
    }

    private void generateMonth(){

    }

    private void generateWeek(){

    }

    private void generateDay(){

    }
    @Override
    public void update() {

    }
}