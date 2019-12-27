package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.common.clients.ebay.exceptions.EbayAuthError;
import io.kirill.ebayapp.common.clients.ebay.models.Seller;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import io.kirill.ebayapp.mobilephone.MobilePhone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MobilePhoneEbayClientTest {

  @Mock
  EbayAuthClient ebayAuthClient;

  @Mock
  EbaySearchClient ebaySearchClient;

  @Mock
  MobilePhoneMapper mobilePhoneMapper;

  @InjectMocks
  MobilePhoneEbayClient mobilePhoneEbayClient;

  @Captor
  ArgumentCaptor<MultiValueMap<String, String>> paramsCaptor;

  String accessToken = "access-token";

  @Test
  void getPhonesListedInTheLastMinutesWhenItemNotFound() {
    var searchResult = List.of(
        SearchResult.builder().itemId("item-6").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-6").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-7").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-8").seller(new Seller("s", 99.0, 3.99, "s")).build(),
        SearchResult.builder().itemId("item-9").seller(new Seller("s", 89.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-10").seller(null).build()
    );

    doAnswer(inv -> Mono.just(accessToken)).when(ebayAuthClient).accessToken();
    doAnswer(inv -> Flux.fromIterable(searchResult)).when(ebaySearchClient).search(anyString(), any());
    doAnswer(inv -> Mono.empty()).when(ebaySearchClient).getItem(anyString(), anyString());

    var mobilePhones = mobilePhoneEbayClient.getPhonesListedInTheLastMinutes(10);

    StepVerifier
        .create(mobilePhones)
        .verifyComplete();

    verify(ebayAuthClient, times(4)).accessToken();
    verify(ebaySearchClient).search(eq(accessToken), any());
    verify(ebaySearchClient, times(3)).getItem(anyString(), anyString());
    verify(mobilePhoneMapper, never()).map(any());
  }

  @Test
  void getPhonesEndingSoon() {
    var searchResult = List.of(
        SearchResult.builder().itemId("item-11").seller(new Seller("s", 99.0, 15.0, "s")).build()
    );

    doAnswer(inv -> Mono.just(accessToken)).when(ebayAuthClient).accessToken();
    doAnswer(inv -> Flux.fromIterable(searchResult)).when(ebaySearchClient).search(anyString(), paramsCaptor.capture());
    doAnswer(inv -> Mono.just(Item.builder().itemId(inv.getArgument(1)).build())).when(ebaySearchClient).getItem(anyString(), anyString());
    doAnswer(inv -> MobilePhone.builder().id(((Item)inv.getArgument(0)).getItemId()).build()).when(mobilePhoneMapper).map(any());

    var mobilePhones = mobilePhoneEbayClient.getPhonesEndingSoon(10);

    StepVerifier
        .create(mobilePhones)
        .expectNextMatches(mob -> mob.getId().equals("item-11"))
        .verifyComplete();

    verify(ebayAuthClient, times(2)).accessToken();
    verify(ebaySearchClient).search(eq(accessToken), any());
    verify(ebaySearchClient).getItem(accessToken, "item-11");

    var params = paramsCaptor.getValue();
    assertThat(params.getFirst("limit")).isEqualTo("200");
    assertThat(params.getFirst("category_ids")).isEqualTo("9355");
    assertThat(params.getFirst("filter")).startsWith("conditionIds:%7B1000|1500|2000|2500|3000|4000|5000%7D,deliveryCountry:GB,priceCurrency:GBP,itemLocationCountry:GB,price:[0..1800],buyingOptions:%7BAUCTION%7D,itemEndDate:[..");
  }

  @Test
  void getPhonesListedInTheLastMinutes() {
    var searchResult = List.of(
        SearchResult.builder().itemId("item-1").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-1").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-2").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-3").seller(new Seller("s", 99.0, 3.99, "s")).build(),
        SearchResult.builder().itemId("item-4").seller(new Seller("s", 89.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-5").seller(null).build()
    );

    doAnswer(inv -> Mono.just(accessToken)).when(ebayAuthClient).accessToken();
    doAnswer(inv -> Flux.fromIterable(searchResult)).when(ebaySearchClient).search(anyString(), paramsCaptor.capture());
    doAnswer(inv -> Mono.just(Item.builder().itemId(inv.getArgument(1)).build())).when(ebaySearchClient).getItem(anyString(), anyString());
    doAnswer(inv -> MobilePhone.builder().id(((Item)inv.getArgument(0)).getItemId()).build()).when(mobilePhoneMapper).map(any());

    var mobilePhones = mobilePhoneEbayClient.getPhonesListedInTheLastMinutes(10);

    StepVerifier
        .create(mobilePhones)
        .expectNextMatches(mob -> mob.getId().equals("item-1"))
        .expectNextMatches(mob -> mob.getId().equals("item-2"))
        .verifyComplete();

    verify(ebayAuthClient, times(3)).accessToken();
    verify(ebaySearchClient).search(eq(accessToken), any());
    verify(ebaySearchClient).getItem(accessToken, "item-1");
    verify(ebaySearchClient).getItem(accessToken, "item-2");

    var params = paramsCaptor.getValue();
    assertThat(params.getFirst("limit")).isEqualTo("200");
    assertThat(params.getFirst("category_ids")).isEqualTo("9355");
    assertThat(params.getFirst("filter")).startsWith("conditionIds:%7B1000|1500|2000|2500|3000|4000|5000%7D,deliveryCountry:GB,priceCurrency:GBP,itemLocationCountry:GB,price:[39..1800],buyingOptions:%7BFIXED_PRICE%7D,itemStartDate:[");
  }

  @Test
  void getPhonesListedInTheLastMinutesWhenError() {
    doAnswer(inv -> Mono.just(accessToken)).when(ebayAuthClient).accessToken();
    doAnswer(inv -> Flux.error(new EbayAuthError(HttpStatus.TOO_MANY_REQUESTS, "too many requests")))
        .when(ebaySearchClient).search(anyString(), any());

    var mobilePhones = mobilePhoneEbayClient.getPhonesListedInTheLastMinutes(10);

    StepVerifier
        .create(mobilePhones)
        .verifyErrorMatches(e -> e instanceof EbayAuthError && e.getMessage().equals("error authenticating with ebay: too many requests"));

    verify(ebayAuthClient).accessToken();
    verify(ebayAuthClient).switchAccount();
    verify(ebaySearchClient).search(eq(accessToken), any());
    verify(ebaySearchClient, never()).getItem(anyString(), anyString());
  }
}