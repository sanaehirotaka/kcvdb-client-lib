package logbook.kcvdb.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * 送信データを構成する
 *
 */
class Composer {

    /**
     * metadata を構成します
     *
     * @param metadata メタデータ
     * @return 送信データの metadata
     */
    public static String composeMetaData(UUID uuid, String agent) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("SessionId", uuid.toString().toLowerCase());
        builder.add("AgentId", agent);
        return builder.build().toString();
    }

    /**
     * body を構成します
     *
     * @param datas APIデータ
     * @return 送信データの body
     */
    public static byte[] composeBody(Collection<ApiData> datas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gout = new GZIPOutputStream(out)) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (ApiData data : datas) {
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

                objectBuilder.add("RequestUri", data.getRequestUri());
                objectBuilder.add("RequestBody", data.getRequestBody());
                objectBuilder.add("ResponseBody", data.getResponseBody());
                objectBuilder.add("StatusCode", data.getStatusCode());
                objectBuilder.add("HttpDate", data.getHttpDate());
                objectBuilder.add("LocalTime", DateTimeFormatter.RFC_1123_DATE_TIME.format(data.getLocalTime()));

                arrayBuilder.add(objectBuilder.build());
            }
            try (JsonWriter writer = Json.createWriter(gout)) {
                writer.writeArray(arrayBuilder.build());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return out.toByteArray();
    }
}
