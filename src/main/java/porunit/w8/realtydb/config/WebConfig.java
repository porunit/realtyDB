package porunit.w8.realtydb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

// WebConfig.java — раздаём файлы из файловой системы по /feeds/yandex/**
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${yandex.feed.storageDir}")
  private String storageDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path p = Paths.get(storageDir).toAbsolutePath().normalize();
    registry.addResourceHandler("/feeds/yandex/**")
            .addResourceLocations("file:" + p.toString() + "/")
            .setCachePeriod(3600);
  }
}
