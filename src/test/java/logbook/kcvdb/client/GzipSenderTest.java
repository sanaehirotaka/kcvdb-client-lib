package logbook.kcvdb.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

public class GzipSenderTest {

    /**
     * 再送信の回数と時間
     */
    @Test
    public void retryTest() {
        GzipSender sender = new GzipSender();
        // 1回目の送信失敗
        assertEquals(true, sender.retryHandler());
        // 1回目の再送信待ち時間
        assertEquals(1000, sender.retryInterval());
        // 2回目の送信失敗
        assertEquals(true, sender.retryHandler());
        // 2回目の再送信待ち時間
        assertEquals(21000, sender.retryInterval());
        // 3回目の送信失敗
        assertEquals(true, sender.retryHandler());
        // 3回目の再送信待ち時間
        assertEquals(41000, sender.retryInterval());
        // 4回目の送信失敗
        assertEquals(true, sender.retryHandler());
        // 4回目の再送信待ち時間
        assertEquals(61000, sender.retryInterval());
        // 5回目の送信失敗
        assertEquals(true, sender.retryHandler());
        // 5回目の再送信待ち時間
        assertEquals(81000, sender.retryInterval());
        // 5回目の再送信に失敗すると再送信しない
        assertEquals(false, sender.retryHandler());
    }

    /**
     * サーバーが返すHTTPステータスを500と仮定
     *
     * @throws Exception
     */
    @Test
    public void failureTest1() throws Exception {
        // サーバーが返すHTTPステータスを500と仮定したStatusLineをモック
        StatusLine mockStatusLine = mock(StatusLine.class);
        doReturn(500).when(mockStatusLine).getStatusCode();

        // HttpEntityとStatusLineのモックを返すHttpResponseをモック
        HttpResponse mockResponse = mock(CloseableHttpResponse.class);
        doReturn(mockStatusLine).when(mockResponse).getStatusLine();

        GzipSender sender = spy(new GzipSender());
        HttpClient client = spy(sender.client());
        doReturn(mockResponse).when(client).execute(anyObject());

        doReturn(client).when(sender).client();

        sender.add(this.get());
        sender.add(this.get());
        sender.send();

        assertEquals(0, sender.queue.size());

        verify(sender).failure();
    }

    /**
     * サーバーが例外を返す(UnknownHostException)
     *
     * @throws Exception
     */
    @Test
    public void failureTest2() throws Exception {
        GzipSender sender = spy(new GzipSender());
        HttpClient client = spy(sender.client());
        doThrow(new UnknownHostException()).when(client).execute(anyObject());

        doReturn(client).when(sender).client();

        sender.add(this.get());
        sender.add(this.get());
        sender.send();

        assertEquals(0, sender.queue.size());

        verify(sender).failure();
    }

    /**
     * サーバーが返すHTTPステータスを200と仮定
     *
     * @throws Exception
     */
    @Test
    public void successTest() throws Exception {
        // サーバーが返すHTTPステータスを200と仮定したStatusLineをモック
        StatusLine mockStatusLine = mock(StatusLine.class);
        doReturn(200).when(mockStatusLine).getStatusCode();

        // HttpEntityとStatusLineのモックを返すHttpResponseをモック
        HttpResponse mockResponse = mock(CloseableHttpResponse.class);
        doReturn(mockStatusLine).when(mockResponse).getStatusLine();

        GzipSender sender = spy(new GzipSender());
        HttpClient client = spy(sender.client());
        doReturn(mockResponse).when(client).execute(anyObject());

        doReturn(client).when(sender).client();

        sender.add(this.get());
        sender.add(this.get());
        sender.send();

        assertEquals(0, sender.queue.size());

        verify(sender).success();
    }

    /**
     * テスト用の送信データ
     *
     * @return ApiData
     */
    private ApiData get() {
        return ApiData.createBuilder()
                .setStatusCode(200)
                .setHttpDate("Sat, 07 May 2016 01:34:30 GMT")
                .setLocalTime(
                        ZonedDateTime.from(
                                DateTimeFormatter.RFC_1123_DATE_TIME.parse("Sat, 07 May 2016 01:34:37 GMT")))
                .setRequestBody("RequestBody")
                .setRequestUri("RequestUri")
                .setResponseBody("ResponseBody")
                .build();
    }
}
