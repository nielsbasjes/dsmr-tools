package nl.basjes.dsmr.parse;

import nl.basjes.parse.ReadUTF8RecordStream;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import static nl.basjes.dsmr.parse.CheckCRC.crcIsValid;
import static org.junit.Assert.assertTrue;

public class TestRecordStream {

        private static final Logger LOG = LoggerFactory.getLogger(TestRecordStream.class);

        @Test
        public void runTest() throws IOException, InterruptedException {
            FileInputStream inputStream = new FileInputStream("../ttyUSB0-raw.txt");

            ReadUTF8RecordStream reader = new ReadUTF8RecordStream("\r\n![0-9A-F]{4}\r\n", inputStream);

            String value;
            int count = 0;
            while ((value = reader.read() )!= null) {
                if (value.length() < 8) {
                    continue;
                }

                count++;
                boolean valid = crcIsValid(value);
                LOG.info("{}: {} --> {}",
                    count,
                    value.substring(value.length()-7, value.length()-2),
                    valid ? "Ok" : "BAD");
                if (value.startsWith("/")) {
                    if (!valid) {
                        crcIsValid(value);
                    }
                }
//                LOG.info("\n=========== \n{}\n=========== \n", value);

            }
            LOG.info("---------------------- Done ----------------------");
        }

// TODO       PipedInputStream
// TODO       PipedOutputStream
    @Ignore
    @Test
    public void runTestWithSimulator() throws IOException, InterruptedException {
        FileInputStream inputStream = new FileInputStream("../simulator/ttyUSB0");

        ReadUTF8RecordStream reader = new ReadUTF8RecordStream("\r\n![0-9A-F]{4}\r\n", inputStream);

        String value;
        int count = 0;
        while ((value = reader.read() )!= null) {
            if (value.length() < 8) {
                continue;
            }

            ParseDsmrTelegram.DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(value);

            count++;
            boolean valid = crcIsValid(value);
            LOG.info("{}: {} {} --> {}",
                count,
                dsmrTelegram.getTimestamp(),
                value.substring(value.length()-7, value.length()-2),
                dsmrTelegram.isValid() ? "Ok" : "BAD");
            if (value.startsWith("/")) {
                if (!valid) {
                    crcIsValid(value);
                }
            }
//                LOG.info("\n=========== \n{}\n=========== \n", value);

        }
        LOG.info("---------------------- Done ----------------------");
    }

}
