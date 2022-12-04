package com.nightsky.cryptonate.convert;

import org.springframework.core.convert.support.DefaultConversionService;

/**
 *
 * @author Chris
 */
public class ConverterSupport {

    public static void addInternalConverters(DefaultConversionService service) {
        // For converting Strings:
        service.addConverter(new ByteArrayToStringConverter());
        service.addConverter(new StringToByteArrayConverter());

        // For converting Doubles:
        service.addConverter(new ByteArrayToDoubleConverter());
        service.addConverter(new DoubleToByteArrayConverter());

        // For converting Floats:
        service.addConverter(new ByteArrayToFloatConverter());
        service.addConverter(new FloatToByteArrayConverter());

        // For converting Longs:
        service.addConverter(new ByteArrayToLongConverter());
        service.addConverter(new LongToByteArrayConverter());

        // For converting Integers:
        service.addConverter(new ByteArrayToIntegerConverter());
        service.addConverter(new IntegerToByteArrayConverter());
    }

}
