package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class LongToByteArrayConverter implements Converter<Long, byte[]> {

    @Override
    public byte[] convert(Long source) {
        ByteBuffer buf = ByteBuffer.allocate(8).putLong(source);
        return buf.array();
    }

}
