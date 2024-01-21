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

package nl.basjes.dsmr.service.graphql.endpoints;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.log4j.Log4j2;
import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.service.device.DSMRTelegramPublisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@EnableScheduling
@Log4j2
public class GraphQlRequestHandler {

    private final DSMRTelegramPublisher output;

    public GraphQlRequestHandler(DSMRTelegramPublisher output) {
        this.output = output;
    }

    @QueryMapping("dsmrTelegram")
    public List<DSMRTelegram> queryMeasurement(
            @Argument("onlyValid")  Boolean onlyValid,
            @Argument("since")      Long since,
            @Argument("count")      Integer count
    ) {
        List<DSMRTelegram> measurements = output.getMeasurements(since, count);
        if (onlyValid == Boolean.TRUE) {
            return measurements
                .stream()
                .filter(DSMRTelegram::isValid)
                .collect(Collectors.toList());
        }
        return measurements;
    }

    @SubscriptionMapping("dsmrTelegram")
    public Flux<DSMRTelegram> subscribeMeasurement(
        @Argument("onlyValid")  Boolean onlyValid
    ) {
        Flux<DSMRTelegram> dsmrTelegramFlux = output.asFlux();
        if (onlyValid == Boolean.TRUE) {
            dsmrTelegramFlux = dsmrTelegramFlux.filter(DSMRTelegram::isValid);
        }
        return dsmrTelegramFlux;
    }

    @SuppressWarnings("unused") // Used via annotation
    @GraphQlExceptionHandler
    public GraphQLError handle(@NonNull Throwable ex, @NonNull DataFetchingEnvironment environment){
        return GraphQLError
                .newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(environment.getExecutionStepInfo().getPath())
                .location(environment.getField().getSourceLocation())
                .build();
    }
}
