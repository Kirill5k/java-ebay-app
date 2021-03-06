package io.kirill.ebayapp.videogame.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.models.Price;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemProperty;
import io.kirill.ebayapp.common.clients.ebay.models.item.ItemSeller;
import io.kirill.ebayapp.common.clients.ebay.models.item.ShippingCost;
import io.kirill.ebayapp.common.clients.ebay.models.item.ShippingOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VideoGameMapperTest {

  VideoGameMapper videoGameMapper = new VideoGameMapper();

  @Test
  void mapPs4Game() {
    var item = Item.builder()
        .title("PS4-[/ Spider-manused.] * | Limited Edition - Remastered: Sony Playstation 4  Game new (PS4)")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of())
        .shippingOptions(List.of(
            new ShippingOption("DPD", "Express", new ShippingCost(BigDecimal.valueOf(100), "GBP")),
            new ShippingOption("Royal Mail 48", "Economy Delivery", new ShippingCost(BigDecimal.TEN, "GBP"))
        ))
        .build();


    var game = videoGameMapper.map(item);

    assertThat(game.getListingDetails().getPrice()).isEqualTo(BigDecimal.valueOf(19.99));
    assertThat(game.getName()).isEqualTo("Spider-man");
    assertThat(game.getPlatform()).isEqualTo("PS4");
  }

  @Test
  void mapSwitchGame() {
    var item = Item.builder()
        .title("Tom Clancy’s Tom Clancy's Pokémon Sword Switch")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of())
        .build();


    var game = videoGameMapper.map(item);

    assertThat(game.getName()).isEqualTo("Pokemon Sword");
    assertThat(game.getPlatform()).isEqualTo("SWITCH");
  }

  @Test
  void mapSwitchGameWithNameProperty() {
    var item = Item.builder()
        .title("Pokémon Sword Switch")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of(
            new ItemProperty("type", "Game Name", "Pokémon Sword")
        ))
        .build();


    var game = videoGameMapper.map(item);

    assertThat(game.getName()).isEqualTo("Pokemon Sword");
    assertThat(game.getPlatform()).isEqualTo("SWITCH");
  }

  @Test
  void mapGameUnusualTitle() {
    var item = Item.builder()
        .title("PS4 Pokémon Sword")
        .price(new Price(BigDecimal.valueOf(9.99), "GBP"))
        .seller(new ItemSeller("boris"))
        .localizedAspects(List.of())
        .build();


    var game = videoGameMapper.map(item);

    assertThat(game.getName()).isEqualTo("Pokemon Sword");
    assertThat(game.getPlatform()).isEqualTo("PS4");
  }
}