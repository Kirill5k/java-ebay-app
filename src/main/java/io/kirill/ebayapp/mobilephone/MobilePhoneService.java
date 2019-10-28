package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.clients.cex.CexClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.EbayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class MobilePhoneService {
  private final EbayClient ebayClient;
  private final CexClient cexClient;
  private final MobilePhoneRepository mobilePhoneRepository;

  public Flux<MobilePhone> getLatestPhonesFromEbay() {
    return ebayClient.getPhonesListedInLast10Mins()
        .flatMap(mobilePhoneRepository::save);
  }
}
