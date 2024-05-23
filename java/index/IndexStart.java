package index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class IndexStart {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(IndexStart.class, args);
        Indexer indexer = new Indexer("src/main/java/index", "src/main/resources/files2");
        int docs = indexer.createIndexer();
        System.out.println("Indexed files: " + docs);

    }
}