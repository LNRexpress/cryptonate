package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class ByteArrayToFloatConverter implements Converter<byte[], Float> {

    @Override
    public Float convert(byte[] source) {
        ByteBuffer buf = ByteBuffer.wrap(source);
        return buf.getFloat();
    }

}
