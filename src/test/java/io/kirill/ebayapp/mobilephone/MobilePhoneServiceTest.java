package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.common.clients.cex.CexClient;
import io.kirill.ebayapp.common.clients.telegram.TelegramClient;
import io.kirill.ebayapp.common.domain.ResellPrice;
import io.kirill.ebayapp.mobilephone.clients.ebay.MobilePhoneEbayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Sort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
class MobilePhoneServiceTest {

  @Mock
  MobilePhoneEbayClient mobilePhoneEbayClient;

  @Mock
  CexClient cexClient;

  @Mock
  TelegramClient telegramClient;

  @Mock
  MobilePhoneRepository mobilePhoneRepository;

  @InjectMocks
  MobilePhoneService mobilePhoneService;

  @Captor
  ArgumentCaptor<Instant> dateCaptor;

  MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build().withResellPrice(null);

  @Test
  void getEndingSoonestOnEbay() {
    doAnswer(inv -> Flux.just(iphone6s))
        .when(mobilePhoneEbayClient)
        .getPhonesEndingSoon(anyInt());

    StepVerifier
        .create(mobilePhoneService.getEndingSoonestOnEbay(10))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(mobilePhoneEbayClient).getPhonesEndingSoon(10);
  }

  @Test
  void getLatestFromEbay() {
    doAnswer(inv -> Flux.just(iphone6s))
        .when(mobilePhoneEbayClient)
        .getPhonesListedInTheLastMinutes(anyInt());

    StepVerifier
        .create(mobilePhoneService.getLatestFromEbay(10))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(mobilePhoneEbayClient).getPhonesListedInTheLastMinutes(10);
  }

  @Test
  void findResellPrice() {
    doAnswer(inv -> Mono.just(new ResellPrice(null, BigDecimal.valueOf(10.0))))
        .when(cexClient)
        .getMinResellPrice(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .expectNextMatches(phone -> phone.getResellPrice().equals(new ResellPrice(null, BigDecimal.valueOf(10.0))) && phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(cexClient).getMinResellPrice(iphone6s);
  }

  @Test
  void findResellPriceWhenNotFound() {
    doAnswer(inv -> Mono.empty())
        .when(cexClient)
        .getMinResellPrice(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .expectNextMatches(phone -> phone.getResellPrice() == null && phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(cexClient).getMinResellPrice(iphone6s);
  }

  @Test
  void informAboutThePhone() {
    doAnswer(inv -> Mono.empty())
        .when(telegramClient)
        .sendMessageToMainChannel(anyString());

    StepVerifier
        .create(mobilePhoneService.informAboutThePhone(iphone6s.withResellPrice(new ResellPrice(null, BigDecimal.TEN))))
        .verifyComplete();

    verify(telegramClient).sendMessageToMainChannel("just listed \"Apple Iphone 6s 16GB Space Grey Unlocked\": ebay: £100.0, cex: £10 ebay.com");
  }

  @Test
  void getLatest() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneRepository)
        .findAll(any(Sort.class));

    StepVerifier
        .create(mobilePhoneService.getLatest(2))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(mobilePhoneRepository).findAll(Sort.by(new Sort.Order(DESC, "listingDetails.datePosted")));
  }

  @Test
  void getLatestByCondition() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneRepository)
        .findAllByCondition(anyString(), any(Sort.class));

    StepVerifier
        .create(mobilePhoneService.getLatestByCondition("Faulty", 2))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(mobilePhoneRepository).findAllByCondition("Faulty", Sort.by(new Sort.Order(DESC, "listingDetails.datePosted")));
  }

  @Test
  void feedLatest() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneRepository)
        .findByListingDetailsDatePostedAfter(dateCaptor.capture());

    StepVerifier
        .create(mobilePhoneService.feedLatest())
        .expectNextCount(3)
        .verifyComplete();

    assertThat(dateCaptor.getValue()).isCloseTo(Instant.now().minusSeconds(1800), within(5, ChronoUnit.SECONDS));
  }

  @Test
  void save() {
    doAnswer(inv -> Mono.just(((MobilePhone)inv.getArgument(0)).withId("mb1")))
        .when(mobilePhoneRepository).save(any());

    StepVerifier
        .create(mobilePhoneService.save(iphone6s.withId(null)))
        .expectNextMatches(phone -> phone.getId().equals("mb1"))
        .verifyComplete();

    verify(mobilePhoneRepository).save(iphone6s);
  }

  @Test
  void isNew() {
    doAnswer(inv -> Mono.just(true))
        .when(mobilePhoneRepository).existsByListingDetailsUrl(anyString());

    StepVerifier
        .create(mobilePhoneService.isNew(iphone6s))
        .expectNextMatches(exists -> !exists)
        .verifyComplete();

    verify(mobilePhoneRepository).existsByListingDetailsUrl(iphone6s.getListingDetails().getUrl());
  }
}