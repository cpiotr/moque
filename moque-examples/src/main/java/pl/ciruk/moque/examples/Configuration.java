package pl.ciruk.moque.examples;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;

@SpringBootConfiguration
@ComponentScan(basePackages = "pl.ciruk")
@EnableJms
@EnableAutoConfiguration
public class Configuration {
}
