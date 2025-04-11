	package com.omnivault;

	import com.omnivault.config.StorageProperties;
	import com.omnivault.service.impl.HybridFileService;
	import org.springframework.boot.CommandLineRunner;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.boot.context.properties.EnableConfigurationProperties;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Profile;
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

		@Profile("!test")
		@Bean
		CommandLineRunner init(HybridFileService hybridFileService) {
			return (args) -> {
				// Initialize storage location
				hybridFileService.init();
			};
		}
	}