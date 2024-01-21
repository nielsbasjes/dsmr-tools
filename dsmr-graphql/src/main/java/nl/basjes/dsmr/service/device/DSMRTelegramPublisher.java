/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2024 Niels Basjes
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

package nl.basjes.dsmr.service.device;

import nl.basjes.dsmr.DSMRTelegram;
import reactor.core.publisher.Flux;

import java.util.List;

public interface DSMRTelegramPublisher {
    /**
     * Publish a new measurement
     * @param measurement The measurement to be published
     */
    void publish(DSMRTelegram measurement);

    /**
     * Get all the future published measurements as a flux
     * @return The flux
     */
    Flux<DSMRTelegram> asFlux();

    List<DSMRTelegram> getMeasurements(Long epoch, Integer count);
}
