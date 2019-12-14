package io.kirill.ebayapp.mobilephone;

import io.kirill.ebayapp.mobilephone.domain.MobilePhone;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Optional;

@RestController
@RequestMapping("/api/mobile-phones")
@RequiredArgsConstructor
public class MobilePhoneController {
  private final MobilePhoneService mobilePhoneService;

  @GetMapping
  public Flux<MobilePhone> getAll(@RequestParam Optional<Integer> limit) {
    return mobilePhoneService.getLatest(limit.orElse(100));
  }

  @GetMapping(value = "/feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<MobilePhone> feed() {
    return mobilePhoneService.feedLatest().onErrorStop();
  }
}
