#!/bin/sh
mvn clean package exec:java -Dexec.mainClass=com.gametracker.GameTrackerGUI
