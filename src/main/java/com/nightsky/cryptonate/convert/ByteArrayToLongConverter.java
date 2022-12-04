package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class ByteArrayToLongConverter implements Converter<byte[], Long> {

    @Override
    public Long convert(byte[] source) {
        ByteBuffer buf = ByteBuffer.wrap(source);
        return buf.getLong();
    }

}
