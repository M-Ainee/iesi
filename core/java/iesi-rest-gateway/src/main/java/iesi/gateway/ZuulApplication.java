package iesi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import iesi.gateway.filters.CustomFilter;

@SpringBootApplication
@EnableZuulProxy
@EnableOAuth2Sso
@Controller
@EnableAutoConfiguration
public class ZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZuulApplication.class, args);
	}

	@Bean
	public CustomFilter customFilter() {
	    return new CustomFilter();
	}

}