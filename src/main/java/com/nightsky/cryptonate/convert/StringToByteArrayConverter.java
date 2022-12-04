package com.nightsky.cryptonate.convert;

import java.nio.charset.StandardCharsets;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class StringToByteArrayConverter implements Converter<String, byte[]> {

    @Override
    public byte[] convert(String source) {
        return source.getBytes(StandardCharsets.UTF_8);
    }

}
