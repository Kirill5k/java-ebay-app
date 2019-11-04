package io.kirill.ebayapp.mobilephone;

import java.time.Instant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {

  @Tailable
  Flux<MobilePhone> findByDatePostedAfter(Instant datePosted);
}
