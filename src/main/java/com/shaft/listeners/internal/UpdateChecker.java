package com.shaft.listeners.internal;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.tools.io.internal.ReportManagerHelper;
import io.restassured.RestAssured;
import org.apache.logging.log4j.Level;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateChecker {

    public static void check() {
        
        boolean isLoggingDisabled = SHAFT.Properties.reporting.disableLogging();
        ReportManager.logDiscrete("Checking for engine updates...");
        SHAFT.Properties.reporting.set().disableLogging(true);
        
        String lastCheckedFile = "engine_check";
        String todayDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        boolean doesEngineCheckFileExists = SHAFT.CLI.file().doesFileExist("target/", lastCheckedFile,1);
        boolean doesFileContainTodayDate = SHAFT.CLI.file().readFile("target/",lastCheckedFile).equals(todayDate);

        if (!doesEngineCheckFileExists || !doesFileContainTodayDate) {

            String logMessage = "";
            try {
               
                String latestVersion = RestAssured.given().baseUri("https://api.github.com/").and().basePath("repos/ShaftHQ/SHAFT_ENGINE/releases/")
                        .when().get("latest")
                        .thenReturn().body().jsonPath().getString("name");
                String currentVersion = SHAFT.Properties.internal.shaftEngineVersion();
                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    logMessage = "⚠\uFE0F You're using an outdated engine version \"" + currentVersion + "\" ⚠\uFE0F\nKindly upgrade to the latest one \"" + latestVersion + "\" to ensure the best experience.\nFor more information click here: https://github.com/ShaftHQ/SHAFT_ENGINE/releases/latest .";
                } else {
                    logMessage = "You're using the latest engine version \"" + latestVersion + "\". \uD83D\uDC4D";
                }
            } catch (Throwable throwable) {
                logMessage = "Failed to check for updates... proceeding with engine setup...";
            }
            SHAFT.Properties.reporting.set().disableLogging(isLoggingDisabled);
            if (logMessage.contains("⚠\uFE0F")) {
                ReportManagerHelper.logDiscrete(logMessage, Level.WARN);
            } else {
                ReportManager.logDiscrete(logMessage);
            }
            SHAFT.CLI.file().writeToFile("target/", lastCheckedFile, todayDate);
        }
        else {
            SHAFT.Properties.reporting.set().disableLogging(isLoggingDisabled);
            ReportManager.logDiscrete("Engine Update check done for the day. \uD83D\uDC4D");
        }
    }

}
