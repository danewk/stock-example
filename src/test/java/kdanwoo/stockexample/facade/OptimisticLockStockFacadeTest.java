package kdanwoo.stockexample.facade;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kdanwoo.stockexample.domain.Stock;
import kdanwoo.stockexample.repository.StockRepository;
import kdanwoo.stockexample.service.PessimisticLockStockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OptimisticLockStockFacadeTest {

  @Autowired
  private OptimisticLockStockFacade stockFacade;

  @Autowired
  private StockRepository stockRepository;

  @BeforeEach
  public void before(){
    Stock stock = new Stock(1L, 100L);
    stockRepository.saveAndFlush(stock);
  }

  @AfterEach
  public void after(){
    stockRepository.deleteAll();
  }

  @Test
  public void stock_decrease() throws InterruptedException {
    stockFacade.decrease(1L, 1L);

    // 100 - 1 : 99개
    Stock stock = stockRepository.findById(1L)
        .orElseThrow();

    assertEquals(stock.getQuantity(), 99L);
  }

  @Test
  public void 동시에_100개의_요청() throws InterruptedException {
    int threadCount = 100;

    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch countDownLatch = new CountDownLatch(threadCount);

    for(int i=0 ; i < threadCount ; i++ ){
      executorService.submit(()-> {
        try {
          stockFacade.decrease(1L, 1L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally {
          countDownLatch.countDown();
        }
      });
    }

    countDownLatch.await();

    Stock stock = stockRepository.findById(1L)
        .orElseThrow();

    assertEquals(0L, stock.getQuantity());


  }
}