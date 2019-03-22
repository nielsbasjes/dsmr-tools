package nl.basjes.nifi;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestReadRecordStream {

    private static final Logger LOG = LoggerFactory.getLogger(TestReadRecordStream.class);

    @Test
    public void runTest() throws IOException, InterruptedException {
        FileInputStream inputStream = new FileInputStream("ttyUSB0-mini.txt");

        ReadUTF8RecordStream reader = new ReadUTF8RecordStream("\n", null, inputStream);

        reader.read();
    }
}
