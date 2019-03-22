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
    private Pattern startMatcher;
    private String recordStartPrefix;


    private InputStream inputStream;

    public ReadUTF8RecordStream(String recordStartRegex, String recordStartPrefix, InputStream inputStream) {
        this.startMatcher = Pattern.compile(recordStartRegex);
        this.recordStartPrefix = recordStartPrefix;

        this.inputStream = inputStream;
        }

    private String previousLastRecord = "";

    // TODO: This returns a record the moment the NEXT record appears. This may be a needless delay
    // Returns null if end of stream
    public String read() throws IOException {
        byte[] readBuffer = new byte[1];
        if (previousLastRecord == null) {
            return null;
        }

        while (true) {
            int bytesRead = inputStream.read(readBuffer);
            if (bytesRead == -1) { // -1 == End of stream
                String returnValue = previousLastRecord;
                previousLastRecord = null; // Next call will return null immediately
                return returnValue;
            }

            previousLastRecord += new String(readBuffer, UTF_8);

            String[] splits = startMatcher.split(previousLastRecord, 2);

            if (splits.length == 2) {
                previousLastRecord = splits[1];
                return splits[0];
            }
        }
    }
}
