package com.jchiocchio.config;

import com.jchiocchio.mapping.BoardToBoardDTOConverter;

import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;

import org.springframework.context.annotation.Configuration;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;

@Configuration
public class OrikaConfig implements OrikaMapperFactoryConfigurer {

    @Override
    public void configure(MapperFactory mapperFactory) {
        this.registerCustomConverters(mapperFactory);
    }

    private void registerCustomConverters(MapperFactory mapperFactory) {
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        converterFactory.registerConverter(new BoardToBoardDTOConverter());
    }
}
