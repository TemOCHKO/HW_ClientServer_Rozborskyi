package org.temochko.HW2and3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.temochko.Message;
import org.temochko.Models.ProductCriteria;
import org.temochko.Processor;
import org.temochko.Repositories.IProductRepository;
import org.temochko.Repositories.SqlLiteProductRepository;
import org.temochko.Services.IProductService;
import org.temochko.Services.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorTest {
    private final IProductRepository productRepository = new SqlLiteProductRepository("products.db");
    private IProductService service;
    private BlockingQueue<Message> inputQueue;
    private BlockingQueue<Message> outputQueue;
    private List<Processor> processors;
    private List<Thread> processorThreads;

    @BeforeEach
    public void setUp() {
        service = new ProductService(productRepository);
        inputQueue = new LinkedBlockingQueue<>();
        outputQueue = new LinkedBlockingQueue<>();
        processors = new ArrayList<>();
        processorThreads = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Processor p = new Processor(inputQueue, outputQueue, service);
            processors.add(p);
            Thread t = new Thread(p, "Processor " + i);
            processorThreads.add(t);
            t.start();
        }
    }

    @AfterEach
    public void end() {
        for (Processor p : processors) {
            p.stop();
        }
        for (Thread t : processorThreads) {
            t.interrupt();
        }
    }

    @Test
    public void testAddition() throws InterruptedException {
        int numberOfThreads = 25;
        int amountToAddPerThread = 3;
        int targetProductId = 1;

        CountDownLatch startAtTheSameTimeLatch = new CountDownLatch(1);
        CountDownLatch eachThreadDoneLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            executorService.submit(() -> {
                try {
                    startAtTheSameTimeLatch.await();

                    String payload = targetProductId + "/" + amountToAddPerThread;
                    Message msg = new Message((byte) 1, threadNum, 1, 1, payload);

                    inputQueue.put(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    eachThreadDoneLatch.countDown();
                }
            });
        }

        // Ready set go
        startAtTheSameTimeLatch.countDown();
        eachThreadDoneLatch.await(5, TimeUnit.SECONDS);

        // dont continue before everyone has done
        while (outputQueue.size() < numberOfThreads) {
            Thread.sleep(50);
        }
        Thread.sleep(100);
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        int expectedTotal = numberOfThreads * amountToAddPerThread;
        int actualTotal =  service.getAllProducts(new ProductCriteria()).size();

        assertEquals(expectedTotal, actualTotal);
        executorService.shutdown();
    }

    @Test
    public void testRemove() throws InterruptedException {
        int productId = 2;
        service.addStock(productId, 600);

        int threadsCount = 50;

        CountDownLatch startAtTheSameTimeLatch = new CountDownLatch(1);
        CountDownLatch eachThreadDoneLatch = new CountDownLatch(threadsCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    startAtTheSameTimeLatch.await();
                    Message msg = new Message((byte) 1, 0, 2, 1, productId + "/20");
                    inputQueue.put(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    eachThreadDoneLatch.countDown();
                }
            });
        }

        startAtTheSameTimeLatch.countDown();
        eachThreadDoneLatch.await(5, TimeUnit.SECONDS);

        // dont continue before everyone has done
        while (outputQueue.size() < threadsCount) {
            Thread.sleep(50);
        }

        Thread.sleep(100);

        assertEquals(0, service.getAllProducts(new ProductCriteria()).size());
        executor.shutdown();
    }

    @Test
    public void testGet() throws InterruptedException {
        int targetProductId = 3;
        service.addStock(targetProductId, 67);

        int threadsCount = 80;
        CountDownLatch startAtTheSameTimeLatch = new CountDownLatch(1);
        CountDownLatch eachThreadDoneLatch = new CountDownLatch(threadsCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    startAtTheSameTimeLatch.await();
                    Message msg = new Message((byte) 1, 0, 3, 1, String.valueOf(targetProductId));
                    inputQueue.put(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    eachThreadDoneLatch.countDown();
                }
            });
        }

        startAtTheSameTimeLatch.countDown();
        eachThreadDoneLatch.await(5, TimeUnit.SECONDS);

        // dont continue before everyone has done
        while (outputQueue.size() < threadsCount) {
            Thread.sleep(50);
        }

        Thread.sleep(100);
        assertEquals(80, outputQueue.size());

        int correctResponses = 0;
        while (!outputQueue.isEmpty()) {
            Message response = outputQueue.poll();
            if (response.getMessageString().contains("Stock 67")) {
                correctResponses++;
            }
        }
        assertEquals(80, correctResponses);
        executor.shutdown();
    }
}
