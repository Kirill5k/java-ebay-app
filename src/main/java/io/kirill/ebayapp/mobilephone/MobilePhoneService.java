package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.clients.cex.CexClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.EbayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MobilePhoneService {
  private final EbayClient ebayClient;
  private final CexClient cexClient;
  private final MobilePhoneRepository mobilePhoneRepository;

  public Flux<MobilePhone> getLatestPhonesFromEbay(int minutes) {
    return ebayClient.getPhonesListedInTheLastMinutes(minutes)
        .flatMap(mobilePhoneRepository::save);
  }

  public Mono<MobilePhone> findResellPrice(MobilePhone phone) {
    return cexClient.getAveragePrice(phone)
        .map(phone::withResellPrice)
        .flatMap(mobilePhoneRepository::save);
  }
}
