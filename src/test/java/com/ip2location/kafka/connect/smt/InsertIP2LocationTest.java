/*
 * Copyright © 2022 IP2Location.com (support@ip2location.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ip2location.kafka.connect.smt;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InsertIP2LocationTest {
    private static String binFile = "IP2LOCATION-LITE-DB1.BIN";
    private static String binFilePath;

    private static String ip = "8.8.8.8";
    private static String inputField = "ip_address";
    private static String countryField = "ip2location_country_code";
    private static String expectedCountry = "US";

    private InsertIP2Location<SourceRecord> xform;

    @BeforeAll
    static void setup() throws IOException {
        Path binPath = Paths.get("src", "test", "resources", binFile);
        binFilePath = binPath.toFile().getAbsolutePath();
    }

    @BeforeEach
    void init() {
        xform = new InsertIP2Location.Value<>();
    }

    @AfterEach
    public void tearDown() {
        if (xform != null) {
            xform.close();
            xform = null;
        }
    }

    @Test
    public void topLevelStructRequired() {
        assertThrows(DataException.class, () -> {
            Map<String, Object> settings = new HashMap<>();
            settings.put("ip2location.bin.path", binFilePath);
            settings.put("ip2location.input", inputField);

            xform.configure(settings);
            xform.apply(new SourceRecord(null, null, "", 0, Schema.STRING_SCHEMA, ip));
        });
    }

    @Test
    public void copySchemaAndInsertIP2LocationField() {
        final Map<String, Object> props = new HashMap<>();

        props.put("ip2location.bin.path", binFilePath);
        props.put("ip2location.input", inputField);

        xform.configure(props);

        final Schema simpleStructSchema = SchemaBuilder.struct().name("name").version(1).doc("doc").field(inputField, Schema.STRING_SCHEMA).build();
        final Struct simpleStruct = new Struct(simpleStructSchema).put(inputField, ip);

        final SourceRecord record = new SourceRecord(null, null, "test", 0, simpleStructSchema, simpleStruct);
        final SourceRecord transformedRecord = xform.apply(record);

        assertEquals(simpleStructSchema.name(), transformedRecord.valueSchema().name());
        assertEquals(simpleStructSchema.version(), transformedRecord.valueSchema().version());
        assertEquals(simpleStructSchema.doc(), transformedRecord.valueSchema().doc());

        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field(inputField).schema());
        assertEquals(ip, ((Struct) transformedRecord.value()).getString(inputField));
        assertEquals(Schema.STRING_SCHEMA, transformedRecord.valueSchema().field(countryField).schema());
        assertNotNull(((Struct) transformedRecord.value()).getString(countryField));
        assertEquals(expectedCountry, ((Struct) transformedRecord.value()).getString(countryField));
    }

    @Test
    public void schemalessInsertUuidField() {
        final Map<String, Object> props = new HashMap<>();

        props.put("ip2location.bin.path", binFilePath);
        props.put("ip2location.input", inputField);

        xform.configure(props);

        final SourceRecord record = new SourceRecord(null, null, "test", 0,
                null, Collections.singletonMap(inputField, ip));

        final SourceRecord transformedRecord = xform.apply(record);
        assertEquals(ip, ((Map) transformedRecord.value()).get(inputField));
        assertNotNull(((Map) transformedRecord.value()).get(countryField));
        assertEquals(expectedCountry, ((Map) transformedRecord.value()).get(countryField));
    }
}