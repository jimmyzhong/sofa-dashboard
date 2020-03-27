package me.izhong.dashboard.web.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class MappingConverterAdapter {

	@Bean
	public Converter<String, LocalDateTime> LocalDateTimeConvert() {
		return new Converter<String, LocalDateTime>() {
			@Override
			public LocalDateTime convert(String source) {
				DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime date = null;
				try {
					date = LocalDateTime.parse((String) source, df);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return date;
			}
		};
	}
}
