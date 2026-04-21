package com.jozedev.bankapp.client.configuration;

import org.springframework.core.convert.converter.Converter;
import java.nio.ByteBuffer;

public class ByteBooleanConverter implements Converter<ByteBuffer, Boolean> {

    @Override
    public Boolean convert(ByteBuffer source) {
        return source.hasRemaining() && source.get(source.position()) != 0;
    }
}