package io.kirill.ebayapp.videogame.clients.ebay;

import io.kirill.ebayapp.common.clients.ebay.ItemMapper;
import io.kirill.ebayapp.common.clients.ebay.models.item.Item;
import io.kirill.ebayapp.videogame.VideoGame;
import org.springframework.stereotype.Component;

@Component
public class VideoGameMapper implements ItemMapper<VideoGame> {
  @Override
  public VideoGame map(Item item) {
    return null;
  }
}
