package logbook.kcvdb.client;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * 検証DBへ送信する艦これAPI
 *
 */
public class ApiData {

    /** 艦これAPIの絶対URL */
    private final String requestUri;

    /** 艦これAPIのリクエストボディ */
    private final String requestValue;

    /** 艦これAPIのレスポンスボディ */
    private final String responseValue;

    /** 艦これAPIのレスポンスのステータスコードを表す数値 */
    private final int statusCode;

    /** 艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列 */
    private final String httpDate;

    /** 送信クライアントが艦これAPIを受信した日時 */
    private final ZonedDateTime localTime;

    private ApiData(String requestUri, String requestValue, String responseValue, int statusCode, String httpDate,
            ZonedDateTime localTime) {
        this.requestUri = Objects.requireNonNull(requestUri, "艦これAPIの絶対URL");
        this.requestValue = Objects.requireNonNull(requestValue, "艦これAPIのリクエストボディ");
        this.responseValue = Objects.requireNonNull(responseValue, "艦これAPIのレスポンスボディ");
        this.statusCode = statusCode;
        this.httpDate = Objects.requireNonNull(httpDate, "艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列");
        this.localTime = Objects.requireNonNull(localTime, "送信クライアントが艦これAPIを受信した日時");
    }

    /**
     * 艦これAPIの絶対URLを取得します。
     * @return 艦これAPIの絶対URL
     */
    public String getRequestUri() {
        return this.requestUri;
    }

    /**
     * 艦これAPIのリクエストボディを取得します。
     * @return 艦これAPIのリクエストボディ
     */
    public String getRequestValue() {
        return this.requestValue;
    }

    /**
     * 艦これAPIのレスポンスボディを取得します。
     * @return 艦これAPIのレスポンスボディ
     */
    public String getResponseValue() {
        return this.responseValue;
    }

    /**
     * 艦これAPIのレスポンスのステータスコードを表す数値を取得します。
     * @return 艦これAPIのレスポンスのステータスコードを表す数値
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * 艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列を取得します。
     * @return 艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列
     */
    public String getHttpDate() {
        return this.httpDate;
    }

    /**
     * 送信クライアントが艦これAPIを受信した日時を取得します。
     * @return 送信クライアントが艦これAPIを受信した日時
     */
    public ZonedDateTime getLocalTime() {
        return this.localTime;
    }

    /**
     * ApiDataBuilder を作成します
     * @return ApiDataBuilder
     */
    public static ApiDataBuilder createBuilder() {
        return new ApiDataBuilder();
    }

    public static class ApiDataBuilder {

        private String requestUri;

        private String requestValue;

        private String responseValue;

        private int statusCode;

        private String httpDate;

        private ZonedDateTime localTime;

        /**
         * 艦これAPIの絶対URLを設定します。
         * @param requestUri 艦これAPIの絶対URL
         */
        public ApiDataBuilder setRequestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        /**
         * 艦これAPIのリクエストボディを設定します。
         * @param requestValue 艦これAPIのリクエストボディ
         * @return
         */
        public ApiDataBuilder setRequestValue(String requestValue) {
            this.requestValue = requestValue;
            return this;
        }

        /**
         * 艦これAPIのレスポンスボディを設定します。
         * @param responseValue 艦これAPIのレスポンスボディ
         * @return
         */
        public ApiDataBuilder setResponseValue(String responseValue) {
            this.responseValue = responseValue;
            return this;
        }

        /**
         * 艦これAPIのレスポンスのステータスコードを表す数値を設定します。
         * @param statusCode 艦これAPIのレスポンスのステータスコードを表す数値
         * @return
         */
        public ApiDataBuilder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * 艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列を設定します。
         * @param httpDate 艦これAPIのレスポンスヘッダーのDateフィールドから得られる文字列
         * @return
         */
        public ApiDataBuilder setHttpDate(String httpDate) {
            this.httpDate = httpDate;
            return this;
        }

        /**
         * 送信クライアントが艦これAPIを受信した日時を設定します。
         * @param localTime 送信クライアントが艦これAPIを受信した日時
         * @return
         */
        public ApiDataBuilder setLocalTime(ZonedDateTime localTime) {
            this.localTime = localTime;
            return this;
        }

        /**
         * ApiDataを生成します
         *
         * @return ApiData
         */
        public ApiData build() {
            return new ApiData(this.requestUri, this.requestValue, this.responseValue, this.statusCode, this.httpDate,
                    this.localTime);
        }
    }
}
