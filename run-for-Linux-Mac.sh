#!/bin/sh
echo "🔨 Building and running application..."
mvn clean package exec:java -Dexec.mainClass=com.gametracker.GameTrackerGUI
