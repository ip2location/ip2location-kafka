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

import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.cache.LRUCache;
import org.apache.kafka.common.cache.SynchronizedCache;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SchemaUtil;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ip2location.*;

import static org.apache.kafka.connect.transforms.util.Requirements.requireMap;
 import static org.apache.kafka.connect.transforms.util.Requirements.requireStruct;

public abstract class InsertIP2Location<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String OVERVIEW_DOC =
            "Insert IP2Location data using a field value in the record as the IP address to query.";

    private interface ConfigName {
        String IP2LOCATION_BIN_PATH = "ip2location.bin.path";
        String IP2LOCATION_INPUT = "ip2location.input";
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ConfigName.IP2LOCATION_BIN_PATH, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
                    "Path to the IP2Location BIN database")
            .define(ConfigName.IP2LOCATION_INPUT, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH,
                    "IP address field to query");

    private static final String PURPOSE = "inserting IP2Location data into record";

    private String binFile;
    private String ipAddressField;

    private Cache<Schema, Schema> schemaUpdateCache;

    @Override
    public void configure(Map<String, ?> props) {
        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        binFile = config.getString(ConfigName.IP2LOCATION_BIN_PATH);
        ipAddressField = config.getString(ConfigName.IP2LOCATION_INPUT);

        schemaUpdateCache = new SynchronizedCache<>(new LRUCache<Schema, Schema>(16));
    }

    @Override
    public R apply(R record) {
        if (operatingSchema(record) == null) {
            return applySchemaless(record);
        } else {
            return applyWithSchema(record);
        }
    }

    private R applySchemaless(R record) {
        final Map<String, Object> value = requireMap(operatingValue(record), PURPOSE);

        final Map<String, Object> updatedValue = new HashMap<>(value);

        // get IP2Location here and add the result fields below
        String prefix = "ip2location_";
        IP2Location loc = null;
        try {
            loc = new IP2Location();
            loc.Open(binFile);
            IPResult rec = loc.IPQuery(updatedValue.get(ipAddressField).toString());

            if ("OK".equals(rec.getStatus())) {
                updatedValue.put(prefix + "country_code", rec.getCountryShort());
                updatedValue.put(prefix + "country_name", rec.getCountryLong());
                updatedValue.put(prefix + "region", rec.getRegion());
                updatedValue.put(prefix + "city", rec.getCity());
                updatedValue.put(prefix + "latitude", rec.getLatitude());
                updatedValue.put(prefix + "longitude", rec.getLongitude());
                updatedValue.put(prefix + "zip_code", rec.getZipCode());
                updatedValue.put(prefix + "time_zone", rec.getTimeZone());
                updatedValue.put(prefix + "isp", rec.getISP());
                updatedValue.put(prefix + "domain", rec.getDomain());
                updatedValue.put(prefix + "net_speed", rec.getNetSpeed());
                updatedValue.put(prefix + "idd_code", rec.getIDDCode());
                updatedValue.put(prefix + "area_code", rec.getAreaCode());
                updatedValue.put(prefix + "weather_station_code", rec.getWeatherStationCode());
                updatedValue.put(prefix + "weather_station_name", rec.getWeatherStationName());
                updatedValue.put(prefix + "mcc", rec.getMCC());
                updatedValue.put(prefix + "mnc", rec.getMNC());
                updatedValue.put(prefix + "mobile_brand", rec.getMobileBrand());
                updatedValue.put(prefix + "elevation", rec.getElevation());
                updatedValue.put(prefix + "usage_type", rec.getUsageType());
                updatedValue.put(prefix + "address_type", rec.getAddressType());
                updatedValue.put(prefix + "category", rec.getCategory());
            } else if ("EMPTY_IP_ADDRESS".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "IP address cannot be blank.");
            } else if ("INVALID_IP_ADDRESS".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "Invalid IP address.");
            } else if ("MISSING_FILE".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "Invalid database path.");
            } else if ("IPV6_NOT_SUPPORTED".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "This BIN does not contain IPv6 data.");
            } else {
                updatedValue.put(prefix + "error", "Unknown error." + rec.getStatus());
            }
        } catch (IOException ex) {
            updatedValue.put(prefix + "error", ex.getMessage());
        } finally {
            if (loc != null) {
                loc.Close();
            }
        }
        return newRecord(record, null, updatedValue);
    }

    private R applyWithSchema(R record) {
        final Struct value = requireStruct(operatingValue(record), PURPOSE);

        Schema updatedSchema = schemaUpdateCache.get(value.schema());
        if (updatedSchema == null) {
            updatedSchema = makeUpdatedSchema(value.schema());
            schemaUpdateCache.put(value.schema(), updatedSchema);
        }

        final Struct updatedValue = new Struct(updatedSchema);

        for (Field field : value.schema().fields()) {
            updatedValue.put(field.name(), value.get(field));
        }

        // get IP2Location here and add the result fields below
        String prefix = "ip2location_";
        IP2Location loc = null;
        try {
            loc = new IP2Location();
            loc.Open(binFile);
            IPResult rec = loc.IPQuery(updatedValue.get(ipAddressField).toString());

            if ("OK".equals(rec.getStatus())) {
                updatedValue.put(prefix + "country_code", rec.getCountryShort());
                updatedValue.put(prefix + "country_name", rec.getCountryLong());
                updatedValue.put(prefix + "region", rec.getRegion());
                updatedValue.put(prefix + "city", rec.getCity());
                updatedValue.put(prefix + "latitude", rec.getLatitude());
                updatedValue.put(prefix + "longitude", rec.getLongitude());
                updatedValue.put(prefix + "zip_code", rec.getZipCode());
                updatedValue.put(prefix + "time_zone", rec.getTimeZone());
                updatedValue.put(prefix + "isp", rec.getISP());
                updatedValue.put(prefix + "domain", rec.getDomain());
                updatedValue.put(prefix + "net_speed", rec.getNetSpeed());
                updatedValue.put(prefix + "idd_code", rec.getIDDCode());
                updatedValue.put(prefix + "area_code", rec.getAreaCode());
                updatedValue.put(prefix + "weather_station_code", rec.getWeatherStationCode());
                updatedValue.put(prefix + "weather_station_name", rec.getWeatherStationName());
                updatedValue.put(prefix + "mcc", rec.getMCC());
                updatedValue.put(prefix + "mnc", rec.getMNC());
                updatedValue.put(prefix + "mobile_brand", rec.getMobileBrand());
                updatedValue.put(prefix + "elevation", rec.getElevation());
                updatedValue.put(prefix + "usage_type", rec.getUsageType());
                updatedValue.put(prefix + "address_type", rec.getAddressType());
                updatedValue.put(prefix + "category", rec.getCategory());
            } else if ("EMPTY_IP_ADDRESS".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "IP address cannot be blank.");
            } else if ("INVALID_IP_ADDRESS".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "Invalid IP address.");
            } else if ("MISSING_FILE".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "Invalid database path.");
            } else if ("IPV6_NOT_SUPPORTED".equals(rec.getStatus())) {
                updatedValue.put(prefix + "error", "This BIN does not contain IPv6 data.");
            } else {
                updatedValue.put(prefix + "error", "Unknown error." + rec.getStatus());
            }
        } catch (IOException ex) {
            updatedValue.put(prefix + "error", ex.getMessage());
        } finally {
            if (loc != null) {
                loc.Close();
            }
        }

        return newRecord(record, updatedSchema, updatedValue);
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
        schemaUpdateCache = null;
    }

    private Schema makeUpdatedSchema(Schema schema) {
        final SchemaBuilder builder = SchemaUtil.copySchemaBasics(schema, SchemaBuilder.struct());

        for (Field field : schema.fields()) {
            builder.field(field.name(), field.schema());
        }

        String prefix = "ip2location_";
        builder.field(prefix + "country_code", Schema.STRING_SCHEMA);
        builder.field(prefix + "country_name", Schema.STRING_SCHEMA);
        builder.field(prefix + "region", Schema.STRING_SCHEMA);
        builder.field(prefix + "city", Schema.STRING_SCHEMA);
        builder.field(prefix + "latitude", Schema.FLOAT32_SCHEMA);
        builder.field(prefix + "longitude", Schema.FLOAT32_SCHEMA);
        builder.field(prefix + "zip_code", Schema.STRING_SCHEMA);
        builder.field(prefix + "time_zone", Schema.STRING_SCHEMA);
        builder.field(prefix + "isp", Schema.STRING_SCHEMA);
        builder.field(prefix + "domain", Schema.STRING_SCHEMA);
        builder.field(prefix + "net_speed", Schema.STRING_SCHEMA);
        builder.field(prefix + "idd_code", Schema.STRING_SCHEMA);
        builder.field(prefix + "area_code", Schema.STRING_SCHEMA);
        builder.field(prefix + "weather_station_code", Schema.STRING_SCHEMA);
        builder.field(prefix + "weather_station_name", Schema.STRING_SCHEMA);
        builder.field(prefix + "mcc", Schema.STRING_SCHEMA);
        builder.field(prefix + "mnc", Schema.STRING_SCHEMA);
        builder.field(prefix + "mobile_brand", Schema.STRING_SCHEMA);
        builder.field(prefix + "elevation", Schema.FLOAT32_SCHEMA);
        builder.field(prefix + "usage_type", Schema.STRING_SCHEMA);
        builder.field(prefix + "address_type", Schema.STRING_SCHEMA);
        builder.field(prefix + "category", Schema.STRING_SCHEMA);
        builder.field(prefix + "error", Schema.STRING_SCHEMA);

        return builder.build();
    }

    protected abstract Schema operatingSchema(R record);

    protected abstract Object operatingValue(R record);

    protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue);

    public static class Key<R extends ConnectRecord<R>> extends InsertIP2Location<R> {

        @Override
        protected Schema operatingSchema(R record) {
            return record.keySchema();
        }

        @Override
        protected Object operatingValue(R record) {
            return record.key();
        }

        @Override
        protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
            return record.newRecord(record.topic(), record.kafkaPartition(), updatedSchema, updatedValue, record.valueSchema(), record.value(), record.timestamp());
        }

    }

    public static class Value<R extends ConnectRecord<R>> extends InsertIP2Location<R> {

        @Override
        protected Schema operatingSchema(R record) {
            return record.valueSchema();
        }

        @Override
        protected Object operatingValue(R record) {
            return record.value();
        }

        @Override
        protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), updatedSchema, updatedValue, record.timestamp());
        }

    }
}


