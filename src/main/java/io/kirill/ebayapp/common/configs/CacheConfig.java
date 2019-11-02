package io.kirill.ebayapp.common.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Scheduled(cron = "0 0 0 1/3 * *")
  void evictAllCaches() {
    log.info("evicting all cache");
    cacheManager.getCacheNames().stream()
        .map(cacheManager::getCache)
        .forEach(Cache::clear);
  }
}
