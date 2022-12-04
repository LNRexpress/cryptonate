package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class FloatToByteArrayConverter implements Converter<Float, byte[]> {

    @Override
    public byte[] convert(Float source) {
        ByteBuffer buf = ByteBuffer.allocate(4).putFloat(source);
        return buf.array();
    }

}
