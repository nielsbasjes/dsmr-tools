package nl.basjes.nifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadUTF8RecordStream {

    private static final Logger LOG = LoggerFactory.getLogger(ReadUTF8RecordStream.class);
    private String recordStartRegex;
    private String recordStartPrefix;

    private Pattern startMatcher;

    private InputStream inputStream;

    public ReadUTF8RecordStream(String recordStartRegex, String recordStartPrefix, InputStream inputStream) {
        this.recordStartRegex = recordStartRegex;
        this.recordStartPrefix = recordStartPrefix;

        startMatcher = Pattern.compile(recordStartRegex);
        this.inputStream = inputStream;
        }

    // FIXME: This can be done a LOT more efficient.
    public void read() throws IOException, InterruptedException {
        boolean keepRunning = true;
        String previousLastRecord = "";
        byte[] readBuffer = new byte[1];
        long recordNumber = 0;
        while (keepRunning) {

            int bytesRead = inputStream.read(readBuffer);
            if (bytesRead == -1) { // -1 == End of stream
                LOG.error("<End Of File>");
                keepRunning = false;
                continue;
            }

            previousLastRecord += new String(readBuffer, UTF_8);

            String[] splits = startMatcher.split(previousLastRecord, 2);

            if (splits.length == 2) {
                recordNumber++;
                LOG.info("\n" +
                    "== {} ================================= \n" +
                    "{} \n" +
                    "== {} ================================= \n",
                    recordNumber, recordStartPrefix+splits[0], recordNumber);
                previousLastRecord = splits[1];
            }
        }
    }
}
