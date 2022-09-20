package br.com.zup.edu.nossalojavirtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NossaLojaVirtualApplication {

    public static void main(String[] args) {
        SpringApplication.run(NossaLojaVirtualApplication.class, args);
    }

//	@Bean
//	CommandLineRunner runner(UserRepository repository) {
//		User buzz = new User("buzz@toystory.com", Password.encode("123456"));
//		User woody = new User("woody@toystory.com", Password.encode("1234567"));
//
//		return (args) -> {
//			repository.save(buzz);
//			repository.save(woody);
//		};
//
//	}
}
