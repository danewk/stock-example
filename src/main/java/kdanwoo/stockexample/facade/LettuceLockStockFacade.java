package kdanwoo.stockexample.facade;

import kdanwoo.stockexample.repository.RedisLockRepository;
import kdanwoo.stockexample.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

  private final RedisLockRepository redisLockRepository;

  private final StockService stockService;

  public void decrease(Long key, Long quantity) throws InterruptedException {
    while (!redisLockRepository.lock(key)) {
      Thread.sleep(100);
    }

    try {
      stockService.decrease(key, quantity);
    } finally {
      redisLockRepository.unlock(key);
    }
  }
}
