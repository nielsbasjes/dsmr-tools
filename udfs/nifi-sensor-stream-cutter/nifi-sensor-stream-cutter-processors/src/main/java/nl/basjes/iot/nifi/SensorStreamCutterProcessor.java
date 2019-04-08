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
package nl.basjes.iot.nifi;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;

import static org.apache.nifi.processor.util.StandardValidators.createLongValidator;
import static org.apache.nifi.processor.util.StandardValidators.createRegexValidator;

@Tags({"iot", "dsmr"})
@CapabilityDescription("Reads an infinite UTF-8 text stream from a character device (or file) and " +
    "creates a flowfile each time the end of a record is seen. The end of a record is detected by the provided pattern.")
@SeeAlso({})
//@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
//@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class SensorStreamCutterProcessor extends AbstractProcessor {

    public static final PropertyDescriptor FILE_NAME = new PropertyDescriptor
        .Builder()
        .name("Input filename/character device")
        .displayName("Input filename/character device")
        .description("The local file name of the file or character device from where the records can be read.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(createRegexValidator(0, 0, false)) // It must be a regex without grouping
        .build();

    public static final PropertyDescriptor END_OF_RECORD_REGEX = new PropertyDescriptor
        .Builder()
        .name("End-of-record regex")
        .displayName("End-of-record regex")
        .description("The regular expression that is the end of the record. NOTE: Grouping is NOT allowed!")
        .required(true)
        .defaultValue("\n")
        .allowableValues("\n", "\r\n![0-9A-F]{4}\r\n") // FIXME: Show these allowed values and still support a manual regex
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(createRegexValidator(0, 0, false)) // It must be a regex without grouping
        .build();

    public static final PropertyDescriptor MAX_CHARACTERS_PER_RECORD = new PropertyDescriptor
        .Builder()
        .name("Max characters per record")
        .displayName("Max characters per record")
        .description("The maximum number of characters that can occur in a normal record. " +
            "This limit is used to force cut the stream if no end-of-record markers are found (to avoid memory overflow failures)")
        .required(true)
        .defaultValue("10240")
        .addValidator(createLongValidator(1024, 10 * 1024 * 1024, true)) // Anything from 1KiB to 10 MiB is allowed.
        .build();

    static final Relationship SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Here we route all FlowFiles that have been successfully extracted from the stream.")
        .build();

    static final Relationship TOOLONG = new Relationship.Builder()
        .name("too long")
        .description("Here we route the text of the stream cut by \"\"all FlowFiles that have been analyzed.")
        .build();

//    static final Relationship MISSING = new Relationship.Builder()
//        .name("missing")
//        .description("Here we route the FlowFiles that did not have the " + USERAGENTSTRING_ATTRIBUTENAME + " attribute set.")
//        .build();
//
//
//    public static final Relationship MY_RELATIONSHIP = new Relationship.Builder()
//        .name("MY_RELATIONSHIP")
//        .description("Example relationship")
//        .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(FILE_NAME);
        descriptors.add(END_OF_RECORD_REGEX);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        relationships.add(TOOLONG);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        // TODO implement
    }
}
