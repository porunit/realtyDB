// repo/projections/PhotoMetaView.java
package porunit.w8.realtydb.data;

import java.util.UUID;

public interface PhotoMetaView {
    UUID getId();
    String getFilename();
    String getContentType();
    long getSizeBytes();
    int getPosition();
    boolean isCover();
}
