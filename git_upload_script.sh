#!/bin/bash

# Initialize git repository
git init
git branch -M main

# Add remote origin (replace with your actual repository URL)
git remote add origin https://github.com/TSR0705/FCFS-SCHEDULING-SIMULATOR.git

# Commit 1: Project configuration file
git add pom.xml
git commit -m "Add Maven project configuration with JavaFX dependencies"
git push origin main

# Commit 2: Git ignore file
git add .gitignore
git commit -m "Add .gitignore to exclude build artifacts and IDE files"
git push origin main

# Commit 3: Main application module
git add src/main/java/module-info.java
git commit -m "Add Java module descriptor with JavaFX dependencies"
git push origin main

# Commit 4: Main scheduler application
git add src/main/java/com/example/fcfs_scheduler/FCFS_Scheduler.java
git commit -m "Add FCFS Scheduler main application with UI and simulation logic"
git push origin main

# Commit 5: FXML controller
git add src/main/java/com/example/fcfs_scheduler/HelloController.java
git commit -m "Add FXML controller for UI interactions"
git push origin main

# Commit 6: Application launcher
git add src/main/java/com/example/fcfs_scheduler/Launcher.java
git commit -m "Add application launcher class"
git push origin main

# Commit 7: FXML view file
git add src/main/resources/com/example/fcfs_scheduler/hello-view.fxml
git commit -m "Add FXML view definition for main UI"
git push origin main

# Commit 8: Maven wrapper scripts
git add mvnw mvnw.cmd
git commit -m "Add Maven wrapper scripts for consistent builds"
git push origin main

# Commit 9: README documentation
git add README.md
git commit -m "Add project documentation and usage instructions"
git push origin main

echo "All files have been individually committed and pushed to the repository."