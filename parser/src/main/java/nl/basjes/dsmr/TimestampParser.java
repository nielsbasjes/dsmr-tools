package nl.basjes.dsmr;

import java.time.*;
import java.time.format.DateTimeFormatterBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampParser {
    // YYMMDDhhmmssX ASCII presentation of Time stamp
    // Year, Month, Day, Hour, Minute, Second, and an indication whether
    // DST is active (X=S) or DST is not active (X=W).

    // IMPORTANT ASSUMPTION:
    // This is for the DSMR = Dutch Smart Meter Requirements.
    // Dutch ! means Netherlands means timezone "Europe/Amsterdam"

    public ZonedDateTime parse(String dsmrTimestamp) {
        // Format       Y    Y     M   M     D    D     h    h     m    m     s    s      S or W
        String timeFormat= "([0-9][0-9])([01][0-9])([0-3][0-9])([0-2][0-9])([0-5][0-9])([0-5][0-9])([SW])";

        Pattern dateTimePattern = Pattern.compile(timeFormat);

        Matcher matcher = dateTimePattern.matcher(dsmrTimestamp);

        if (!matcher.find()) {
            return null;
        }

        Instant baseInstant = Instant.ofEpochSecond(0);
        ZonedDateTime zonedDateTime = ZonedDateTime
            .ofInstant(baseInstant, ZoneOffset.UTC)
            .withYear(2000 +  Integer.parseInt(matcher.group(1)) )
            .withMonth(       Integer.parseInt(matcher.group(2)) )
            .withDayOfMonth(  Integer.parseInt(matcher.group(3)) )
            .withHour(        Integer.parseInt(matcher.group(4)) )
            .withMinute(      Integer.parseInt(matcher.group(5)) )
            .withSecond(      Integer.parseInt(matcher.group(6)) )
//            .withZoneSameLocal(ZoneId.of("Europe/Amsterdam"))
            ;

        if (matcher.group(7).equalsIgnoreCase("S")) {
            zonedDateTime = zonedDateTime.withZoneSameLocal(ZoneOffset.of("+02:00"));
        }
        if (matcher.group(7).equalsIgnoreCase("W")) {
            zonedDateTime = zonedDateTime.withZoneSameLocal(ZoneOffset.of("+01:00"));
        }

        return zonedDateTime;
    }

}
