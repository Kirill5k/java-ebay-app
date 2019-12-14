package io.kirill.ebayapp.mobilephone.clients.ebay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.kirill.ebayapp.common.clients.ebay.EbayAuthClient;
import io.kirill.ebayapp.common.clients.ebay.EbaySearchClient;
import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import io.kirill.ebayapp.common.clients.ebay.models.Seller;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.search.SearchResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EbayClientTest {

  @Mock
  EbayAuthClient ebayAuthClient;

  @Mock
  EbaySearchClient ebaySearchClient;

  @Mock
  ItemMapper itemMapper;

  @InjectMocks
  EbayClient ebayClient;

  @Captor
  ArgumentCaptor<Instant> instantCaptor;

  String accessToken = "access-token";

  @Test
  void getPhonesListedInTheLastMinutes() {
    var searchResult = List.of(
        SearchResult.builder().itemId("item-1").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-1").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-2").seller(new Seller("s", 99.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-3").seller(new Seller("s", 99.0, 9.99, "s")).build(),
        SearchResult.builder().itemId("item-4").seller(new Seller("s", 89.0, 15.0, "s")).build(),
        SearchResult.builder().itemId("item-5").seller(null).build()
    );

    doAnswer(inv -> Mono.just(accessToken)).when(ebayAuthClient).accessToken();
    doAnswer(inv -> Flux.fromIterable(searchResult)).when(ebaySearchClient).searchForNewestInCategoryFrom(anyString(), anyInt(), instantCaptor.capture());
    doAnswer(inv -> Mono.just(Item.builder().itemId(inv.getArgument(1)).build())).when(ebaySearchClient).getItem(anyString(), anyString());
    doAnswer(inv -> MobilePhone.builder().id(((Item)inv.getArgument(0)).getItemId()).build()).when(itemMapper).toMobilePhone(any());

    var mobilePhones = ebayClient.getPhonesListedInTheLastMinutes(10);

    StepVerifier
        .create(mobilePhones)
        .expectNextMatches(mob -> mob.getId().equals("item-1"))
        .expectNextMatches(mob -> mob.getId().equals("item-2"))
        .verifyComplete();

    verify(ebayAuthClient, times(3)).accessToken();
    verify(ebaySearchClient).searchForNewestInCategoryFrom(eq(accessToken), eq(9355), any());
    verify(ebaySearchClient).getItem(accessToken, "item-1");
    verify(ebaySearchClient).getItem(accessToken, "item-2");

    var startDate = instantCaptor.getValue();
    assertThat(startDate).isBetween(Instant.now().minusSeconds(10 * 60 + 5), Instant.now().plusSeconds(10 * 60 + 5));
  }
}