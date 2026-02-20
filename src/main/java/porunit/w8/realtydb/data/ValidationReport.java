package porunit.w8.realtydb.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidationReport {
  private boolean valid;
  private List<Issue> errors;
  private List<Issue> warnings;

  @Data @Builder
  public static class Issue {
    private String code;
    private String message;
    private String path;
  }
}