package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.domain.ListingDetails;
import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@DataMongoTest
class MobilePhoneRepositoryTest {

  @Autowired
  private ReactiveMongoTemplate template;

  @Autowired
  private MobilePhoneRepository mobilePhoneRepository;

  @Test
  void save() {
    var mobilePhone = MobilePhoneBuilder.iphone6s().build();
    var savedMobilePhone = mobilePhoneRepository.save(mobilePhone)
        .map(MobilePhone::getId)
        .flatMap(id -> template.findById(id, MobilePhone.class));

    StepVerifier
        .create(savedMobilePhone)
        .expectNextMatches(order -> order.getId() != null && order.getModel().equals("Iphone 6s"))
        .verifyComplete();
  }

  @Test
  void findAll() {
    var mobilePhones = Flux.fromStream(IntStream.range(1, 11).boxed())
        .map(i -> MobilePhoneBuilder.iphone6s().model("iphone " + i).listingDetails(ListingDetails.builder().datePosted(Instant.now().minusSeconds(i * 100000)).build()).build())
        .flatMap(template::save)
        .thenMany(mobilePhoneRepository.findAll(Sort.by(new Sort.Order(DESC, "listingDetails.datePosted"))));

    StepVerifier
        .create(mobilePhones)
        .recordWith(ArrayList::new)
        .thenConsumeWhile($ -> true)
        .expectRecordedMatches(phones -> sortByDate(phones).equals(phones))
        .verifyComplete();
  }

  private Collection<MobilePhone> sortByDate(Collection<MobilePhone> phones) {
    return phones.stream()
        .sorted(Comparator.comparing((MobilePhone mp) -> mp.getListingDetails().getDatePosted()).reversed())
        .collect(toList());
  }
}