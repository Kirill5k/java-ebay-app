package io.kirill.ebayapp.mobilephone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.kirill.ebayapp.mobilephone.clients.cex.CexClient;
import io.kirill.ebayapp.mobilephone.clients.ebay.EbayClient;
import io.kirill.ebayapp.mobilephone.clients.telegram.TelegramClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

@ExtendWith(MockitoExtension.class)
class MobilePhoneServiceTest {

  @Mock
  EbayClient ebayClient;

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

  MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void getLatestFromEbay() {
    doAnswer(inv -> Flux.just(iphone6s))
        .when(ebayClient)
        .getPhonesListedInTheLastMinutes(anyInt());

    StepVerifier
        .create(mobilePhoneService.getLatestFromEbay(10))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(ebayClient).getPhonesListedInTheLastMinutes(10);
  }

  @Test
  void findResellPrice() {
    doAnswer(inv -> Mono.just(BigDecimal.valueOf(10.0)))
        .when(cexClient)
        .getAveragePrice(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .expectNextMatches(phone -> phone.getResellPrice().equals(BigDecimal.valueOf(10.0)) && phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(cexClient).getAveragePrice(iphone6s);
  }

  @Test
  void findResellPriceWithIncompleteDetails() {
    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s.withModel(null)))
        .expectNextMatches(phone -> phone.getResellPrice() == null)
        .verifyComplete();

    verify(cexClient, never()).getAveragePrice(iphone6s);
  }

  @Test
  void findResellPriceWhenNotFound() {
    doAnswer(inv -> Mono.empty())
        .when(cexClient)
        .getAveragePrice(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .expectNextMatches(phone -> phone.getResellPrice() == null && phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(cexClient).getAveragePrice(iphone6s);
  }

  @Test
  void informAboutThePhone() {
    doAnswer(inv -> Mono.empty())
        .when(telegramClient)
        .informAboutThePhone(any());

    StepVerifier
        .create(mobilePhoneService.informAboutThePhone(iphone6s))
        .verifyComplete();

    verify(telegramClient).informAboutThePhone(iphone6s);
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

    verify(mobilePhoneRepository).findAll(Sort.by(new Sort.Order(DESC, "datePosted")));
  }

  @Test
  void feedLatest() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneRepository)
        .findByDatePostedAfter(dateCaptor.capture());

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
}