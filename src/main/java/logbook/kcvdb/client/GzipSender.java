package logbook.kcvdb.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class GzipSender extends AbstractSender {

    @Override
    protected String uri() {
        return "https://kancollevdataapi.azurewebsites.net/api/send/gzip";
    }

    @Override
    protected Optional<HttpEntity> httpEntity() {
        if (!this.queue.isEmpty()) {
            List<ApiData> datas = new ArrayList<>();
            ApiData data;
            while ((data = this.queue.poll()) != null) {
                datas.add(data);
            }
            HttpEntity entry = MultipartEntityBuilder.create()
                    .addTextBody("metadata", Composer.composeMetaData(this.sessionId, this.agent),
                            ContentType.create("text/plain", StandardCharsets.UTF_8))
                    .addBinaryBody("body", Composer.composeBody(datas))
                    .setBoundary(UUID.randomUUID().toString())
                    .build();
            return Optional.of(entry);
        }
        return Optional.empty();
    }
}
