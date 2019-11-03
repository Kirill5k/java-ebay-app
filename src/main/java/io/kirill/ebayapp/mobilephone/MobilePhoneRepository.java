package io.kirill.ebayapp.mobilephone;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {

  @Tailable
  Flux<MobilePhone> findByDatePostedAfter(Instant datePosted);

  Mono<Boolean> existsByUrl(String url);
}
