/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2019 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.basjes.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.regex.MatchResult;
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
        byte[] readBuffer = new byte[4];
        if (previousLastRecord == null) {
            return null;
        }

        // In case the previous read retrieved multiple records
        String record = extractRecordFromBuffer();
        if (record != null) {
            return record;
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

//            LOG.info("previousLastRecord = {}", previousLastRecord);
            record = extractRecordFromBuffer();
            if (record != null) {
                return record;
            }
        }
    }


    private String extractRecordFromBuffer() {
        // In case we now have (one or more) records return the first one.
//        String[] splits = endMatcher.split(previousLastRecord, 2);
        Matcher matcher = endMatcher.matcher(previousLastRecord);
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            int endOfPartIndex = matchResult.end(1);
            String result = previousLastRecord.substring(0, endOfPartIndex);
            previousLastRecord = previousLastRecord.substring(endOfPartIndex);
            return result;
        }
        return null;
    }

}
