package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class ByteArrayToDoubleConverter implements Converter<byte[], Double> {

    @Override
    public Double convert(byte[] source) {
        ByteBuffer buf = ByteBuffer.wrap(source);
        return buf.getDouble();
    }

}
