package com.example.mock_otel_app;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
class MockRestController {
    Logger logger = LoggerFactory.getLogger(MockRestController.class);
    private final Tracer tracer;

    @Autowired
    MockRestController(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(MockRestController.class.getName());
    }

    @GetMapping("/fast/{k}")
    String fast(@PathVariable("k") String k) {
        int kbSize = 0;
        try {
            kbSize = Integer.parseInt(k);
            findUserMock();
            lookupAccountMock();
        }
        catch (Exception e){
            logger.error(String.format("Invalid API Request Parameter k=%s",k));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        logger.info("Fast method call that returns a fixed length string");
        return getMockResponse(kbSize);
    }

    private void findUserMock() throws InterruptedException {
        Span span = tracer.spanBuilder("find user mock").startSpan();
        TimeUnit.MILLISECONDS.sleep(100);
        logger.info("found user");
        span.end();
    }

    private void lookupAccountMock() throws InterruptedException {
        Span span = tracer.spanBuilder("lookup account mock").startSpan();
        TimeUnit.MILLISECONDS.sleep(500);
        logger.info("account found");
        span.end();
    }

    private void slowMethodMock() throws InterruptedException {
        Span span = tracer.spanBuilder("slow method mock").startSpan();
        TimeUnit.SECONDS.sleep(3);
        logger.info("slow method");
        span.end();
    }

    @GetMapping("/slow/{s}/{k}")
    String slow(@PathVariable("s") String s, @PathVariable("k") String k) throws InterruptedException {
        int kbSize = 0;
        int sleepTime = 0;
        try {
            kbSize = Integer.parseInt(k);
            sleepTime = Integer.parseInt(s);
            findUserMock();
            lookupAccountMock();
            slowMethodMock();
        }
        catch (Exception e){
            logger.error(String.format("Invalid API Request Parameter k=%s, s=%s",k,s));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        logger.warn("About to go to sleep for a bit");
        TimeUnit.SECONDS.sleep(sleepTime);
        logger.info("Woke up after a brief nap");
        return getMockResponse(kbSize);
    }

    @GetMapping("/roulette/{o}")
    String roulette(@PathVariable("o") String o) throws InterruptedException {
        try {
            int odds = 0;
            odds = Integer.parseInt(o);
            Random random = new Random();
            int min = 1;
            int max = odds;
            int nbr = random.nextInt((max - min) + 1) + min;
            if (nbr == odds) {
                throw new RuntimeException("Something very bad happened. Bad luck...");
            }
            logger.warn(String.format("You have a 1 in %d chance of NOT getting this message", odds));
            return getMockResponse(1);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/terminate")
    void terminate() {
        logger.error("Who told you to do that? Something bad is going to happen");
        int counter = 0;
        String shining = "All work and no play makes Jack a dull boy\n";
        try{
            while (10 > 1){
                shining += shining;
                counter++;
                logger.debug(shining);
                logger.error(String.format("Cant stop, won't stop. Forever loop count=%d",counter));
            }
        }
        catch (OutOfMemoryError o){
            logger.error(o.getMessage(), o);
        }
        finally {
            System.exit(-1);
        }
    }

    private String getMockResponse(int s) {
        String response = "";
        int kbSize = s * 1000;

        for(int i=0; i<kbSize; i++) {
            if (i % 100 == 0)
                response += "\n";
            response += "!";
        }

        return response;
    }

    @ExceptionHandler({InterruptedException.class})
    public String error(Exception e) throws ResponseStatusException {
        logger.error(e.getMessage());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public String errorInvalidInput(Exception e) throws ResponseStatusException {
        logger.error(e.getMessage());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
