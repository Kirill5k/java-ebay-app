package io.kirill.ebayapp.mobilephone;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  MobilePhone iphone6s = MobilePhoneBuilder.iphone6s().build();

  @Test
  void getLatestPhonesFromEbay() {
    doAnswer(inv -> Flux.just(iphone6s))
        .when(ebayClient)
        .getPhonesListedInTheLastMinutes(anyInt());

    doAnswer(inv -> Mono.just(inv.getArgument(0)))
        .when(mobilePhoneRepository)
        .save(any());

    StepVerifier
        .create(mobilePhoneService.getLatestPhonesFromEbay(10))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(ebayClient).getPhonesListedInTheLastMinutes(10);
    verify(mobilePhoneRepository).save(iphone6s);
  }

  @Test
  void findResellPrice() {
    doAnswer(inv -> Mono.just(BigDecimal.valueOf(10.0)))
        .when(cexClient)
        .getAveragePrice(any());

    doAnswer(inv -> Mono.just(inv.getArgument(0)))
        .when(mobilePhoneRepository)
        .save(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .expectNextMatches(phone -> phone.getResellPrice().equals(BigDecimal.valueOf(10.0)) && phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(cexClient).getAveragePrice(iphone6s);
    verify(mobilePhoneRepository).save(iphone6s.withResellPrice(BigDecimal.valueOf(10.0)));
  }

  @Test
  void findResellPriceWhenNoResult() {
    doAnswer(inv -> Mono.empty())
        .when(cexClient)
        .getAveragePrice(any());

    StepVerifier
        .create(mobilePhoneService.findResellPrice(iphone6s))
        .verifyComplete();

    verify(cexClient).getAveragePrice(iphone6s);
    verify(mobilePhoneRepository, never()).save(any());
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
  void getAll() {
    doAnswer(inv -> Flux.just(iphone6s, iphone6s, iphone6s))
        .when(mobilePhoneRepository)
        .findAll(any(Sort.class));

    StepVerifier
        .create(mobilePhoneService.getAll(2))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .expectNextMatches(phone -> phone.getModel().equals(iphone6s.getModel()))
        .verifyComplete();

    verify(mobilePhoneRepository).findAll(Sort.by(new Sort.Order(DESC, "datePosted")));
  }
}