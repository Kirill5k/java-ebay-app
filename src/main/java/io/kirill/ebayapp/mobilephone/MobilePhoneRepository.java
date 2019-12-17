package io.kirill.ebayapp.mobilephone;

import java.time.Instant;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {

  @Tailable
  Flux<MobilePhone> findByListingDetailsDatePostedAfter(Instant datePosted);

  Mono<Boolean> existsByListingDetailsUrl(String url);

  Flux<MobilePhone> findAllByCondition(String condition, Sort sort);
}
