package io.kirill.ebayapp.mobilephone;

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
  public Flux<MobilePhone> getAll(@RequestParam Optional<Integer> limit, @RequestParam Optional<String> condition) {
    int lim = limit.orElse(100);
    return condition
        .map(cond -> mobilePhoneService.getLatestByCondition(cond, lim))
        .orElseGet(() -> mobilePhoneService.getLatest(lim));
  }

  @GetMapping(value = "/feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<MobilePhone> feed() {
    return mobilePhoneService.feedLatest().onErrorStop();
  }
}
