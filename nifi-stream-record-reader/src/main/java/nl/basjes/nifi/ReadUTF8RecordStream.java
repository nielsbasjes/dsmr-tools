package nl.basjes.nifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadUTF8RecordStream implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ReadUTF8RecordStream.class);
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

        String[] splits = endMatcher.split(previousLastRecord, 2);
        if (splits.length == 2) {
            Matcher matcher = endMatcher.matcher(previousLastRecord);
            if (matcher.find()) {
                String keepThis = matcher.group(1);
                previousLastRecord = splits[1];
                return splits[0] + keepThis;
            }
            // What do we do if this happens?
            previousLastRecord = splits[1];
            return splits[0];
        }

        while (true) {
            int bytesRead = inputStream.read(readBuffer);
            if (bytesRead == -1) { // -1 == End of stream
                String returnValue = previousLastRecord;
                previousLastRecord = null; // Next call will return null immediately
                return returnValue;
            }

            previousLastRecord += new String(readBuffer, 0, bytesRead, UTF_8);

            splits = endMatcher.split(previousLastRecord, 2);
            if (splits.length == 2) {
                Matcher matcher = endMatcher.matcher(previousLastRecord);
                if (matcher.find()) {
                    String keepThis = matcher.group(1);
                    previousLastRecord = splits[1];
                    return splits[0] + keepThis;
                }
                // What do we do if this happens?
                previousLastRecord = splits[1];
                return splits[0];
            }
        }
    }
}
