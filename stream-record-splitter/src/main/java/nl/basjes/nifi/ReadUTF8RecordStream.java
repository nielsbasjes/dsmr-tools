package nl.basjes.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadUTF8RecordStream implements Serializable {

    private Pattern endMatcher;
    private InputStream inputStream;

    public ReadUTF8RecordStream(String recordEndRegex, InputStream input) {
        endMatcher = Pattern.compile("(" + recordEndRegex + ")");

        inputStream = input;
    }

    private String previousLastRecord = "";

    // Returns null if end of stream
    public String read() throws IOException {
        byte[] readBuffer = new byte[1024];
        if (previousLastRecord == null) {
            return null;
        }

        // In case the previous read retrieved multiple records
        String[] splits = endMatcher.split(previousLastRecord, 2);
        if (splits.length == 2) {
            Matcher matcher = endMatcher.matcher(previousLastRecord);
            if (matcher.find()) {
                String keepThis = matcher.group(1);
                previousLastRecord = splits[1];
                return splits[0] + keepThis;
            }
            // FIXME: What do we do if this happens? This should not be possible.
            previousLastRecord = splits[1];
            return splits[0];
        }

        // Keep reading until we have atleast one record in the buffer (sometimes we get multiple records)
        while (true) {
            int bytesRead = inputStream.read(readBuffer);
            if (bytesRead == -1) { // -1 == End of stream
                String returnValue = previousLastRecord;
                previousLastRecord = null; // Next call will return null immediately
                return returnValue;
            }

            previousLastRecord += new String(readBuffer, 0, bytesRead, UTF_8);

            // In case we now have (one or more) records return the first one.
            splits = endMatcher.split(previousLastRecord, 2);
            if (splits.length == 2) {
                Matcher matcher = endMatcher.matcher(previousLastRecord);
                if (matcher.find()) {
                    String keepThis = matcher.group(1);
                    previousLastRecord = splits[1];
                    return splits[0] + keepThis;
                }
                // FIXME: What do we do if this happens? This should not be possible.
                previousLastRecord = splits[1];
                return splits[0];
            }
        }
    }
}
