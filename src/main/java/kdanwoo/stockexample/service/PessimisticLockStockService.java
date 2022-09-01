package kdanwoo.stockexample.service;

import kdanwoo.stockexample.domain.Stock;
import kdanwoo.stockexample.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockStockService {

  private final StockRepository stockRepository;

  @Transactional
  public void decrease(Long id, Long quantity){
    Stock stock = stockRepository.findByIdWithPessimisticLock(id);

    stock.decrease(quantity);

    stockRepository.saveAndFlush(stock);
  }

}
