package com.example.ferm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ferm")
public class TimerProperties {

    private long mainDurationHours = 50;
    private int warningMinutesLeft = 5;
    private int breachMinutesAfter = 15;
    private List<Integer> subTimers = new ArrayList<>();
    private String logDirectory = "logs";

    public Duration mainDuration() {
        return Duration.ofHours(mainDurationHours);
    }

    public Duration warningDuration() {
        return Duration.ofMinutes(warningMinutesLeft);
    }

    public Duration breachDuration() {
        return Duration.ofMinutes(breachMinutesAfter);
    }

    public List<Integer> getSubTimers() {
        return subTimers;
    }

    public void setSubTimers(List<Integer> subTimers) {
        this.subTimers = subTimers;
    }

    public long getMainDurationHours() {
        return mainDurationHours;
    }

    public void setMainDurationHours(long mainDurationHours) {
        this.mainDurationHours = mainDurationHours;
    }

    public int getWarningMinutesLeft() {
        return warningMinutesLeft;
    }

    public void setWarningMinutesLeft(int warningMinutesLeft) {
        this.warningMinutesLeft = warningMinutesLeft;
    }

    public int getBreachMinutesAfter() {
        return breachMinutesAfter;
    }

    public void setBreachMinutesAfter(int breachMinutesAfter) {
        this.breachMinutesAfter = breachMinutesAfter;
    }

    public Path getLogDirectory() {
        return Path.of(logDirectory);
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }
}
