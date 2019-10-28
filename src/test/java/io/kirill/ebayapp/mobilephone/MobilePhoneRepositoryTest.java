package io.kirill.ebayapp.mobilephone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.test.StepVerifier;

@DataMongoTest
class MobilePhoneRepositoryTest {

  @Autowired
  private ReactiveMongoTemplate template;

  @Autowired
  private MobilePhoneRepository mobilePhoneRepository;

  @Test
  void save() {
    var mobilePhone = MobilePhone.builder().model("iphone 6s").build();
    var savedMobilePhone = mobilePhoneRepository.save(mobilePhone)
        .map(MobilePhone::getId)
        .flatMap(id -> template.findById(id, MobilePhone.class));

    StepVerifier
        .create(savedMobilePhone)
        .expectNextMatches(order -> order.getId() != null && order.getModel().equals("iphone 6s"))
        .verifyComplete();
  }
}