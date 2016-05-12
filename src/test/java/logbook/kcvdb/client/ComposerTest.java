package logbook.kcvdb.client;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

public class ComposerTest {

    @Test
    public void testComposeMetaData() {
        UUID uuid = UUID.randomUUID();
        String agent = "test-client";

        String actual = Composer.composeMetaData(uuid, agent);

        String expected = new StringBuilder()
                .append("{")
                .append("\"SessionId\":")
                .append("\"").append(uuid.toString().toLowerCase()).append("\",")
                .append("\"AgentId\":")
                .append("\"").append(agent).append("\"")
                .append("}")
                .toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testComposeBody() throws IOException {

        // テストデータ
        List<ApiData> datas = Arrays.asList(
                ApiData.createBuilder()
                        .setStatusCode(200)
                        .setHttpDate("Sat, 07 May 2016 01:34:30 GMT")
                        .setLocalTime(
                                ZonedDateTime.from(
                                        DateTimeFormatter.RFC_1123_DATE_TIME.parse("Sat, 07 May 2016 01:34:37 GMT")))
                        .setRequestValue("RequestBody1")
                        .setRequestUri("RequestUri1")
                        .setResponseValue("ResponseBody1")
                        .build(),
                ApiData.createBuilder()
                        .setStatusCode(301)
                        .setHttpDate("Sat, 07 May 2016 01:37:50 GMT")
                        .setLocalTime(
                                ZonedDateTime.from(
                                        DateTimeFormatter.RFC_1123_DATE_TIME.parse("Sat, 07 May 2016 01:36:52 GMT")))
                        .setRequestValue("RequestBody2")
                        .setRequestUri("RequestUri2")
                        .setResponseValue("ResponseBody2")
                        .build());

        byte[] actual = Composer.composeBody(datas);

        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(actual))) {
            try (JsonReader reader = Json.createReader(in)) {
                JsonArray array = reader.readArray();

                assertEquals(datas.size(), array.size());

                for (int i = 0; i < datas.size(); i++) {
                    ApiData data = datas.get(i);
                    JsonObject obj = array.getJsonObject(i);

                    assertEquals(data.getStatusCode(), obj.getInt("StatusCode"));
                    assertEquals(data.getHttpDate(), obj.getString("HttpDate"));
                    assertEquals(data.getLocalTime(),
                            ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(obj.getString("LocalTime"))));
                    assertEquals(data.getRequestValue(), obj.getString("RequestBody"));
                    assertEquals(data.getRequestUri(), obj.getString("RequestUri"));
                    assertEquals(data.getResponseValue(), obj.getString("ResponseBody"));

                }
            }
        }
    }
}
