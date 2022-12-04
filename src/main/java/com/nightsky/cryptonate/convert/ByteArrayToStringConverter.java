package com.nightsky.cryptonate.convert;

import java.nio.charset.StandardCharsets;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class ByteArrayToStringConverter implements Converter<byte[], String> {

    @Override
    public String convert(byte[] source) {
        return new String(source, StandardCharsets.UTF_8);
    }

}
