# kcvdb-client-lib

KCVDBへプレイデータを送信するライブラリ

## 使い方

### KCVDBへ送信するには

KCVDBへ送信するには`GzipSender`クラスを使用します。セッションの管理、失敗時のリトライ処理は自動で行われます。

(自動で行われるセッション管理は送信失敗時のセッション再生成のみです。ゲームの再ログイン等でのセッション再生成はライブラリを利用するロジックが責任を負います。)

	GzipSender sender = new GzipSender();
	sender.add(apidata);
	sender.send();

* `GzipSender#send()`メソッドはスレッドセーフではありません。同期化が必要になる可能性があります(`GzipSender#add(apidata)`メソッドはスレッドセーフです)。
* メッセージがキューイングされていない場合は`GzipSender#send()`メソッドを呼び出しても何もしません。
* `GzipSender#send()`メソッドを短期間のうちに連続して呼び出しても`AbstractSender#waitTime`より短い間隔で送信されることはありません(送信可能になるまでブロックされます)。
* 明示的にセッションを再生成する必要がある場合には`GzipSender`クラスインスタンスを生成しなおすか`AbstractSender#regenerateSession()` を呼び出してください。

キューイングによりメッセージの送信回数を減らすことが出来、同期化の手間を省けるため`GzipSender#send()`メソッドの呼び出しは`ScheduledExecutorService#scheduleWithFixedDelay`を利用してスケジュールする事をお勧めします。

	ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	service.scheduleWithFixedDelay(sender::send, 1, 5, TimeUnit.SECONDS);

