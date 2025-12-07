package com.example.ferm.service;

import com.example.ferm.config.TimerProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class LogbookService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final TimerProperties properties;

    public LogbookService(TimerProperties properties) {
        this.properties = properties;
    }

    public synchronized String createOrGetLogFile(Long fermId) {
        String fileName = "ferm-" + fermId + "-" + DATE.format(Instant.now()) + ".log";
        Path directory = properties.getLogDirectory();
        Path file = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            if (Files.notExists(file)) {
                Files.createFile(file);
                Files.writeString(file, headerLine(fermId));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare log file", e);
        }
        return fileName;
    }

    public void append(String logFile, String message) {
        Path file = properties.getLogDirectory().resolve(logFile);
        String line = TS.format(Instant.now()) + " | " + message + System.lineSeparator();
        try {
            Files.writeString(file, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to append log entry", e);
        }
    }

    private String headerLine(Long fermId) {
        return "=== Log for Ferm " + fermId + " started on " + TS.format(Instant.now()) + " ===" + System.lineSeparator();
    }
}
