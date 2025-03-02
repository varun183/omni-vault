	package com.personal.omnivault;

	import com.personal.omnivault.config.StorageProperties;
	import com.personal.omnivault.service.StorageService;
	import org.springframework.boot.CommandLineRunner;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.boot.context.properties.EnableConfigurationProperties;
	import org.springframework.context.annotation.Bean;
	import org.springframework.scheduling.annotation.EnableAsync;
	import org.springframework.scheduling.annotation.EnableScheduling;

	@SpringBootApplication
	@EnableConfigurationProperties(StorageProperties.class)
	@EnableAsync
	@EnableScheduling
	public class OmnivaultBackendApplication {

		public static void main(String[] args) {
			SpringApplication.run(OmnivaultBackendApplication.class, args);
		}

		@Bean
		CommandLineRunner init(StorageService storageService) {
			return (args) -> {
				// Initialize storage location
				storageService.init();
			};
		}
	}