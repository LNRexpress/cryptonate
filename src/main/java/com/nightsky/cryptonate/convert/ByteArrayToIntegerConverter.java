package com.nightsky.cryptonate.convert;

import java.nio.ByteBuffer;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Chris
 */
public class ByteArrayToIntegerConverter implements Converter<byte[], Integer> {

    @Override
    public Integer convert(byte[] source) {
        ByteBuffer buf = ByteBuffer.wrap(source);
        return buf.getInt();
    }

}
