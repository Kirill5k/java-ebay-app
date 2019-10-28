package io.kirill.ebayapp.mobilephone;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {
}
