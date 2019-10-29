package io.kirill.ebayapp.mobilephone;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

interface MobilePhoneRepository extends ReactiveMongoRepository<MobilePhone, String> {
}
