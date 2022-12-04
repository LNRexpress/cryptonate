package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class IntegerToByteArrayConverter implements Converter<Integer, byte[]> {

    @Override
    public byte[] convert(Integer source) {
        ByteBuffer buf = ByteBuffer.allocate(4).putInt(source);
        return buf.array();
    }

}
