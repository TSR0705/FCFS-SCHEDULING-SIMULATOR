module com.example.fcfs_scheduler {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fcfs_scheduler to javafx.fxml;
    exports com.example.fcfs_scheduler;
}