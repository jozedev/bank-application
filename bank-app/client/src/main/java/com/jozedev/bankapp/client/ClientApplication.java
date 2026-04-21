package com.jozedev.bankapp.client;

import com.jozedev.bankapp.client.configuration.ByteBooleanConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("http://localhost:*");
		config.addAllowedOriginPattern("http://localhost");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}

	@Bean
	public R2dbcCustomConversions r2dbcCustomConversions(DatabaseClient databaseClient) {
		R2dbcDialect dialect = DialectResolver.getDialect(databaseClient.getConnectionFactory());

		List<Object> converters = new ArrayList<>();
		converters.add(new ByteBooleanConverter());

		return new R2dbcCustomConversions(CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder()), converters);
	}
}
