package motka.abhishek.auditor.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableSpringConfigured
@ComponentScan(basePackages = {"motka.abhishek.auditor.appenders"})
public class AuditorAspectConfig {
}
