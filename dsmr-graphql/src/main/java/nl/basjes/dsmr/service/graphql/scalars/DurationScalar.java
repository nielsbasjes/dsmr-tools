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

package nl.basjes.dsmr.service.graphql.scalars;

import graphql.GraphQLContext;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class DurationScalar {

    public static final GraphQLScalarType INSTANCE;

    private DurationScalar() {}

    static {
        Coercing<Duration, Long> coercing = new Coercing<>() {
            @Override
            public @Nullable Long serialize(@NotNull Object dataFetcherResult,
                                            @NotNull GraphQLContext graphQLContext,
                                            @NotNull Locale locale) throws CoercingSerializeException {
                if (dataFetcherResult instanceof Duration) {
                    Duration duration = (Duration) dataFetcherResult;
                    return duration.getSeconds();
                }
                throw new CoercingParseValueException("Can only serialize a Duration instance");
            }

            @Override
            public @Nullable Duration parseValue(@NotNull Object input,
                                                 @NotNull GraphQLContext graphQLContext,
                                                 @NotNull Locale locale) throws CoercingParseValueException {
                if (input instanceof Integer) {
                    return Duration.of((Integer) input, ChronoUnit.SECONDS);
                }
                if (input instanceof Long) {
                    return Duration.of((Long) input, ChronoUnit.SECONDS);
                }
                throw new CoercingParseValueException(
                    "Can only parseValue from an Integer or Long. " +
                    "This was a " + input.getClass().getCanonicalName());
            }
        };

        INSTANCE = GraphQLScalarType.newScalar()
                .name("Duration")
                .description("A duration in seconds")
                .coercing(coercing)
                .build();
    }

}
