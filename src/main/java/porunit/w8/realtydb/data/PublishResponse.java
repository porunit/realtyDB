package porunit.w8.realtydb.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublishResponse {
  private boolean published;
  private String feedId;     // UUID
  private String url;        // public ссылка /feeds/yandex/{feedId}.xml
  private ValidationReport report;
}