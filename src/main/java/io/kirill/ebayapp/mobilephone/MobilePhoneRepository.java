package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

import java.time.Instant;

interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {

  @Tailable
  Flux<MobilePhone> findByListingDetailsDatePostedAfter(Instant datePosted);
}
