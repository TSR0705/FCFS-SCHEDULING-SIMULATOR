package com.example.fcfs_scheduler;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FCFS_Scheduler extends Application {

    // ---------- PROCESS CLASS ----------
    static class Process {
        String name;
        int arrivalTime, burstTime;
        int remainingTime;
        int waitingTime = 0, turnaroundTime = 0, completionTime = 0;
        Color color;

        Process(String name, int arrivalTime, int burstTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            this.color = Color.color(Math.random() * 0.7 + 0.2,
                    Math.random() * 0.7 + 0.2,
                    Math.random() * 0.7 + 0.2);
        }
    }

    // ---------- GANTT BLOCK ----------
    static class GanttBlock {
        Process process; // null if idle
        double x, y, width, height;
        int cumulativeWT, cumulativeTAT, cumulativeCT; // store cumulative metrics for tooltip

        GanttBlock(Process process, double x, double y, double width, double height,
                   int cumulativeWT, int cumulativeTAT, int cumulativeCT) {
            this.process = process;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.cumulativeWT = cumulativeWT;
            this.cumulativeTAT = cumulativeTAT;
            this.cumulativeCT = cumulativeCT;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    // ---------- INSTANCE VARIABLES ----------
    private final double ganttBarWidth = 40;
    private final double ganttBarHeight = 50;
    private final double ganttStartX = 20;
    private final double ganttStartY = 50;
    private final double ganttRowSpacing = 70;

    private double ganttX = ganttStartX;
    private int ganttRow = 0;

    private final List<Process> processes = new ArrayList<>();
    private final List<GanttBlock> ganttBlocks = new ArrayList<>();
    private final TableView<Process> table = new TableView<>();
    private final TableView<Process> readyQueueTable = new TableView<>();
    private final Canvas canvas = new Canvas(900, 250);
    private Timeline timeline;
    private Queue<Process> readyQueue = new LinkedList<>();
    private int currentTime = 0;
    private int totalCPUTime = 0;
    private int totalIdleTime = 0;

    private Label avgWTLabel = createMetricLabel("Average WT", Color.LIGHTBLUE);
    private Label avgTATLabel = createMetricLabel("Average TAT", Color.LIGHTGREEN);
    private Label maxWTLabel = createMetricLabel("Max WT", Color.LIGHTPINK);
    private Label cpuLabel = createMetricLabel("CPU", Color.LIGHTCORAL);
    private Label cpuUtilLabel = createMetricLabel("CPU Utilization", Color.LIGHTGOLDENRODYELLOW);
    private Label idleTimeLabel = createMetricLabel("Total Idle", Color.LIGHTGRAY);

    private TextArea logArea = new TextArea();
    private LineChart<Number, Number> metricChart;
    private XYChart.Series<Number, Number> wtSeries;
    private XYChart.Series<Number, Number> tatSeries;

    private Button startBtn, pauseBtn, stepBtn, resetBtn;
    private Slider speedSlider = new Slider(0.1, 3, 1); // 0.1x to 3x speed
    private boolean isAutoPlay = false;

    private Tooltip tooltip = new Tooltip();

    // ---------- START ----------
    @Override
    public void start(Stage stage) {

        // --- INPUT FIELDS ---
        TextField nameField = new TextField();
        nameField.setPromptText("Process Name");
        TextField arrivalField = new TextField();
        arrivalField.setPromptText("Arrival Time");
        TextField burstField = new TextField();
        burstField.setPromptText("Burst Time");

        Button addBtn = new Button("Add Process");
        startBtn = new Button("Start");
        pauseBtn = new Button("Pause");
        stepBtn = new Button("Step");
        resetBtn = new Button("Reset");

        HBox inputBox = new HBox(10, nameField, arrivalField, burstField, addBtn, startBtn, pauseBtn, stepBtn, resetBtn);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);

        // --- TABLES ---
        setupProcessTable();
        setupReadyQueueTable();
        VBox tableBox = new VBox(10, new Label("Process Table:"), table, new Label("Ready Queue:"), readyQueueTable);
        tableBox.setPadding(new Insets(10));
        tableBox.setPrefWidth(450); // extended width

        // --- METRICS PANEL ---
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setBlockIncrement(0.1);
        VBox speedBox = new VBox(5, new Label("Simulation Speed:"), speedSlider);
        speedBox.setPadding(new Insets(10));

        VBox metricsPanel = new VBox(10, avgWTLabel, avgTATLabel, maxWTLabel, cpuLabel, cpuUtilLabel, idleTimeLabel, speedBox);
        metricsPanel.setPadding(new Insets(10));
        metricsPanel.setAlignment(Pos.TOP_CENTER);
        metricsPanel.setPrefWidth(220);

        // --- LOG AREA ---
        logArea.setPrefHeight(150);
        logArea.setEditable(false);

        // --- CHART ---
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Value");
        metricChart = new LineChart<>(xAxis, yAxis);
        metricChart.setTitle("WT & TAT Evolution");
        metricChart.setLegendVisible(true);
        metricChart.setCreateSymbols(false);
        wtSeries = new XYChart.Series<>();
        wtSeries.setName("Average WT");
        tatSeries = new XYChart.Series<>();
        tatSeries.setName("Average TAT");
        metricChart.getData().addAll(wtSeries, tatSeries);
        metricChart.setPrefHeight(200);

        // --- BUTTON ACTIONS ---
        addBtn.setOnAction(e -> {
            if (!nameField.getText().isEmpty() && !burstField.getText().isEmpty()) {
                String name = nameField.getText();
                int at = arrivalField.getText().isEmpty() ? 0 : Integer.parseInt(arrivalField.getText());
                int bt = Integer.parseInt(burstField.getText());
                processes.add(new Process(name, at, bt));
                table.getItems().setAll(processes);
                nameField.clear(); arrivalField.clear(); burstField.clear();
                log("Added process: " + name + " AT=" + at + " BT=" + bt);
            }
        });

        startBtn.setOnAction(e -> { isAutoPlay = true; runAutoPlay(); });
        pauseBtn.setOnAction(e -> { if (timeline != null) timeline.pause(); isAutoPlay = false; });
        stepBtn.setOnAction(e -> { isAutoPlay = false; advanceOneUnit(); });
        resetBtn.setOnAction(e -> resetSimulation());

        // --- LAYOUT ---
        BorderPane root = new BorderPane();
        root.setTop(inputBox);

        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setPadding(new Insets(10));
        canvasPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f8ff, #e6f0fa); -fx-border-color: #000000; -fx-border-width: 2;");
        canvasPane.setPrefWidth(750);

        HBox centerBox = new HBox(10, tableBox, canvasPane);
        centerBox.setPadding(new Insets(10));
        centerBox.setAlignment(Pos.TOP_LEFT);
        root.setCenter(centerBox);

        root.setRight(metricsPanel);

        SplitPane bottomPane = new SplitPane();
        bottomPane.setOrientation(Orientation.HORIZONTAL);
        bottomPane.getItems().addAll(new VBox(new Label("Event Log:"), logArea), metricChart);
        bottomPane.setDividerPositions(0.5);
        bottomPane.setPrefHeight(250);
        root.setBottom(bottomPane);

        Scene scene = new Scene(root, 1300, 850);
        stage.setScene(scene);
        stage.setTitle("FCFS Professional Simulation");
        stage.show();

        // --- TOOLTIP FOR GANTT ---
        tooltip.setAutoHide(true);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::showTooltip);
    }

    // ---------- TOOLTIP ----------
    private void showTooltip(MouseEvent e) {
        double mouseX = e.getX();
        double mouseY = e.getY();
        tooltip.hide();

        for (GanttBlock block : ganttBlocks) {
            if (block.contains(mouseX, mouseY)) {
                if (block.process != null) {
                    tooltip.setText(
                            block.process.name +
                                    "\nAT: " + block.process.arrivalTime +
                                    " BT: " + block.process.burstTime +
                                    "\nWT: " + block.cumulativeWT +
                                    " TAT: " + block.cumulativeTAT +
                                    " CT: " + block.cumulativeCT
                    );
                } else {
                    tooltip.setText("CPU Idle");
                }
                Tooltip.install(canvas, tooltip);
                return;
            }
        }
        tooltip.hide();
    }

    // ---------- UTILITY METHODS ----------
    private Label createMetricLabel(String text, Color color) {
        Label label = new Label(text + ": 0");
        label.setPrefWidth(180);
        label.setFont(Font.font(14));
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: " + toRgbString(color) + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5;");
        label.setEffect(new DropShadow());
        return label;
    }

    private String toRgbString(Color c) {
        return String.format("rgb(%d,%d,%d)", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private void setupProcessTable() {
        TableColumn<Process, String> nameCol = new TableColumn<>("Process");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        TableColumn<Process, String> atCol = new TableColumn<>("AT");
        atCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().arrivalTime)));
        TableColumn<Process, String> btCol = new TableColumn<>("BT");
        btCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().burstTime)));
        TableColumn<Process, String> wtCol = new TableColumn<>("WT");
        wtCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().waitingTime)));
        TableColumn<Process, String> tatCol = new TableColumn<>("TAT");
        tatCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().turnaroundTime)));
        table.getColumns().addAll(nameCol, atCol, btCol, wtCol, tatCol);
        table.setPrefHeight(300);
    }

    private void setupReadyQueueTable() {
        TableColumn<Process, String> nameCol = new TableColumn<>("Process");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        TableColumn<Process, String> remainingCol = new TableColumn<>("Remaining BT");
        remainingCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().remainingTime)));
        readyQueueTable.getColumns().addAll(nameCol, remainingCol);
        readyQueueTable.setPrefHeight(150);
    }

    // ---------- STEP / AUTO METHODS ----------
    private void advanceOneUnit() {
        boolean cpuIdle = true;

        // --- ADD ARRIVING PROCESSES ---
        for (Process p : processes) {
            if (p.arrivalTime == currentTime) {
                readyQueue.add(p);
                log("Time " + currentTime + ": " + p.name + " arrived.");
            }
        }

        Process running = readyQueue.peek();
        if (running != null) {
            cpuLabel.setText("CPU: " + running.name);
            cpuIdle = false;
            running.remainingTime--;
            totalCPUTime++;
            drawGantt(running);
            if (running.remainingTime <= 0) {
                running.completionTime = currentTime + 1;
                running.turnaroundTime = running.completionTime - running.arrivalTime;
                running.waitingTime = running.turnaroundTime - running.burstTime;
                readyQueue.poll();
                log("Time " + (currentTime+1) + ": " + running.name + " completed.");
            }
        } else {
            cpuLabel.setText("CPU: Idle");
            totalIdleTime++;
            drawGantt(null);
        }

        currentTime++;
        table.refresh();
        readyQueueTable.getItems().setAll(readyQueue);
        updateMetrics();
        updateCPUUtil();
        updateChart();
    }

    private void runAutoPlay() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame kf = new KeyFrame(Duration.seconds(1), e -> {
            if (!processes.isEmpty() && currentTime <= maxSimulationTime()) advanceOneUnit();
            else timeline.stop();
        });

        timeline.getKeyFrames().add(kf);
        timeline.setRate(speedSlider.getValue());
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> timeline.setRate(newV.doubleValue()));
        timeline.play();
    }

    private void drawGantt(Process running) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double yPos = ganttStartY + ganttRow * ganttRowSpacing;

        // Wrap to next row
        if (ganttX + ganttBarWidth > canvas.getWidth() - 20) {
            ganttRow++;
            ganttX = ganttStartX;
            yPos = ganttStartY + ganttRow * ganttRowSpacing;
        }

        // Extend canvas height
        if (yPos + ganttBarHeight > canvas.getHeight() - 20) {
            canvas.setHeight(yPos + ganttBarHeight + 20);
        }

        if (running != null) {
            LinearGradient gradient = new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                    new Stop(0,running.color.brighter()), new Stop(1,running.color.darker()));
            gc.setFill(gradient);
            gc.fillRect(ganttX, yPos, ganttBarWidth, ganttBarHeight);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(ganttX, yPos, ganttBarWidth, ganttBarHeight);
            gc.setFill(Color.BLACK);
            gc.fillText(running.name, ganttX + 5, yPos + ganttBarHeight / 2 + 5);

            ganttBlocks.add(new GanttBlock(running, ganttX, yPos, ganttBarWidth, ganttBarHeight,
                    running.waitingTime, running.turnaroundTime, running.completionTime));
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(ganttX, yPos, ganttBarWidth, ganttBarHeight);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(ganttX, yPos, ganttBarWidth, ganttBarHeight);
            gc.setFill(Color.BLACK);
            gc.fillText("Idle", ganttX + 5, yPos + ganttBarHeight / 2 + 5);

            ganttBlocks.add(new GanttBlock(null, ganttX, yPos, ganttBarWidth, ganttBarHeight,
                    0,0,0));
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        gc.fillText(String.valueOf(currentTime), ganttX + 5, yPos - 5);

        ganttX += ganttBarWidth; // only increment after executed block
    }

    private void updateCPUUtil() {
        double util = (currentTime == 0) ? 0 : ((double)totalCPUTime / currentTime) * 100;
        cpuUtilLabel.setText(String.format("CPU Utilization: %.2f%%", util));
        idleTimeLabel.setText("Total Idle: " + totalIdleTime);
    }

    private void updateMetrics() {
        double avgWT = processes.stream().mapToInt(p -> p.waitingTime).average().orElse(0);
        double avgTAT = processes.stream().mapToInt(p -> p.turnaroundTime).average().orElse(0);
        int maxWT = processes.stream().mapToInt(p -> p.waitingTime).max().orElse(0);

        avgWTLabel.setText(String.format("Average WT: %.2f", avgWT));
        avgTATLabel.setText(String.format("Average TAT: %.2f", avgTAT));
        maxWTLabel.setText("Max WT: " + maxWT);
    }

    private void updateChart() {
        wtSeries.getData().add(new XYChart.Data<>(currentTime, processes.stream().mapToInt(p -> p.waitingTime).average().orElse(0)));
        tatSeries.getData().add(new XYChart.Data<>(currentTime, processes.stream().mapToInt(p -> p.turnaroundTime).average().orElse(0)));
    }

    private void resetSimulation() {
        if (timeline != null) timeline.stop();
        currentTime = 0;
        ganttX = ganttStartX;
        ganttRow = 0;
        totalCPUTime = 0;
        totalIdleTime = 0;
        ganttBlocks.clear();
        readyQueue.clear();
        table.refresh();
        readyQueueTable.getItems().clear();
        avgWTLabel.setText("Average WT: 0");
        avgTATLabel.setText("Average TAT: 0");
        maxWTLabel.setText("Max WT: 0");

        cpuLabel.setText("CPU: -");
        cpuUtilLabel.setText("CPU Utilization: 0%");
        idleTimeLabel.setText("Total Idle: 0");
        wtSeries.getData().clear();
        tatSeries.getData().clear();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,canvas.getWidth(), canvas.getHeight());
        logArea.clear();
    }

    private int maxSimulationTime() {
        return processes.stream().mapToInt(p -> p.arrivalTime + p.burstTime).sum() + 10;
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
    }

    public static void main(String[] args) {
        launch();
    }
}
