package splitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import splitter.controller.CommandController;

@SpringBootApplication
public class SplitterApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SplitterApplication.class, args);
        CommandController commandProcessor = context.getBean(CommandController.class);
        commandProcessor.processCommands();
    }
}
