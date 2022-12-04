package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class DoubleToByteArrayConverter implements Converter<Double, byte[]> {

    @Override
    public byte[] convert(Double source) {
        ByteBuffer buf = ByteBuffer.allocate(8).putDouble(source);
        return buf.array();
    }

}
