package logbook.kcvdb.client;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

/**
 * KCVDBへ送信する抽象クラスです
 *
 */
public abstract class AbstractSender {

    /** セッションID */
    protected UUID sessionId = UUID.randomUUID();

    /** 送信クライアント */
    protected String agent = "logbook-kcvdb-client v1";

    /** 送信待ちキュー */
    protected Queue<ApiData> queue = new ArrayBlockingQueue<>(32);

    /** 送信リトライカウント */
    protected int failureCount = 0;

    /** 最大送信リトライ数 */
    protected int maxFailure = 5;

    /** 送信待ち時間 */
    protected Duration waitTime = Duration.ofSeconds(1);

    /** 送信失敗時の待ち時間 */
    protected Duration coolDownTime = Duration.ofSeconds(20);

    /** 最後に送信試行(成功失敗を問わない)した時間 */
    protected LocalDateTime sendTime = LocalDateTime.MIN;

    /**
     * 送信待ちキューにApiDataを追加します
     *
     * @param data 追加されるApiData
     */
    public void add(ApiData data) {
        if (!this.queue.offer(data)) {
            // キューがあふれた場合
            this.regenerateSession();
            this.queue.offer(data);
        }
    }

    /**
     * 接続設定
     *
     * @return 接続設定
     */
    protected RequestConfig config() {
        return RequestConfig.custom()
                .setSocketTimeout((int) Duration.ofSeconds(10).toMillis())
                .setConnectTimeout((int) Duration.ofSeconds(10).toMillis())
                .setConnectionRequestTimeout((int) Duration.ofSeconds(30).toMillis())
                .build();
    }

    /**
     * HTTPヘッダー
     *
     * @return HTTPヘッダー
     */
    protected List<Header> headers() {
        return Collections.emptyList();
    }

    /**
     * HttpClient
     *
     * @return HttpClient
     */
    protected HttpClient client() {
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(this.config())
                .setDefaultHeaders(this.headers())
                // IOException時に再試行処理を行う
                .setRetryHandler(this::retryHandler)
                // ステータスコードが200以外の場合に再試行処理を行う
                .setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy() {
                    @Override
                    public long getRetryInterval() {
                        return AbstractSender.this.retryInterval();
                    }

                    @Override
                    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                        // ステータスコードが2xx以外の場合にretryHandlerを呼び出す
                        int status = response.getStatusLine().getStatusCode();
                        return !isSuccess(status) && AbstractSender.this.retryHandler();
                    }
                })
                .build();

    }

    /**
     * リクエスト送信先URI
     *
     * @return URI
     */
    abstract protected String uri();

    /**
     * 送信メッセージ
     *
     * @return HttpEntity
     */
    abstract protected Optional<HttpEntity> httpEntity();

    /**
     * リクエストを送信する<br>
     * このメソッドを複数回呼び出す場合、前回のメソッド呼び出しが復帰してから呼び出す必要があります<br>
     * <strong>このメソッドはスレッドセーフではありません</strong>
     */
    public void send() {
        Optional<HttpEntity> entity = this.httpEntity();
        if (entity.isPresent()) {
            try {
                LocalDateTime execute = this.sendTime.plus(this.waitTime);
                LocalDateTime now = LocalDateTime.now();
                if (execute.compareTo(now) > 0) {
                    Duration wait = Duration.between(now, execute);
                    TimeUnit.MILLISECONDS.sleep(wait.toMillis());
                }

                this.failureCount = 0;
                HttpClient client = this.client();
                try {
                    HttpPost method = new HttpPost(this.uri());
                    method.setEntity(entity.get());

                    HttpResponse response = client.execute(method);
                    if (isSuccess(response.getStatusLine().getStatusCode())) {
                        this.success();
                    } else {
                        this.failure();
                    }
                } catch (Exception e) {
                    this.failure();
                }
                this.sendTime = LocalDateTime.now();
                HttpClientUtils.closeQuietly(client);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * 送信失敗時に再試行のために待機する時間
     *
     * @return 待機時間(ミリ秒)
     */
    protected long retryInterval() {
        long interval = this.waitTime.toMillis() + ((this.failureCount - 1) * this.coolDownTime.toMillis());
        return interval;
    }

    /**
     * 送信失敗時に再試行されるかどうかを判断する
     */
    protected boolean retryHandler() {
        this.failureCount++;
        return this.maxFailure >= this.failureCount;
    }

    /**
     * 送信成功時の動作
     */
    protected void success() {
    }

    /**
     * 送信失敗時の動作
     */
    protected void failure() {
        this.regenerateSession();
    }

    /**
     * セッションを再生成し送信待ちキューを空にします
     */
    public void regenerateSession() {
        this.sessionId = UUID.randomUUID();
        this.queue.clear();
    }

    private boolean retryHandler(IOException paramIOException, int paramInt, HttpContext paramHttpContext) {
        if (this.retryHandler()) {
            long wait = this.retryInterval();
            try {
                TimeUnit.MILLISECONDS.sleep(wait);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private static boolean isSuccess(int statusCode) {
        switch (statusCode) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_CREATED:
        case HttpStatus.SC_ACCEPTED:
        case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
        case HttpStatus.SC_NO_CONTENT:
        case HttpStatus.SC_RESET_CONTENT:
        case HttpStatus.SC_PARTIAL_CONTENT:
        case HttpStatus.SC_MULTI_STATUS:
            return true;
        default:
            return false;
        }
    }
}
