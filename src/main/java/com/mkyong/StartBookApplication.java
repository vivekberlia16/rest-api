package com.mkyong;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;

@SpringBootApplication
public class StartBookApplication {

    @Autowired
    HttpClientUtil httpClientUtil;

    // start everything
    public static void main(String[] args) {
        SpringApplication.run(StartBookApplication.class, args);
    }

    // run this only on profile 'demo', avoid run this in test
    @Profile("demo")
    @Bean
    CommandLineRunner initDatabase(BookRepository repository) {
        return args -> {
            repository.save(new Book("A Guide to the Bodhisattva Way of Life", "Santideva", new BigDecimal("15.41")));
            repository.save(new Book("The Life-Changing Magic of Tidying Up", "Marie Kondo", new BigDecimal("9.69")));
            repository.save(new Book("Refactoring: Improving the Design of Existing Code", "Martin Fowler", new BigDecimal("47.99")));
        };
    }
    
    void httpUtil()
    {
            try {
              String res=  httpClientUtil.apiServiceUtil(new HashMap<>(),"https://test-servicevivek.herokuapp.com/books",null, HttpMethod.GET);
                System.out.println("res"+res);
                HttpClientBuilder builder=HttpClientBuilder.create();
            }catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("here error"+e);
            }

    }

}