/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2021 Niels Basjes
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

package nl.basjes.dsmr.graphql;

import lombok.extern.log4j.Log4j2;
import nl.basjes.dsmr.DSMRTelegram;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static reactor.core.publisher.Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER;

@Component
@Log4j2
public class DSMRTelegramPublisherImpl implements DSMRTelegramPublisher {

    // About 1 per second, keep 2 days history.
    private static final long MAX_HISTORY = 60*60*24*2L;
    private final TreeMap<Long, DSMRTelegram> history = new TreeMap<>();

    GraphQlConfig config;

    public DSMRTelegramPublisherImpl(GraphQlConfig config) {
        this.config = config;
    }

    @Override
    public void publish(DSMRTelegram measurement) {
        log.debug("Emit : {}", measurement);
        publishToHistory(measurement);
        publishToSink(measurement);
    }

    private void publishToHistory(DSMRTelegram dsmrTelegram) {
        // Store the dsmrTelegram
        history.put(dsmrTelegram.getReceiveTimestamp().toInstant().toEpochMilli(), dsmrTelegram);

        // Prune buffer to max size
        while (history.size() > MAX_HISTORY) {
            history.pollFirstEntry();
        }
        log.debug("- History has {} entries from {} to {} ",
                history.size(),
                history.firstEntry().getKey(),
                history.lastEntry().getKey());
    }

    private void publishToSink(DSMRTelegram measurement) {
        Sinks.EmitResult emitResult = config.sink().tryEmitNext(measurement);
        if (emitResult.isFailure()) {
            if (emitResult != FAIL_ZERO_SUBSCRIBER) {
                log.error("Got an emit failure: Measurement {} --> Sinks.EmitResult {}", measurement, emitResult);
            }
        }
    }

    @Override
    public Flux<DSMRTelegram> asFlux() {
        return config.flux();
    }

    @Override
    public List<DSMRTelegram> getMeasurements(Long epoch, Integer count) {
        List<DSMRTelegram> measurements;
        if (epoch == null) {
            measurements = new ArrayList<>(history.values());
        } else {
            measurements = new ArrayList<>(history.subMap(epoch, System.currentTimeMillis()).values());
        }
        if (count != null && count <= measurements.size()) {
            measurements = measurements.subList(measurements.size() - count, measurements.size());
        }
        return measurements;
    }
}
