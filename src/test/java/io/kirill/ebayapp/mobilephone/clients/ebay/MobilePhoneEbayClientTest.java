package io.kirill.ebayapp.mobilephone.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
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
    assertThat(params.getFirst("category_ids")).isEqualTo("9355");
    assertThat(params.getFirst("filter")).startsWith("conditionIds:%7B1000|1500|2000|2500|3000|4000|5000%7D,deliveryCountry:GB,price:[39..800],priceCurrency:GBP,itemLocationCountry:GB,buyingOptions:%7BFIXED_PRICE%7D,itemStartDate:[");
  }
}