# FCFS Scheduler Simulator

A professional JavaFX-based visualization tool for simulating First-Come, First-Served (FCFS) CPU scheduling algorithms with real-time metrics and interactive Gantt chart display.

[![GitHub](https://img.shields.io/github/license/TSR0705/FCFS-SCHEDULING-SIMULATOR)](https://github.com/TSR0705/FCFS-SCHEDULING-SIMULATOR)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![Platform](https://img.shields.io/badge/Platform-JavaFX-green)](https://openjfx.io/)

## 🚀 Key Features

- **Interactive Process Management**: Add custom processes with names, arrival times, and burst times
- **Real-time Simulation**: Step through the simulation manually or run it automatically
- **Dynamic Gantt Chart Visualization**: Color-coded Gantt chart showing process execution and idle periods
- **Comprehensive Metrics Display**: Real-time calculation of waiting time, turnaround time, and CPU utilization
- **Performance Analytics**: Line charts showing the evolution of average waiting time and turnaround time
- **Detailed Logging**: Event log tracking process arrivals, executions, and completions
- **Adjustable Simulation Speed**: Control simulation speed with a slider from 0.1x to 3x
- **Process Tables**: View process details and ready queue status
- **Tooltips**: Hover over Gantt blocks for detailed process information

## 📋 Overview

This application provides an interactive simulation of the First-Come, First-Served (FCFS) CPU scheduling algorithm. Users can add processes with customizable arrival times and burst times, then visualize how the CPU scheduler handles these processes in real-time. The simulator displays a dynamic Gantt chart, process tables, performance metrics, and evolution charts to help understand the behavior of the FCFS scheduling algorithm.

## ⚙️ Technical Components

### JavaFX UI Elements

- **Main Window**: 1300x850 pixel resizable window with organized layout
- **Input Section**: Text fields for process name, arrival time, and burst time with Add button
- **Control Buttons**: Start, Pause, Step, and Reset controls for simulation management
- **Process Tables**: Dual table view showing all processes and ready queue status
- **Gantt Chart Canvas**: Dynamic 900x250 pixel canvas that auto-expands for visualization
- **Metrics Panel**: Color-coded labels displaying key performance indicators
- **Speed Control**: Slider for adjusting simulation speed from 0.1x to 3x
- **Event Log**: Scrollable text area showing simulation events
- **Analytics Charts**: Line chart showing WT and TAT evolution over time

### Core Logic Components

#### Process Class
```java
static class Process {
    String name;
    int arrivalTime, burstTime;
    int remainingTime;
    int waitingTime = 0, turnaroundTime = 0, completionTime = 0;
    Color color; // Randomly assigned color for visualization
}
```

#### Gantt Block Class
```java
static class GanttBlock {
    Process process; // null if idle
    double x, y, width, height;
    int cumulativeWT, cumulativeTAT, cumulativeCT; // store cumulative metrics for tooltip
}
```

#### Scheduling Algorithm
- Implements First-Come, First-Served (FCFS) scheduling
- Uses a FIFO queue for process management
- Calculates waiting time, turnaround time, and completion time for each process
- Tracks CPU utilization and idle time

#### Visualization Engine
- Dynamic Gantt chart rendering with color gradients
- Auto-expanding canvas for long simulations
- Interactive tooltips with detailed process information
- Real-time metrics updates during simulation

#### Metrics Calculation
- Average Waiting Time (WT)
- Average Turnaround Time (TAT)
- Maximum Waiting Time
- CPU Utilization percentage
- Total Idle Time tracking

## ▶️ Simulation Engine Workflow

1. **Process Initialization**
   - Users add processes via input fields
   - Each process is assigned a random color for visualization
   - Processes are stored in the main process list

2. **Simulation Execution**
   - At each time unit, check for arriving processes
   - Add arriving processes to the ready queue
   - Execute the first process in the queue (FIFO)
   - Update process remaining time and metrics
   - Remove completed processes from the queue

3. **Visualization Updates**
   - Draw Gantt block for current time unit (process or idle)
   - Update process tables with latest metrics
   - Refresh performance metrics display
   - Update analytics charts with current averages

4. **Completion Handling**
   - Calculate final metrics when processes complete
   - Log completion events
   - Update all UI components with final values

## 🛠️ Installation & Running

### Prerequisites
- Java Development Kit (JDK) 21 ([Download Adoptium](https://adoptium.net/))
- Apache Maven 3.6 or higher ([Download Maven](https://maven.apache.org/download.cgi))

### Quick Start with Git

To quickly get started with the project, follow these Git commands:

```bash
# Create a new README.md file
echo "# FCFS-SCHEDULING-SIMULATOR" >> README.md

# Initialize a Git repository
git init

# Add README.md to staging area
git add README.md

# Make the first commit
git commit -m "first commit"

# Rename the default branch to main
git branch -M main

# Add the remote origin
git remote add origin https://github.com/TSR0705/FCFS-SCHEDULING-SIMULATOR.git

# Push to the remote repository
git push -u origin main
```

### Full Project Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/TSR0705/FCFS-SCHEDULING-SIMULATOR.git
   cd FCFS-SCHEDULING-SIMULATOR
   ```

2. **Verify Prerequisites**
   ```bash
   java -version
   mvn -version
   ```

3. **Compile the Project**
   ```bash
   mvn clean compile
   ```

4. **Run the Application**
   ```bash
   mvn javafx:run
   ```

### Alternative Execution Methods

1. **Using the Launcher Class**
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.fcfs_scheduler.Launcher"
   ```

2. **Building a JAR Package**
   ```bash
   mvn clean package
   java -jar target/FCFS_Scheduler-1.0-SNAPSHOT.jar
   ```

### Troubleshooting

- **JAVA_HOME not set**: Ensure JAVA_HOME environment variable points to your JDK 21 installation
- **JavaFX modules not found**: Verify JavaFX dependencies in pom.xml
- **Maven not found**: Ensure Maven is installed and added to your PATH

## 📁 Project Structure

```
FCFS_Scheduler/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/fcfs_scheduler/
│       │       ├── FCFS_Scheduler.java    # Main application class
│       │       ├── HelloController.java   # FXML controller (unused)
│       │       └── Launcher.java          # Application launcher
│       │   └── module-info.java           # Java module descriptor
│       └── resources/
├── pom.xml                                # Maven configuration
└── README.md                              # This file
```

## 🎮 Demo Workflow

1. **Adding Processes**
   - Enter process name (e.g., "P1")
   - Enter arrival time (e.g., "0") or leave blank for immediate arrival
   - Enter burst time (e.g., "5")
   - Click "Add Process"
   - Repeat for additional processes

2. **Running Simulation**
   - Use "Step" to advance one time unit at a time
   - Use "Start" for automatic execution
   - Adjust speed with the slider during automatic execution
   - Use "Pause" to temporarily stop automatic execution

3. **Monitoring Results**
   - Watch the Gantt chart update in real-time
   - Observe metrics changing as processes execute
   - Check the event log for detailed process information
   - Analyze the WT/TAT evolution chart for performance trends

4. **Resetting Simulation**
   - Click "Reset" to clear all processes and start over
   - All metrics and visualizations will be cleared

## 📸 Screenshots

<div style="display: flex; justify-content: space-around; flex-wrap: wrap;">
  <img src="screenshots/SIMULATOR-DASHBOARD.png" alt="Simulator Dashboard" width="45%" />
  <img src="screenshots/DEMO.png" alt="Simulation Demo" width="45%" />
</div>

## ▶️ Demo Video

<a href="https://youtu.be/Ppm3BWCN66A?si=EezB81bGeXrtdo_Q">
  <img src="https://img.youtube.com/vi/Ppm3BWCN66A/hqdefault.jpg" alt="FCFS Scheduler Demo" width="100%" />
</a>

[Watch the full demo on YouTube](https://youtu.be/Ppm3BWCN66A?si=EezB81bGeXrtdo_Q)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.