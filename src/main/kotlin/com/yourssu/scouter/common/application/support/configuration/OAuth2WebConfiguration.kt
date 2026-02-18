package com.yourssu.scouter.common.application.support.configuration

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class OAuth2WebConfiguration : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(OAuth2TypeConverter())
    }
}

class OAuth2TypeConverter : Converter<String, OAuth2Type> {

    override fun convert(source: String): OAuth2Type {
        return OAuth2Type.from(source)
    }
}
