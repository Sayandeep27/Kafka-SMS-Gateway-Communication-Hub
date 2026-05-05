package com.icici.timebasedmessageservice.service;

import com.icici.timebasedmessageservice.dto.TimeBasedMessageRecord;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;

@Component
public class TimeWindowEvaluator {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HHmm");

    public boolean isEligible(TimeBasedMessageRecord record, LocalDateTime now) {
        return isWithinSendWindow(record, now) && isOutsideNoDeliveryWindow(record, now.toLocalTime());
    }

    boolean isWithinSendWindow(TimeBasedMessageRecord record, LocalDateTime now) {
        LocalDateTime from = record.getMsgSendFromDttime();
        LocalDateTime to = record.getMsgSendToDttime();

        if (from == null && to == null) {
            return true;
        }
        if (from == null) {
            return !now.isAfter(to);
        }
        if (to == null) {
            return !now.isBefore(from);
        }
        return !now.isBefore(from) && !now.isAfter(to);
    }

    boolean isOutsideNoDeliveryWindow(TimeBasedMessageRecord record, LocalTime currentTime) {
        LocalTime from = parseTime(record.getNoDelvryFromTime());
        LocalTime to = parseTime(record.getNoDelvryToTime());

        if (from == null || to == null) {
            return true;
        }

        boolean insideBlockedWindow;
        if (from.equals(to)) {
            insideBlockedWindow = false;
        } else if (from.isBefore(to)) {
            insideBlockedWindow = !currentTime.isBefore(from) && !currentTime.isAfter(to);
        } else {
            insideBlockedWindow = !currentTime.isBefore(from) || !currentTime.isAfter(to);
        }

        return !insideBlockedWindow;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() < 4) {
            normalized = "0".repeat(4 - normalized.length()) + normalized;
        }
        try {
            return LocalTime.parse(normalized, HHMM);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
