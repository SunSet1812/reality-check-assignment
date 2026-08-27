package com.comeon.assignment.realitycheck.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeFormatterUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yy HH:mm");

    private DateTimeFormatterUtil() {
    }

    public static String format(Instant timestamp, String timezone) {
        if (timestamp == null) {
            return null;
        }

        return FORMATTER
                .withZone(ZoneId.of(timezone))
                .format(timestamp);
    }
}