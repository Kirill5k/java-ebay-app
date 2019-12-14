package io.kirill.ebayapp;

import org.springframework.util.ResourceUtils;

import java.nio.file.Files;

public class TestUtils {
  private TestUtils() {}

  public static String getFileContent(String path) {
    try {
      var file = ResourceUtils.getFile(path);
      return new String(Files.readAllBytes(file.toPath()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
