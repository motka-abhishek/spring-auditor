package motka.abhishek.auditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditorApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(AuditorApplication.class, args);
        new TestObject().print(new Modal());
        new TestObject().printHello("Abhishek");
    }

}
