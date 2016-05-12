package logbook.kcvdb.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;
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
                    .addTextBody("metadata", Composer.composeMetaData(this.sessionId, this.agent))
                    .addBinaryBody("body", Composer.composeBody(datas))
                    .build();
            return Optional.of(entry);
        }
        return Optional.empty();
    }
}
