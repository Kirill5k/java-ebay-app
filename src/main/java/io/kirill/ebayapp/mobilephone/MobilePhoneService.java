package io.kirill.ebayapp.mobilephone;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.kirill.ebayapp.common.clients.cex.CexClient;
import io.kirill.ebayapp.common.clients.telegram.TelegramClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.MobilePhoneEbayClient;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MobilePhoneService {
  private final MobilePhoneEbayClient mobilePhoneEbayClient;
  private final CexClient cexClient;
  private final TelegramClient telegramClient;
  private final MobilePhoneRepository mobilePhoneRepository;

  public Mono<MobilePhone> save(MobilePhone mobilePhone) {
    return mobilePhoneRepository.save(mobilePhone);
  }

  public Flux<MobilePhone> getLatestByCondition(String condition, int limit) {
    return mobilePhoneRepository.findAllByCondition(condition, Sort.by(new Sort.Order(DESC, "listingDetails.datePosted")))
        .limitRequest(limit);
  }

  public Flux<MobilePhone> getLatest(int limit) {
    return mobilePhoneRepository.findAll(Sort.by(new Sort.Order(DESC, "listingDetails.datePosted")))
        .limitRequest(limit);
  }

  public Flux<MobilePhone> feedLatest() {
    return mobilePhoneRepository.findByListingDetailsDatePostedAfter(Instant.now().minusSeconds(1800));
  }

  public Flux<MobilePhone> getLatestFromEbay(int minutes) {
    return mobilePhoneEbayClient.getPhonesListedInTheLastMinutes(minutes);
  }

  public Mono<MobilePhone> findResellPrice(MobilePhone phone) {
    return Mono.just(phone)
        .flatMap(cexClient::getMinResellPrice)
        .map(phone::withResellPrice)
        .defaultIfEmpty(phone);
  }

  public Mono<Void> informAboutThePhone(MobilePhone phone) {
    return telegramClient.sendMessageToMainChannel(phone.goodDealMessage());
  }

  public Mono<Boolean> isNew(MobilePhone phone) {
    return mobilePhoneRepository.existsByListingDetailsUrl(phone.getListingDetails().getUrl())
        .map(exists -> !exists);
  }
}
