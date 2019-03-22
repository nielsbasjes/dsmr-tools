package nl.basjes.nifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.cs.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadUTF8RecordStream {

    private static final Logger LOG = LoggerFactory.getLogger(ReadUTF8RecordStream.class);
    private String recordStartRegex;
    private String recordEndRegex;

    private Pattern startMatcher;
    private Matcher endMatcher;

    private InputStream inputStream;

    public ReadUTF8RecordStream(String recordStartRegex, String recordEndRegex, InputStream inputStream) {
        this.recordStartRegex = recordStartRegex;
        this.recordEndRegex = recordEndRegex;

        startMatcher = Pattern.compile(recordStartRegex);
        this.inputStream = inputStream;
        }

    // FIXME: This can be done a LOT more efficient.
    public void read() throws IOException, InterruptedException {
        boolean keepRunning = true;
        String previousLastRecord = "";
        byte[] readBuffer = new byte[4096];
        int sleepsDone = 0;
        while (keepRunning) {
            if (sleepsDone > 10) { // FIXME: Bad magic number
                // It must have stopped
                keepRunning = false;
                continue;
            }
            int bytesRead = inputStream.read(readBuffer);

            if (bytesRead == 0) {
                Thread.sleep(1); // Sleep 1 ms
                sleepsDone++;
//                return;
                continue;
            }
            sleepsDone = 0;
            previousLastRecord += new String(readBuffer, UTF_8);

            String [] splits = startMatcher.split(previousLastRecord, 2);

            if (splits.length == 2) {
                LOG.info("{}", splits[0]);
            }

            previousLastRecord = splits[1];
        }
    }
}
