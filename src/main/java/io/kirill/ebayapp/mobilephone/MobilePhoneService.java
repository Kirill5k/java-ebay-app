package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.common.clients.telegram.TelegramClient;
import io.kirill.ebayapp.common.clients.cex.CexClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.MobilePhoneEbayClient;
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
  private final static String MESSAGE_TEMPLATE = "good deal on \"%s\": asking price %s, cex price %s %s";

  private final MobilePhoneEbayClient mobilePhoneEbayClient;
  private final CexClient cexClient;
  private final TelegramClient telegramClient;
  private final MobilePhoneRepository mobilePhoneRepository;

  public Mono<MobilePhone> save(MobilePhone mobilePhone) {
    return mobilePhoneRepository.save(mobilePhone);
  }

  public Flux<MobilePhone> getLatest(int limit) {
    return mobilePhoneRepository.findAll(Sort.by(new Sort.Order(DESC, "listingDetails.datePosted"))).limitRequest(limit);
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
    var details = phone.getListingDetails();
    var message = String.format(MESSAGE_TEMPLATE, phone.queryString(), details.getPrice(), details.getResellPrice(), details.getUrl());
    return telegramClient.sendMessageToMainChannel(message);
  }
}
