package io.kirill.ebayapp.mobilephone;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/mobile-phones")
@RequiredArgsConstructor
public class MobilePhoneController {
  private final MobilePhoneService mobilePhoneService;

  @GetMapping
  public Flux<MobilePhone> getAll(@RequestParam Optional<Integer> limit) {
    return mobilePhoneService.getAll(limit.orElse(100));
  }
}
