package io.kirill.ebayapp.videogame.clients.ebay;

import static org.assertj.core.api.Assertions.assertThat;

import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemSeller;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class VideoGameMapperTest {

  VideoGameMapper videoGameMapper = new VideoGameMapper();

  @Test
  void map() {
    var item = Item.builder()
        .title("/ Spider-man | Limited Edition - Remastered: Sony Playstation 4 (PS4)")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of())
        .build();


    var game = videoGameMapper.map(item);

    assertThat(game.getName()).isEqualTo("Spider-man");
    assertThat(game.getPlatform()).isEqualTo("PS4");
  }
}