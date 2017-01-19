package jishoMainingu.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "jishoMainingu" })
@SpringBootApplication
public class JishoMaininguMain {
	public static void main(String[] arguments) {
		SpringApplication.run(JishoMaininguMain.class, arguments);
		
		System.out.println();
		System.out.println("Browser starten, http://localhost:8080/ eingeben");
	}
}
