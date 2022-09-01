package kdanwoo.stockexample.facade;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kdanwoo.stockexample.domain.Stock;
import kdanwoo.stockexample.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LettuceLockStockFacadeTest {

  @Autowired
  private LettuceLockStockFacade lettuceLockStockFacade;

  @Autowired
  private StockRepository stockRepository;

  @BeforeEach
  public void insert() {
    Stock stock = new Stock(1L, 100L);

    stockRepository.saveAndFlush(stock);
  }

  @AfterEach
  public void delete() {
    stockRepository.deleteAll();
  }

  @Test
  public void stock_decrease() throws InterruptedException {
    lettuceLockStockFacade.decrease(1L, 1L);

    // 100 - 1 : 99개
    Stock stock = stockRepository.findById(1L)
        .orElseThrow();

    assertEquals(stock.getQuantity(), 99L);
  }

  @Test
  public void 동시에_100개의요청() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          lettuceLockStockFacade.decrease(1L, 1L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    Stock stock = stockRepository.findById(1L).orElseThrow();

    // 100 - (100 * 1) = 0
    assertEquals(0, stock.getQuantity());
  }

}