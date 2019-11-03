package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.clients.cex.CexClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.EbayClient;
import io.kirill.ebayapp.mobilephone.clients.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
public class MobilePhoneService {
  private final EbayClient ebayClient;
  private final CexClient cexClient;
  private final TelegramClient telegramClient;
  private final MobilePhoneRepository mobilePhoneRepository;

  public Mono<Boolean> isNew(MobilePhone mobilePhone) {
    return mobilePhoneRepository.existsByUrl(mobilePhone.getUrl()).map(exists -> !exists);
  }

  public Mono<MobilePhone> save(MobilePhone mobilePhone) {
    return mobilePhoneRepository.save(mobilePhone);
  }

  public Flux<MobilePhone> getLatest(int limit) {
    return mobilePhoneRepository.findAll(Sort.by(new Sort.Order(DESC, "datePosted"))).limitRequest(limit);
  }

  public Flux<MobilePhone> feedLatest() {
    return mobilePhoneRepository.findByDatePostedAfter(Instant.now().minusSeconds(1800));
  }

  public Flux<MobilePhone> getLatestFromEbay(int minutes) {
    return ebayClient.getPhonesListedInTheLastMinutes(minutes);
  }

  public Mono<MobilePhone> findResellPrice(MobilePhone phone) {
    return cexClient.getAveragePrice(phone)
        .map(phone::withResellPrice)
        .defaultIfEmpty(phone);
  }

  public Mono<Void> informAboutThePhone(MobilePhone phone) {
    return telegramClient.informAboutThePhone(phone);
  }
}
