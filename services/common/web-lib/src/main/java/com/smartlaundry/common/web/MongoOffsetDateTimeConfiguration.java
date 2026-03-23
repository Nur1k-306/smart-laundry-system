package com.smartlaundry.common.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class MongoOffsetDateTimeConfiguration {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                OffsetDateTimeReadConverter.INSTANCE,
                OffsetDateTimeWriteConverter.INSTANCE
        ));
    }

    @ReadingConverter
    enum OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
        INSTANCE;

        @Override
        public OffsetDateTime convert(Date source) {
            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    enum OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(OffsetDateTime source) {
            return Date.from(source.toInstant());
        }
    }
}
