package porunit.w8.realtydb;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import porunit.w8.realtydb.data.PublishResponse;
import porunit.w8.realtydb.data.ValidationReport;
import porunit.w8.realtydb.service.YandexFeedService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds/yandex")
public class YandexFeedController {

  private final YandexFeedService service;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ValidationReport validateUpload(@RequestPart("file") MultipartFile file) throws IOException {
    return service.validate(new ByteArrayInputStream(file.getBytes()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/validate-by-url")
  public ValidationReport validateByUrl(@RequestParam String url) throws IOException {
    try (InputStream in = new URL(url).openStream()) {
      return service.validate(in);
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(
          value = "/validate",
          consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "application/*+xml" }
  )
  public ValidationReport validateRaw(@RequestBody byte[] body) throws IOException {
    return service.validate(new ByteArrayInputStream(body));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(
          value = "/publish",
          consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "application/*+xml" }
  )
  public PublishResponse publishRaw(@RequestBody byte[] body) throws IOException {
    return service.publish(new ByteArrayInputStream(body));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = "/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PublishResponse publish(@RequestPart("file") MultipartFile file) throws IOException {
    try (InputStream in = new ByteArrayInputStream(file.getBytes())) {
      return service.publish(in);
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/publish-by-url")
  public PublishResponse publishByUrl(@RequestParam String url) throws IOException {
    try (InputStream in = new URL(url).openStream()) {
      return service.publish(in);
    }
  }
}
