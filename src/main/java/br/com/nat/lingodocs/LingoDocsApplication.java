package br.com.nat.lingodocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LingoDocsApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(LingoDocsApplication.class, args);
	}

}
