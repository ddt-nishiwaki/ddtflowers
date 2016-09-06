#ストアドプロシージャ登録
#UPDATE文にはSELECT ROW_COUNT();を最後に入れないと更新件数が返らない？

#授業予約
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `book_classwork` $$
#授業予約のプロシージャの登録を行う
CREATE PROCEDURE `book_classwork`(
    IN in_default_user_classwork_cost int(11)
    ,IN in_default_flower_cost int(11)
    ,IN in_user_key int(11)
    ,IN in_classwork_key int(11)
    ,IN in_stage_key int(11)
    ,IN in_stage_no_present int(11)
    ,IN in_level_key int(11)
    ,IN in_level_no_present int(11)
    ,IN in_id int(11)
    )
#以降にストアドプロシージャの処理を記述する
BEGIN

#以下、3個の変数を用意する
#ユーザ側の受講情報の存在確認用。INSERTかUPDATEかの判定の値を格納する
DECLARE user_classwork_is_exists int(11);
#テーブル更新前の最新のレコードのタイムスタンプ。正常な更新の成否判定に使う
DECLARE latest_timestamp VARCHAR(25);
#UPDATE文による更新レコード数。UPDATEの成否判定に使う
DECLARE updated_count int(11);

#user_classworkテーブル内のレコードの中で最新の更新日付を取得する
#出力対象の列を指定する
SELECT
    #最新の更新日付を取得する
    MAX(update_datetime) AS latest
#データ取得元のテーブルを指定する
FROM
    #受講情報テーブル
    user_classwork
#SELECTに指定した値を変数に入れる
INTO latest_timestamp;

#既にキャンセルしたレコードがあるかを判定する
#出力対象の列を指定する
SELECT
    #レコード数を集計する
    COUNT(id) AS id
#データ取得元のテーブルを指定する
FROM
    #受講情報テーブル
    user_classwork
#受講情報テーブル
WHERE
    #引数で指定したユーザID
    user_key = in_user_key
#合致条件を追加指定する
AND
    #引数の授業ID
    classwork_key = in_classwork_key
INTO
    #指定したレコードが存在するかどうかの判定結果を格納する(0 or 1)
    user_classwork_is_exists;
    
#更新失敗時のロールバックを行うため、トランザクション処理を開始する
START TRANSACTION;

#予約レコードがあればupdate無ければinsertを行うため分岐する
IF user_classwork_is_exists = 0 THEN
#授業データを新規に追加する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO 
    #更新失敗時のロールバックを行うため、トランザクション処理を開始する
    user_classwork 
    (
        #受講状況
        user_work_status
        #受講料
        ,user_classwork_cost
        #花材費
        ,flower_cost
        #ユーザID
        ,user_key
        #授業ID
        ,classwork_key
        #ステージ情報ID
        ,stage_key
        #ステージ番号
        ,stage_no
        #レベル情報ID
        ,level_key
        #レベル番号
        ,level_no
        #受付日付
        ,order_datetime
        #レコード作成日付
        ,create_datetime
        #更新日付
        ,update_datetime
    )
#指定した列に対する値を以下に設定する 
VALUES (
    #1(予約受付)
    1
    #引数のデフォルト受講料
    ,in_default_user_classwork_cost
    #引数のデフォルト花材費
    ,in_default_flower_cost
    #引数のユーザID
    ,in_user_key
    #引数の授業ID
    ,in_classwork_key
    #引数のステージ情報ID
    ,in_stage_key
    #引数の現在のステージNo
    ,in_stage_no_present
    #引数のレベル情報ID
    ,in_level_key
    #引数の現在のレベルNo
    ,in_level_no_present
    #現在時刻
    ,NOW()
    #現在時刻
    ,NOW()
    #現在時刻
    ,NOW()
);

#既にレコードが存在する場合(キャンセルからの再予約)
ELSE
#既存の授業データをキャンセル状態から予約状態に変更する
#以下のテーブルのレコードを更新する
UPDATE
    #受講情報テーブル
    user_classwork
#更新対象の列と値を指定する
SET
    #1(予約受付)
    user_work_status = 1
    #引数のデフォルト花材費
    ,flower_cost = in_default_flower_cost
    #引数のデフォルト受講料
    ,user_classwork_cost = in_default_user_classwork_cost
    #引数のステージ情報ID
    ,stage_key = in_stage_key
    #引数の現在のステージNo
    ,stage_no = in_stage_no_present
    #引数のレベル情報ID
    ,level_key = in_level_key
    #引数の現在のレベルNo
    ,level_no = in_level_no_present
    #現在時刻
    ,update_datetime = NOW()
    #現在時刻
    ,order_datetime = NOW()
#検索条件を指定する
WHERE
    #引数で指定した受講IDを持つレコード
    id = in_id;
#分岐終了
END IF;

#正常なINSERT、UPDATEがなされたかを確認するために追加・更新を行ったレコード数を取得する
#出力対象の列を指定する
SELECT
    #レコード数を集計する
    COUNT(*)
#データ取得元のテーブルを指定する
FROM
    #受講情報テーブル
    user_classwork
#検索条件を指定する
WHERE
    #以前の最新のタイムスタンプより新しいレコードを取得対象にする
    update_datetime > latest_timestamp
#UPDATE対象の存在をカウントする
INTO updated_count; 

#更新対象が以前の最新のタイムスタンプより新しいものとなっていれば
IF updated_count = 1 THEN
#UPDATE、INSERTを確定する
    COMMIT;
#以前の最新のタイムスタンプより新しいレコードがない場合は失敗とみなす
ELSE
    #不正なUPDATE、INSERTが成立しないようにロールバックする
    ROLLBACK;
#分岐を終了する
END IF;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#授業予約キャンセル
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `cancel_classwork` $$
#授業予約キャンセルのプロシージャの登録を行う
CREATE PROCEDURE `cancel_classwork`(
    IN in_id int(11)
    ,in_classwork_key int(11)
    ,in_cancel_charge int(11)
	,in_cancel_user tinyint(4)
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#以下に変数を定義する
#テーブル内の最新の更新日付取得用。キャンセルが正しく行われたかを確認するために用意する
DECLARE latest_timestamp VARCHAR(25);
#更新レコードカウント用。同じくキャンセルが正しく行われたかを確認するためのもの
DECLARE updated_count int(11); 
#キャンセル要因をわけるための変数。ユーザからのキャンセルなら10、管理者からのキャンセルなら11が入る
DECLARE cancel_status tinyint(4);

#テーブル更新前の最新のタイムスタンプを取得し、UPDATE後に対象レコードのタイムスタンプと照らし合わせる
#出力対象の列を指定する
SELECT 
    MAX(update_datetime) AS latest
#データ取得元のテーブルを指定する
FROM
    #受講情報テーブル
    user_classwork
#取得したタイムスタンプを変数に保存する
INTO latest_timestamp;

#キャンセルユーザの値が0なら
IF in_cancel_user = 0 THEN
	#user_work_statusが10になるようにする
	SET cancel_status = 10;
#キャンセルユーザの値が1なら
ELSE 
	#user_work_statusが11になるようにする
	SET cancel_status = 11;
END IF;

#対象の受講情報データをキャンセル状態にする
#以下に指定したテーブルのレコードを更新する
UPDATE
    #受講情報テーブル
    user_classwork
#更新対象の列と値を指定する
SET
    #受講キャンセル状態の値をセットする
    user_work_status = cancel_status
    #現在時刻を更新時刻としてセットする
    ,update_datetime = NOW()
#検索条件を指定する
WHERE
    #プロシージャの引数で指定したIDのレコードを更新対象とする
    id = in_id;

#更新対象のレコードが正しく更新されているかを検証する
#出力対象の列を指定する
SELECT
    #正しく更新されたレコードの数を取得する
    COUNT(*)
#データ取得元のテーブルを指定する
FROM 
    #受講情報テーブル
    user_classwork
#検索条件を指定する
WHERE
    #以前の最新のタイムスタンプより新しいレコードを取得する(今回の更新対象のレコードが目的)
    update_datetime > latest_timestamp
#更新カウント数を変数に格納する
INTO updated_count; 

#正しく更新されていたら
IF updated_count = 1 THEN
#授業情報テーブルを今回の受講情報の更新に合わせて更新する 
#下記のテーブルのレコードを更新する
UPDATE 
    #授業情報テーブル
    classwork
#更新対象の列と値を指定する
SET
    #予約人数を1人減らす
    order_students = order_students-1
    #現在時刻で更新タイムスタンプを更新する
    ,update_datetime = NOW()
    #キャンセル料をレコードにセットする
    ,cancel_charge = in_cancel_charge
#検索条件を指定する
WHERE
    #更新対象の授業テーブルID
    id = in_classwork_key;
#テーブルの更新を確定する
COMMIT;

#正しく受講情報データが更新されていなかった場合
ELSE
#更新を無効にする
ROLLBACK;
#分岐終了
END IF;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#管理者画面 授業詳細タブ 日ごと予約一覧
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getAdminLessonList` $$
#日ごと予約一覧(管理者画面用)のプロシージャの登録を行う
CREATE PROCEDURE `getAdminLessonList`(
    OUT `result` TEXT
    ,IN `in_date` VARCHAR(14)
)
BEGIN

#指定した日付の授業データを取得する
#出力対象の列を指定する
SELECT
    #授業名
    lesson_name
    #授業マスタテーブルでの授業データID
    ,classwork.lesson_key AS lesson_key
    #授業開始時間
    ,start_time
    #授業終了時間
    ,end_time
    #授業日付
    ,time_table_day.lesson_date AS lesson_date
    #予約者数
    ,order_students
    #授業テーブルのレコードで設定されている最大受講者数
    ,classwork.max_students AS max_students
    #最小受講者数
    ,classwork.min_students AS min_students
    #デフォルトの最小受講者数
    ,lesson_inf.min_students AS default_min_students
    #時間帯単位の最大受講者数
    ,time_table_day.max_num AS max_num
    #時間帯単位の最小受講者数
    ,time_table_day.min_num AS min_num
    #授業データの状態
    ,classwork_status
    #本日の日付
    ,SUBSTRING(NOW(), 1,10) AS today
    #授業テーブル内での授業データのID
    ,classwork.id AS classwork_key
    #補足
    ,classwork_note
    #教室
    ,classroom
    #校舎ID
    ,school_inf.id AS school_key
    #校舎名
    ,school_name
    #時間帯テーブルのデータのID
    ,time_table_day.id AS time_table_day_key
    #時間帯授業情報テーブル内でのID
    ,timetable_inf.id AS timetable_key
#データ取得元のテーブルを指定する
FROM 
    #授業時間帯情報テーブル
    time_table_day
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業データテーブル
    classwork
#以下に指定した列を基に結合を行う
ON
    #IDをキーに結合する
    time_table_day.id = classwork.time_table_day_key
#合致条件を追加指定する
AND
    #指定した日時のレコードのみ抽出する
    time_table_day.lesson_date = in_date
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #授業詳細情報テーブル
    lesson_inf
#以下に指定した列を基に結合を行う
ON
    #IDが合致するデータのみ取得する
    lesson_inf.id = classwork.lesson_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #校舎情報テーブル
    school_inf 
#以下に指定した列を基に結合を行う
ON
    #IDが合致するデータのみ取得する
    school_inf.id = lesson_inf.school_key
#結合対象の列の値のnullを許可して結合する
RIGHT JOIN
    #授業時間帯詳細テーブル
    timetable_inf
#以下に指定した列を基に結合を行う
ON
    #IDをキーに結合する
    timetable_inf.id = time_table_day.timetable_key
;
    
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ブログ
#ブログ記事取得(全データ)
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getBlogArticle` $$
#ブログ記事取得(全データ)のプロシージャの登録を行う
CREATE PROCEDURE getBlogArticle(
        #結果セットを返す
        out result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#ブログ記事データを全て取得する
#出力対象の列を指定する
SELECT 
    #記事ID
    ub.id
    #記事画像1
    ,ub.image_1 AS image1
    #記事画像2
    ,ub.image_2 AS image2
    #記事画像3
    ,ub.image_3 AS image3
    #記事タイトル
    ,ub.title
    #投稿日付
    ,Date(ub.post_timestamp) AS date
    #投稿者名
    ,uin.user_name AS userName
    #記事本文
    ,ub.content AS text 
#データ取得元のテーブルを指定する
FROM 
    #ブログテーブル
    user_blog AS ub
    #ユーザ情報テーブル
    ,user_inf AS uin
#検索条件を指定する
WHERE
    #ユーザ情報とブログ記事が正しく紐づいたレコードのみ抽出する
    ub.user_key=uin.id
    #公開されている記事のみ取得する
    AND disclosure_range = 0
#ソートを行う
ORDER BY 
    #投稿日時が新しい順に並び替える
    post_timestamp DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#IDからブログ記事取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getBlogArticleWithId` $$
#IDからブログ記事を取得するプロシージャの登録を行う
CREATE PROCEDURE getBlogArticleWithId(
        #結果セットを返す
        OUT result text
        #入力引数
        #検索条件として使うユーザID
        ,IN userKey int
        #検索条件として使う記事ID
        ,articleId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #全列取得
    * 
#データ取得元のテーブルを指定する
FROM 
    #ブログテーブル
    user_blog 
#検索条件を指定する
WHERE 
    #指定したユーザID
    user_key = userKey
     #指定した記事ID
    AND id = articleId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ブログ記事作成
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `insertNewBlogArticle` $$
#ブログ記事作成のプロシージャの登録を行う
CREATE PROCEDURE insertNewBlogArticle(
        #ユーザID
        IN userKey int
        #記事タイトル
        ,blogTitle varchar(200)
        #記事本文
        ,blogContent text
        #記事公開設定
        ,blogPublication tinyint
        #記事画像1
        ,blogImage1 varchar(255)
        #記事画像2
        ,blogImage2 varchar(255)
        #記事画像3
        ,blogImage3 varchar(255)
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#ブログ記事データを新規に追加する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO
    #ブログテーブル
    user_blog(
        #ユーザID
        user_key
        #記事タイトル
        ,title
        #記事本文
        ,content
        #投稿日時
        ,post_timestamp
        #公開設定
        ,disclosure_range
        #記事画像1
        ,image_1
        #記事画像2
        ,image_2
        #記事画像3
        ,image_3
    )
#指定した列に対する値を以下に設定する 
VALUES (
        #指定したユーザID
        userKey
        #記事タイトル
        ,blogTitle
        #記事本文
        ,blogContent
        #現在時刻
        ,NOW() 
        #公開設定
        ,blogPublication
        #記事画像1
        ,blogImage1
        #記事画像2
        ,blogImage2
        #記事画像3
        ,blogImage3
    );
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ブログ記事更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateBlogArticle` $$
#ブログ記事更新のプロシージャの登録を行う
CREATE PROCEDURE updateBlogArticle
    (
        #記事ID
        IN blogId int
        #記事タイトル
        ,blogTitle varchar(200)
        #記事本文
        ,blogContent text
        #記事画像1
        ,blogImage1 varchar(255)
        #記事画像2
        ,blogImage2 varchar(255)
        #記事画像3
        ,blogImage3 varchar(255)
        #公開設定
        ,blogPublication tinyint
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#ブログテーブルを更新する
#指定したテーブルを更新する
UPDATE
    #　ブログ
    user_blog 
#更新対象の列と値を指定する
SET 
    #記事タイトル
    title = blogTitle
    #記事本文
    ,content = blogContent
    #記事画像1
    ,image_1 = blogImage1
    #記事画像2
    ,image_2 = blogImage2
    #記事画像3
    ,image_3 = blogImage3
    #公開設定
    ,disclosure_range = blogPublication 
#検索条件を指定する
WHERE
    #指定した記事IDの記事を対象にする
    id = blogId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ブログ記事削除
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `deleteBlogArticle` $$
#ブログ記事削除のプロシージャの登録を行う
CREATE PROCEDURE deleteBlogArticle(
        IN articleId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#指定した記事を削除する
#データ削除対象のテーブルを指定する
DELETE FROM
    #ブログテーブル
    user_blog 
#検索条件を指定する
WHERE
    #指定したIDの記事を削除する
    id = articleId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#マイブログ画面記事取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getMyBlogArticle` $$
#マイブログ画面記事取得のプロシージャの登録を行う
CREATE PROCEDURE getMyBlogArticle(
        OUT result text
        ,IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#指定したユーザの記事を取得する
#出力対象の列を指定する
SELECT 
    #記事ID
    ub.id
    #記事タイトル
    ,ub.title
    #投稿日付
    ,Date(ub.post_timestamp) AS date
    #投稿者名
    ,uin.user_name AS userName
    #記事画像1
    ,ub.image_1 AS image1
    #記事画像2
    ,ub.image_2 AS image2
    #記事画像3
    ,ub.image_3 AS image3
    #記事本文
    ,ub.content AS text
    #一覧表示時のボタン用列
    , '' AS buttons
#データ取得元のテーブルを指定する
FROM
    #ブログテーブル
    user_blog AS ub
    #ユーザ情報テーブル
    ,user_inf AS uin 
#検索条件を指定する
WHERE 
    #指定したユーザID(ブログテーブル側)
    ub.user_key = userKey
    #指定したユーザID(ユーザ情報テーブル側)
    AND ub.user_key=uin.id 
#ソートを行う
ORDER BY
    #投稿日付が新しい順に並べる
    post_timestamp DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#マイブログ画面記事一覧取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getMyBlogList` $$
#マイブログ画面記事一覧取得のプロシージャの登録を行う
CREATE PROCEDURE getMyBlogList(
        OUT result text
        ,IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#指定したユーザの記事一覧を取得する
#出力対象の列を指定する
SELECT
    #記事タイトル
    ub.id,ub.title
    #投稿日時
    ,Date(ub.post_timestamp) AS date
    #ユーザ名
    ,uin.user_name AS userName
    #記事画像1
    ,ub.image_1 AS image1
    #記事画像2
    ,ub.image_2 AS image2
    #記事画像3
    ,ub.image_3 AS image3
    #記事本文
    ,ub.content AS text,
    #一覧表示時のボタン用列
    '' AS buttons 
#データ取得元のテーブルを指定する
FROM
    #ブログテーブル
    user_blog AS ub
    #ユーザ情報テーブル
    ,user_inf AS uin 
#検索条件を指定する
WHERE 
    #指定したユーザIDのレコード(ブログテーブル側)
    ub.user_key=userKey
    #指定したユーザIDのレコード(ユーザ情報テーブル側)
    AND ub.user_key=uin.id
#ソートを行う
ORDER BY 
    #投稿日時が新しい順に並べる
    post_timestamp DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ギャラリー
#ギャラリー記事取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getGalleryContents` $$
#ギャラリー記事取得のプロシージャの登録を行う
CREATE PROCEDURE getGalleryContents(
        #結果セットを返す
        OUT result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #記事ID
    ui.id
    #記事ID
    ,ui.photo_title AS myPhotoImage
    #投稿日時
    ,Date(ui.update_timestamp) AS date
    #記事タイトル
    ,ui.article_title AS myPhotoTitle
    #ユーザ名
    ,uin.user_name AS myPhotoUser
    #記事のコメント
    ,ui.photo_summary AS myPhotoComment 
#データ取得元のテーブルを指定する
FROM
    #ギャラリーテーブル
    user_image AS ui
    #ユーザ情報テーブル
    ,user_inf AS uin 
#検索条件を指定する
WHERE
    #ユーザIDが合致する各テーブルを結合したレコード
    ui.user_key=uin.id 
#ソートを行う
ORDER BY
    #更新日時が新しい順に並べる
    ui.update_timestamp DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#マイギャラリー記事取得(全件)
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getMyGalleryContents1` $$
#マイギャラリー記事取得(全件)のプロシージャの登録を行う
CREATE PROCEDURE getMyGalleryContents1(
        #結果セットを出力する
        OUT result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #ユーザID
    ui.id
    #記事画像
    ,ui.photo_title AS myPhotoImage
    #投稿日時
    ,Date(ui.update_timestamp) AS date
    #記事タイトル
    ,ui.article_title AS myPhotoTitle
    #ユーザ名
    ,uin.user_name AS myPhotoUser
    #記事のコメント
    ,ui.photo_summary AS myPhotoComment 
#データ取得元のテーブルを指定する
FROM
    #ギャラリーテーブル
    user_image AS ui
    #ユーザID
    ,user_inf AS uin
#ソートを行う
ORDER BY
    #更新日時が新しい順に並べる
    ui.update_timestamp DESC
#最大取得記事数を300に制限する
LIMIT 300;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#マイギャラリー記事取得2
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getMyGalleryContents2` $$
#マイギャラリー記事取得(指定したユーザのみ)のプロシージャの登録を行う
CREATE PROCEDURE getMyGalleryContents2(
        OUT result text
        ,IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#ユーザを指定してギャラリー記事を取得する
#出力対象の列を指定する
SELECT
    #記事画像
    ui.id, ui.photo_title AS myPhotoImage
    #更新日付
    ,Date(ui.update_timestamp) AS date
    #記事タイトル
    ,ui.article_title AS myPhotoTitle
    #ユーザ名
    ,uin.user_name AS myPhotoUser
    #記事のコメント
    ,ui.photo_summary AS myPhotoComment 
#データ取得元のテーブルを指定する
FROM
    #ギャラリーテーブル
    user_image AS ui
    #ユーザ情報テーブル
    ,user_inf AS uin 
#検索条件を指定する
WHERE
    #ユーザIDが一致する記事を取得する
    ui.user_key = userKey
    #ユーザIDが一致するユーザ情報を取得する
    AND ui.user_key = uin.id 
#ソートを行う
ORDER BY
    #更新日付が新しい順に並べる
    ui.update_timestamp DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#マイギャラリー記事作成
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `insertGalleryContent` $$
#マイギャラリー記事作成のプロシージャの登録を行う
CREATE PROCEDURE insertGalleryContent(
        IN userKey int
        ,photoTitle varchar(200)
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#ギャラリー記事を追加する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO
    #ギャラリーテーブル
    user_image(
        #ユーザID 
        user_key
        #画像パス
        ,photo_title
        #更新日付
        ,update_timestamp
    ) 
    #指定した列に対する値を以下に設定する
    VALUES (
        #指定したユーザID
        userKey
        #指定した画像パス
        ,photoTitle
        #現在時刻
        ,NOW()
    )
    ;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;


#マイギャラリー記事更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateGalleryContent` $$
#マイギャラリー記事更新のプロシージャの登録を行う
CREATE PROCEDURE updateGalleryContent(
        IN photoSummary varchar(210)
        ,articleTitle varchar(100)
        ,articleId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#以下に指定したテーブルを更新する
UPDATE
    #ギャラリーテーブル
    user_image
#更新対象の列と値を指定する
SET
    #指定した記事コメント
    photo_summary = photoSummary
    #指定した記事タイトル
    ,article_title = articleTitle
#検索条件を指定する
WHERE
    #指定した記事ID
    id = articleId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;


#マイギャラリー記事削除
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `deleteGalleryContent` $$
#マイギャラリー記事削除のプロシージャの登録を行う
CREATE PROCEDURE deleteGalleryContent(
        IN articleId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#データ削除対象のテーブルを指定する
DELETE FROM
    #ギャラリーテーブル
    user_image 
#検索条件を指定する
WHERE
    #指定した記事ID
    id IN (articleId);
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#会員側トップ画面

#ユーザ情報取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_user_inf` $$
#ユーザ情報取得のプロシージャの登録を行う
CREATE PROCEDURE p_user_inf(
    IN in_user_key int(11)
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#ユーザIDを出力する
#出力対象の列を指定する
SELECT 
    *
#データ取得元のテーブルを指定する
FROM 
    user_inf
#検索条件を指定する
WHERE
    #ユーザID
    id = in_user_key
    ;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#お知らせ取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_message_inf` $$
#お知らせ取得のプロシージャの登録を行う
CREATE PROCEDURE p_message_inf(
    IN in_user_key int(11)
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#ユーザに対する最新のお知らせを取得する
#出力対象の列を指定する
SELECT
    #お知らせタイトル
    message_title
    #お知らせ本文
    ,message_content
    #送信日時
    ,send_date
#データ取得元のテーブルを指定する
FROM
    #お知らせ情報テーブル
    message_inf
#検索条件を指定する
WHERE
    #IDがお知らせ送信先テーブルにあるレコードのみ取得対象とする
    id IN (
        #出力対象の列を指定する
        SELECT
            #お知らせIDを取得する
            message_key
        #データ取得元のテーブルを指定する
        FROM
            #お知らせ送信先テーブル
            message_to
        #検索条件を指定する
        WHERE
            #引数のユーザID
            user_key = in_user_key
        #合致条件を追加指定する
        AND
            #未チェックのレコード
            check_datetime IS NULL
    )
#ソートを行う
ORDER BY
    #送信日時が新しい順に並べ替える
    send_date DESC;
#ストアドプロシージャの処理を終える
END $$

#受講可能レッスン一覧取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_booked_lessons` $$
#受講可能レッスン一覧取得のプロシージャの登録を行う
CREATE PROCEDURE p_booked_lessons(
    IN in_user_key int(11)
)
#以降にストアドプロシージャの処理を記述する
BEGIN
#ユーザの受講可能レッスン情報を取得する
#出力対象の列を指定する
SELECT
    #授業料
    user_classwork_cost
    #授業料調整額
    ,user_classwork_cost_aj
    #ポイント
    ,get_point
    #花材費
    ,flower_cost
    #花材費調整額
    ,flower_cost_aj
    #追加料金
    ,extension_cost
    #授業名
    ,lesson_name
    #校舎名
    ,school_name
    #開始時間
    ,start_time
    #終了時間
    ,end_time
    #授業日付
    ,lesson_date
    #本日の日付
    ,today
    #授業詳細情報ID
    ,lesson_key
    #受講情報ID
    ,id
    #最大受講者数
    ,biggest_students
    #ポイントレート
    ,MAX(point_rate) AS point_rate
    #ポイントレートに対応した受講者数
    ,students
    #受講者数
    ,order_students
    #予約状態
    ,user_work_status
#データ取得元のテーブルを指定する
FROM (
#出力対象の列を指定する
SELECT
    #受講料
    user_classwork_cost
    #受講料調整額
    ,user_classwork_cost_aj
    #ポイント
    ,user_classwork.get_point AS get_point
    #花材費
    ,flower_cost
    #花材費調整額
    ,flower_cost_aj
    #追加料金
    ,user_classwork.extension_cost AS extension_cost
    #授業名
    ,lesson_name
    #校舎名
    ,school_name
    #開始時間
    ,start_time
    #終了時管
    ,end_time
    #授業日付
    ,time_table_day.lesson_date AS lesson_date
    #授業のポイントレート
    ,lesson_point_rate.point_rate AS point_rate
    #ポイントレートに対応した人数
    ,students
    #受講者数
    ,order_students
    #今日の日付
    ,SUBSTRING(NOW(), 1,10) AS today
    #ステージ番号
    ,user_classwork.stage_no AS stage_no
    #レベル番号
    ,user_classwork.level_no AS level_no
    #授業詳細情報ID
    ,classwork.lesson_key AS lesson_key
    #受講情報ID
    ,user_classwork.id as id
    #最大受講者数
    ,biggest_students
    #受講状態
    ,user_work_status
    #授業情報ID
    ,classwork.id AS classwork_key
#データ取得元のテーブルを指定する
FROM
    #受講情報テーブル
    user_classwork
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業情報テーブル
    classwork
#以下に指定した列を基に結合を行う
ON
    #授業情報ID
    classwork.id = user_classwork.classwork_key
#合致条件を追加指定する
AND
    #引数のユーザID
    user_classwork.user_key = in_user_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    lesson_inf
#以下に指定した列を基に結合を行う
ON
    #授業情報詳細テーブルID
    lesson_inf.id = classwork.lesson_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #校舎情報テーブル
    school_inf
#以下に指定した列を基に結合を行う
ON
    #校舎情報ID
    school_inf.id = lesson_inf.school_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業時間帯テーブル
    time_table_day
#以下に指定した列を基に結合を行う
ON
    #授業時間帯ID
    time_table_day.id = classwork.time_table_day_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業時間帯情報テーブル
    timetable_inf
#以下に指定した列を基に結合を行う
ON
    #授業時間帯情報テーブルID
    timetable_inf.id = time_table_day.timetable_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    (
        #出力対象の列を指定する
        SELECT
            #ポイントレートが適用される受講者数
            MAX(students) AS biggest_students
            #授業詳細情報テーブルID
            ,lesson_key
        #データ取得元のテーブルを指定する
        FROM
            #ポイントレートテーブル
            lesson_point_rate
        #ソートを行う 
        GROUP BY
            #授業詳細情報テーブルID
            lesson_key    
    #別名を付けてテーブルと同等に扱う
    ) AS lsp
    #以下に指定した列を基に結合を行う
    ON
        #授業詳細情報テーブルID
        lsp.lesson_key = lesson_inf.id
    #合致条件を追加指定する
    AND
        #受講者数がポイントレートの適用人数を上回るレコード
        order_students > biggest_students
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ポイントレートテーブル
    lesson_point_rate
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    lesson_point_rate.lesson_key = lesson_inf.id
#合致条件を追加指定する
AND(
    #ポイントレートの適用対象となるレコード
    students <= order_students
    #又は
    OR    
        #ポイントレート適用最大人数が入力されていないレコード
        biggest_students IS NOT NULL
    )
#別名を付けて通常のテーブルと同等に扱う
) AS student_class_rec
#レコードを集約する
GROUP BY
    #IDごとに集約を行う
    id;
#ストアドプロシージャの処理を終える
END $$

#キャンセル料(率)取得
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_lesson_cancel_rate` $$
#キャンセル料(率)取得のプロシージャの登録を行う
CREATE PROCEDURE p_lesson_cancel_rate(
    IN in_lesson_key INT
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#キャンセル料(率)情報を取得する
#出力対象の列を指定する
SELECT
    #全指定
    *
#データ取得元のテーブルを指定する
FROM
    #授業キャンセル料(率)テーブル
    lesson_cancel_rate
#検索条件を指定する
WHERE
    #指定した授業詳細テーブルID
    lesson_key = in_lesson_key;

#ストアドプロシージャの処理を終える
END $$

#受講可能レッスン一覧取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_bookable_lessons` $$
#受講可能レッスン一覧取得のプロシージャの登録を行う
CREATE PROCEDURE p_bookable_lessons(
    IN in_user_key int(11)    
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#指定したユーザの受講可能レッスン一覧を取得する
#出力対象の列を指定する
SELECT 
    #授業名
    lesson_name
    #授業詳細テーブルID
    ,id AS lesson_key
#データ取得元のテーブルを指定する
FROM
    #授業詳細情報テーブル
    lesson_inf
#検索条件を指定する
WHERE
    #IDが以下に含まれるレコード
    id IN (
        #出力対象の列を指定する
        SELECT
            #授業詳細情報テーブルID
            lesson_key
        #データ取得元のテーブルを指定する
        FROM
            #受講状態テーブル
            user_lesson
        #検索条件を指定する
        WHERE
            #引数のユーザID
            user_key = in_user_key
        #合致条件を追加指定する
        AND
            #レコード状態が0
            rec_status = 0
    )
#合致条件を追加指定する
AND
    #レコード状態が0
    rec_status = 0;

#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#会員側日ごと授業一覧
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS p_user_classwork_a_day $$
#会員側日ごと授業一覧のプロシージャの登録を行う
CREATE PROCEDURE p_user_classwork_a_day(
    IN in_date INT
    ,in_user_key int(11)
)
#以降にストアドプロシージャの処理を記述する
BEGIN

#予約用にその日の授業一覧を取得する
#出力対象の列を指定する
SELECT 
    #受講料
    user_classwork_cost
    #受講料調整額
    ,user_classwork_cost_aj
    #花材費
    ,user_classwork.flower_cost AS flower_cost
    #花材費調整額
    ,flower_cost_aj
    #追加料金
    ,user_classwork.extension_cost AS extension_cost
    #ステージ番号
    ,uc_stage_inf.stage_no AS stage_no
    #レベル番号
    ,uc_lesson_sub.level_no AS level_no
    #授業名
    ,lesson_name
    #授業詳細情報テーブルID
    ,classwork.lesson_key AS lesson_key
    #キャンセル可能日数
    ,pre_order_days
    #キャンセル日
    ,stop_order_date
    #校舎名
    ,school_name
    #開始時間
    ,start_time
    #終了時間
    ,end_time
    #授業日時
    ,time_table_day.lesson_date AS lesson_date
    #予約人数
    ,order_students
    #最大予約人数
    ,classwork.max_students AS max_students
    #最小予約人数
    ,classwork.min_students AS min_students
    #デフォルトの最小予約人数
    ,lesson_inf.min_students AS default_min_students
    #時間帯の最大予約者数
    ,time_table_day.max_num AS max_num
    #時間帯の最少予約者数
    ,time_table_day.min_num AS min_num
    #デフォルトの花材費
    ,ul_lesson_sub.flower_cost AS default_flower_cost
    #デフォルトの受講料
    ,ul_lesson_sub.level_price AS default_user_classwork_cost
    #レベル番号
    ,ul_lesson_sub.level_no AS level_no_present
    #授業状態
    ,classwork_status
    #受講状態
    ,user_work_status
    #ステージ番号
    ,ul_stage_inf.stage_no AS stage_no_present
    #現在日付
    ,SUBSTRING(NOW(), 1,10) AS today
    #受講情報テーブルID
    ,user_classwork.id AS id
    #授業情報テーブルID
    ,classwork.id AS classwork_key
    #授業副情報テーブルID
    ,ul_lesson_sub.id AS level_key
    #ステージ情報テーブルID
    ,ul_stage_inf.id AS stage_key 
#データ取得元のテーブルを指定する
FROM 
    #授業時間帯テーブル
    time_table_day
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業情報テーブル
    classwork
#以下に指定した列を基に結合を行う
ON
    #時間帯授業情報テーブルID
    time_table_day.id = classwork.time_table_day_key
#合致条件を追加指定する
AND
    #指定した授業日時
    time_table_day.lesson_date = in_date
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #受講情報テーブル
    user_classwork
#以下に指定した列を基に結合を行う
ON
    #授業情報テーブルID
    classwork.id = user_classwork.classwork_key
#合致条件を追加指定する
AND    
    #指定したユーザID
    user_classwork.user_key = in_user_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    lesson_inf
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    lesson_inf.id = classwork.lesson_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #校舎情報テーブル
    school_inf
#以下に指定した列を基に結合を行う
ON
    #校舎情報テーブルID
    school_inf.id = lesson_inf.school_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業時間帯詳細情報テーブル
    timetable_inf
#以下に指定した列を基に結合を行う
ON
    #授業時間帯詳細情報テーブルID
    timetable_inf.id = time_table_day.timetable_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #受講状態テーブル
    user_lesson
#以下に指定した列を基に結合を行う
ON
    #指定したユーザID
    user_lesson.user_key = in_user_key
#合致条件を追加指定する
AND
    #授業詳細情報テーブルID
    user_lesson.lesson_key = lesson_inf.id
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #授業副情報テーブル
    lesson_sub AS ul_lesson_sub
#以下に指定した列を基に結合を行う
ON
    #授業副情報テーブルID
    ul_lesson_sub.id = user_lesson.level_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #ステージ情報テーブル
    stage_inf AS ul_stage_inf
#以下に指定した列を基に結合を行う
ON
    #ステージ情報テーブルID
    ul_stage_inf.id = user_lesson.stage_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #授業副情報テーブル
    lesson_sub AS uc_lesson_sub
#以下に指定した列を基に結合を行う
ON
    #授業副情報テーブルID
    uc_lesson_sub.id = user_classwork.level_key
#結合対象の列の値のnullを許可して結合する
LEFT JOIN
    #ステージ情報テーブル
    stage_inf AS uc_stage_inf
#以下に指定した列を基に結合を行う
ON
    #ステージ情報テーブルID
    uc_stage_inf.id = user_classwork.stage_key
;

#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

    
#パスワード変更
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateUserPassword` $$
#パスワード変更のプロシージャの登録を行う
CREATE PROCEDURE updateUserPassword
    (
        IN newPassword varchar(255)
        ,userId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#パスワードの更新を行う
#以下に指定したテーブルを更新する
UPDATE
    user_inf 
#更新対象の列と値を指定する
SET 
    #パスワードを入れ替える
    password = newPassword
    #更新時刻を現在時刻に設定する
    ,update_datetime = NOW() 
#検索条件を指定する
WHERE 
    #指定したユーザIDのレコードを更新対象にする
    id = userId
;

#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ユーザプロフィール取得
#プロフィール取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getUserProfile` $$
#プロフィール取得のプロシージャの登録を行う
CREATE PROCEDURE getUserProfile
    (
        OUT result text
        ,IN userId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN

#指定したユーザの情報を取得する
#出力対象の列を指定する
SELECT 
    #ユーザ名
    user_name
    #ユーザ名(カナ)
    ,name_kana
    #性別
    ,user_sex
    #誕生日
    ,birthday_date
    #郵便番号
    ,zip_code
    #住所
    ,address
    #メールアドレス
    ,mail_address
    #電話番号
    ,telephone
    #緊急用電話番号
    ,telephone2
    #メルマガ受信設定
    ,mail_deny 
#データ取得元のテーブルを指定する
FROM 
    #ユーザ情報テーブル
    user_inf 
#検索条件を指定する
WHERE
    #指定したIDユーザ情報を取得する
    id = userId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#ユーザプロフィール変更
#プロフィール更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateUserProfile` $$
#プロフィール更新のプロシージャの登録を行う
CREATE PROCEDURE updateUserProfile
    (
        IN userName varchar(40)
        ,nameKana varchar(40)
        ,zipCode varchar(8)
        ,userAddress varchar(255)
        ,userSex tinyint
        ,birthdayDate date
        ,userTelephone1 varchar(20)
        ,userTelephone2 varchar(20)
        ,userMailAddress varchar(255)
        ,mailDeny int
        ,userId int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#プロフィールの更新を行う
#以下に指定したテーブルの情報を更新する 
UPDATE
    user_inf 
#更新対象の列と値を指定する
SET 
    #ユーザ名
    user_name = userName
    #ユーザ名(カナ)
    ,name_kana = nameKana
    #郵便番号
    ,zip_code = zipCode
    #住所
    ,address = userAddress
    #性別
    ,user_sex = userSex
    #誕生日
    ,birthday_date = birthdayDate
    #電話番号
    ,telephone = userTelephone1
    #緊急連絡先
    ,telephone2 = userTelephone2
    #メールアドレス
    ,mail_address = userMailAddress
    #メルマガ受信設定
    ,mail_deny = mailDeny
    #レコードの更新日時を現在時刻に設定する
    ,update_datetime=NOW() 
#検索条件を指定する
WHERE
    #指定したユーザID
    id = userId;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#会員一覧
#会員一覧
# ユーザ情報(自分)
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getSelfUserInfo` $$
#ユーザ情報(自分)のプロシージャの登録を行う
CREATE PROCEDURE getSelfUserInfo
    (
        OUT result text
        ,IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#指定したユーザIDのユーザ情報を取得する
#出力対象の列を指定する
SELECT 
    #全指定
    *
#データ取得元のテーブルを指定する
FROM
    #ユーザ情報テーブル
    user_inf
#検索条件を指定する
WHERE
    #指定したユーザID
    id = userKey
;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# ユーザ情報一覧
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getUserInfoList` $$
#ユーザ情報一覧のプロシージャの登録を行う
CREATE PROCEDURE getUserInfoList
    (
        OUT result text
        ,IN sortTarget varchar(30)
        ,sortOrder tinyint
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#昇順ソート指定なら
IF sortOrder = 0 THEN
    #ユーザ一覧を取得する
    #出力対象の列を指定する
    SELECT 
        #全指定
        *
    #データ取得元のテーブルを指定する
    FROM
        #ユーザ情報テーブル
        user_inf
    #ソートを行う
    ORDER BY 
        #引数で指定した列を昇順でソートする
        sortTarget ASC
    ;
#孝順ソート指定なら
ELSE
    #ユーザ一覧を取得する
    #出力対象の列を指定する
    SELECT 
        #全指定
        *
    #データ取得元のテーブルを指定する
    FROM
        #ユーザ情報テーブル
        user_inf
    #ソートを行う
    ORDER BY 
        #指定した列で降順ソートを行う
        sortTarget DESC
    ;
#分岐終了
END IF;

#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# テーマ指定用リスト作成
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getListForChooseThemes` $$
#テーマ指定用リスト作成のプロシージャの登録を行う
CREATE PROCEDURE getListForChooseThemes
    (
        #結果セットを返す
        OUT result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#テーマとマスタテーブルでのIDを取得する
#出力対象の列を指定する
SELECT
    #授業詳細情報テーブルID
    id AS lesson_key
    #授業名
    ,lesson_name
#データ取得元のテーブルを指定する
FROM
    #授業詳細情報テーブル
    lesson_inf
#検索条件を指定する
WHERE
    #有効なレコードを対象とする
    rec_status = 0
#合致条件を追加指定する
AND
    #新宿校のもののみ取得する
    school_key = 1
;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#会員トップ画面のお知らせ
#お知らせ取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getUserMessage` $$
#お知らせ取得のプロシージャの登録を行う
CREATE PROCEDURE getUserMessage
    (
        OUT result text
        ,IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN

#対象のユーザの最新のお知らせを取得する
#出力対象の列を指定する
SELECT
    #お知らせタイトル
    message_title
    #お知らせ本文
    ,message_content
    #配信日付
    ,send_date 
#データ取得元のテーブルを指定する
FROM 
    #お知らせ情報テーブル
    message_inf 
#検索条件を指定する
WHERE
    #対象のIDを持ったレコードを取得する
    id IN 
    (
    #取得対象のお知らせのIDを取得する
    #出力対象の列を指定する
    SELECT 
        message_key 
    #データ取得元のテーブルを指定する
    FROM
        #お知らせ送信先テーブル
        message_to 
    #検索条件を指定する
    WHERE 
        #指定したユーザID
        user_key = userKey
        #未チェックのお知らせを取得する 
        AND check_datetime IS NULL
    ) 
    #ソートを行う
    ORDER BY 
        #配信日付が新しい順に並べる
        send_date DESC
        #更に、IDが大きい順に並べる。send_dataが日付までしかないので同一の日付内での最新を特定するために必要
        ,id DESC;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#お知らせ登録1(お知らせ情報)
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `insertMessageInfo` $$
#お知らせ登録1(お知らせ情報)のプロシージャの登録を行う
CREATE PROCEDURE insertMessageInfo
    (
        IN messageTitle varchar(100)
        ,messageContent text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#新たにお知らせ情報を登録する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO
    #お知らせ情報テーブル
    message_inf(
        #配信日時
        send_date
        #お知らせタイプ
        ,message_type
        #お知らせタイトル
        ,message_title
        #お知らせ本文
        ,message_content
        #作成日時
        ,create_datetime
        #更新日時
        ,update_datetime
    ) 
    #指定した列に対する値を以下に設定する 
    VALUES
    (
        #今日の日付
        DATE(NOW())
        #0(当該列は機能していない)
        ,0
        #指定したお知らせタイトル
        ,messageTitle
        #指定した本文
        ,messageContent
        #現在時刻
        ,NOW()
        #現在時刻
        ,NOW()
    );
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#お知らせ登録2(送信先)
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `insertMessageTo` $$
#お知らせ登録2(送信先)のプロシージャの登録を行う
CREATE PROCEDURE insertMessageTo
    (
        IN userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#指定したテーブルにレコードを追加する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO message_to
    (
        #お知らせ情報テーブルID
        message_key
        #ユーザID
        ,user_key
        #作成日時
        ,create_datetime
        #更新日時
        ,update_datetime
    ) 
    #指定した列に対する値を以下に設定する 
    VALUES
    (
        (
        #お知らせ情報と送信先のデータを紐づけるため、お知らせ情報のIDを取得する
        #出力対象の列を指定する
        SELECT
            #お知らせ情報テーブルID
            id
        #データ取得元のテーブルを指定する
        FROM
            #お知らせ情報テーブル
            message_inf 
        #ソートを行う
        ORDER BY 
            #作成日時。作成されたばかりのお知らせ情報を取得する
            create_datetime DESC
        #最新1件のみ取得
        LIMIT 1
        )
        #指定したユーザID
        ,userKey
        #現在時刻
        ,NOW()
        #現在時刻
        ,NOW()
    );
#ストアドプロシージャの処理を終える
END $$
    
#受講承認
#受講承認
#受講承認対象の一覧取得
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getLecturePermit` $$
#受講承認対象の一覧取得のプロシージャの登録を行う
CREATE PROCEDURE  `getLecturePermit`(OUT `result` TEXT)
#以降にストアドプロシージャの処理を記述する
BEGIN
#本日の受講承認対象の一覧を取得する
#出力対象の列を指定する
SELECT
    #授業時間帯テーブルID
    time_table_day.id AS time_table_key
    #授業日付
    ,time_table_day.lesson_date AS lesson_date
    #受講者数
    ,classwork.order_students AS order_students
    #授業詳細テーブルID
    ,classwork.lesson_key AS lesson_key
    #授業開始時間
    ,start_time 
    #授業終了時間
    ,end_time 
    #ユーザ名
    ,user_name
    #ステージ番号
    ,user_classwork.stage_no AS stage_no
    #レベル番号
    ,user_classwork.level_no AS level_no
    #受講情報テーブルID
    ,user_classwork.id AS user_classwork_key
    #ユーザID
    ,user_inf.id AS user_key
    #所持ポイント
    ,user_inf.get_point AS get_point
    #授業名
    ,lesson_name
    #利用ポイント用列
    ,'' AS use_point
    #受講料
    ,user_classwork_cost
    #校舎テーブルID
    ,timetable_inf.school_key AS school_key
#データ取得元のテーブルを指定する
FROM
    #授業時間帯テーブル
    time_table_day 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業情報テーブル
    classwork 
#以下に指定した列を基に結合を行う
ON
    #授業情報テーブルID
    time_table_day.id = classwork.time_table_day_key 
#合致条件を追加指定する
AND
    #授業日付
    time_table_day.lesson_date = SUBSTRING(NOW(), 1, 10) 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN 
    #受講情報テーブル
    user_classwork 
#以下に指定した列を基に結合を行う
ON
    #授業情報テーブルID
    classwork.id = user_classwork.classwork_key
#結合対象列にnullが入っている列を排除して結合を行う
INNER JOIN user_inf 
#以下に指定した列を基に結合を行う
ON
    #ユーザ情報テーブルID
    user_inf.id = user_classwork.user_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    lesson_inf 
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    lesson_inf.id = classwork.lesson_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業時間帯詳細情報テーブル
    timetable_inf 
#以下に指定した列を基に結合を行う
ON
    #授業時間帯詳細情報テーブル
    timetable_inf.id = time_table_day.timetable_key 
#合致条件を追加指定する
AND
    #受講状態が「受付」になっているレコードのみ取り出す
    user_classwork.user_work_status = 2
;
#ストアドプロシージャの処理を終える
END $$

#受講承認対象の一覧取得(個人ごと)
DROP PROCEDURE IF EXISTS getLecturePermitIndivisual $$
CREATE PROCEDURE  getLecturePermitIndivisual(
    IN in_user_key INT
    ,OUT result TEXT
)
BEGIN

SELECT
    time_table_day.id AS time_table_key
    ,time_table_day.lesson_date AS lesson_date
    ,classwork.order_students AS order_students
    ,classwork.lesson_key AS lesson_key
    ,start_time 
    ,end_time 
    ,user_name
    ,user_classwork.stage_no AS stage_no
    ,user_classwork.level_no AS level_no
    ,user_classwork.id AS user_classwork_key
    ,user_inf.id AS user_key
    ,user_inf.get_point AS get_point
    ,lesson_name
    ,'' AS use_point
    ,level_price AS user_classwork_cost
    ,timetable_inf.school_key AS school_key
FROM
    user_inf
INNER JOIN
    user_classwork
ON
    user_classwork.user_key = user_inf.id
AND
    user_inf.id = in_user_key
INNER JOIN
    classwork
ON
    classwork.id = user_classwork.classwork_key
INNER JOIN 
    lesson_inf
ON
    lesson_inf.id = classwork.lesson_key
INNER JOIN
    time_table_day
ON
    time_table_day.id = classwork.time_table_day_key 
AND
    time_table_day.lesson_date < current_date
INNER JOIN
    timetable_inf 
ON
    timetable_inf.id = time_table_day.timetable_key 
INNER JOIN
    stage_inf
ON
    stage_inf.lesson_key = classwork.lesson_key
AND
    stage_inf.stage_no = user_classwork.stage_no
INNER JOIN
    lesson_sub
ON
    lesson_sub.stage_key = stage_inf.id
AND
    lesson_sub.level_no = user_classwork.level_no;

END$$

#ポイントレート算出
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getPointRate` $$
#授業のポイントレート算出用プロシージャの登録を行う
CREATE PROCEDURE getPointRate
    (
        OUT result text
        ,IN lessonKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #ポイントレート
    point_rate
    #ポイントレートの対象となる予約者数
    ,students
#データ取得元のテーブルを指定する
FROM
    #ポイントレートテーブル
    lesson_point_rate
#検索条件を指定する
WHERE
    #授業詳細情報テーブルID
    lesson_point_rate.lesson_key = lessonKey
;
#ストアドプロシージャの処理を終える
END $$

# 商品購入用商品名リスト用
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getCommodityNameList` $$
#商品購入用商品名リスト取得のプロシージャの登録を行う
CREATE PROCEDURE getCommodityNameList(out result text)
#以降にストアドプロシージャの処理を記述する
BEGIN
#商品名リストのデータを取得する
#出力対象の列を指定する
SELECT
    #商品名
    commodity_name
    #販売価格
    ,selling_price
    #商品マスタテーブルID
    ,id AS commodity_key 
#データ取得元のテーブルを指定する
FROM
    #商品マスタテーブル
    commodity_inf;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#受講承認更新
#受講情報の更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `doLecturePermit` $$
#受講情報の更新処理のプロシージャの登録を行う
CREATE PROCEDURE doLecturePermit
    (
        IN userClassworkCost int
        ,getPoint int
        ,classworkUsePoint int
        ,lateTime int
        ,payPrice int
        ,userClassworkKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#受講情報テーブルを更新し、対象レコードを受講状態にする
#以下に指定したテーブルのレコードを更新する
UPDATE
    #受講情報テーブル
    user_classwork
#更新対象の列と値を指定する
SET
    #授業料
    user_classwork_cost = userClassworkCost
    #利用ポイント
    ,use_point = getPoint
    #所持ポイント
    ,get_point = classworkUsePoint
    #遅刻時間
    ,late_time = lateTime
    #更新時刻に現在時刻をセットする
    ,update_datetime = NOW()
    #承認時刻に現在時刻をセットする
    ,receipt_datetime = NOW()
    #受講状態を「受講」にする
    ,user_work_status = 3
    #支払金額
    ,pay_price = payPrice
#検索条件を指定する
WHERE
    #指定した受講情報テーブルID
    id = userClassworkKey;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ; 

#獲得ポイント更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitGetPoint` $$
#獲得ポイント更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitGetPoint
    (
        IN getPoint int
        ,userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#ユーザの所持ポイントを更新する
#以下に指定したテーブルを更新する
UPDATE
    user_inf
    #更新対象の列と値を指定する
SET
    #取得ポイントを加算する
    get_point = get_point + getPoint
    #更新時刻に現在時刻をセットする
    ,update_datetime = NOW()
#検索条件を指定する
WHERE
    #指定したユーザID
    id = userKey
    ;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#使用ポイント更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitUsePoint` $$
#使用ポイント更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitUsePoint
    (
        IN usePoint int
        ,userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN

#ユーザの獲得ポイントを使用しただけ減らす
#以下に指定したテーブルを更新する
UPDATE    
        user_inf
#更新対象の列と値を指定する
SET
    #所持ポイントを減算する
     get_point = get_point - usePoint
     #通算使用ポイントを加算する
    ,use_point = use_point + usePoint
    #更新時刻を現在時刻にセットする
    ,update_datetime = NOW()
#検索条件を指定する
WHERE
    #指定したユーザID
    id = userKey
    ;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# ポイントの更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitPoints` $$
#ポイントの更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitPoints
    (
        IN getPoint int
        ,usePoint int
        ,userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#獲得ポイントがあれば
IF getPoint <> 0 THEN
    #ユーザの獲得ポイントを更新する
    CALL updateLecturePermitGetPoint(getPoint, userKey);
#分岐終了
END IF;

#使用ポイントがあれば
IF commodityUsePoint <> 0 THEN
    #ユーザの使用ポイントを更新する
    CALL updateLecturePermitUsePoint(usePoint, userKey);
#分岐終了
END IF;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# 商品代情報の更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `insertSellCommodity` $$
#商品代情報の更新のプロシージャの登録を行う
CREATE PROCEDURE insertSellCommodity
    (
        IN sellNumber int
        ,payCash int
        ,commodityUsePoint int
        ,commodityContent text
        ,userKey int
        ,schoolKey int 
        ,commodityKey int
        ,getPoint int
    )
    
#以降にストアドプロシージャの処理を記述する
BEGIN
    
#商品購入情報を追加する
#以下に列挙した列の値を指定してテーブルにレコードを追加する
INSERT INTO commodity_sell(
    #販売時刻
    sell_datetime
    #販売個数
    ,sell_number
    #支払額
    ,pay_cash
    #使用ポイント
    ,use_point
    #商品名
    ,content
    #ユーザID
    ,user_key
    #校舎情報テーブルID
    ,school_key
    #商品マスタテーブルID
    ,commodity_key
    #作成時刻
    ,create_datetime
    #更新時刻
    ,update_datetime
)
#指定した列に対する値を以下に設定する 
VALUES (
    #現在時刻
    NOW()
    #入力された販売個数
    ,sellNumber
    #入力された支払額
    ,payCash
    #入力された商品購入の使用ポイント
    ,commodityUsePoint
    #入力された購入商品名
    ,commodityContent
    #入力されたユーザID
    ,userKey
    #入力された校舎ID
    ,schoolKey
    #入力された商品マスタテーブルID
    ,commodityKey
    #作成時刻に現在時刻をセットする
    ,NOW()
    #更新時刻に現在時刻をセットする
    ,NOW()
);

#ポイントを反映する
CALL updateLecturePermitPoints(getPoint, commodityUsePoint, userKey);    
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#受講承認一覧

#受講承認一覧
#受講承認一覧のデータ取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getLecturePermitInfoList` $$
#受講承認一覧のデータ取得のプロシージャの登録を行う
CREATE PROCEDURE getLecturePermitInfoList
    (
        OUT result text
        ,IN fromDate date
        ,toDate date
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #受講情報テーブルのID
    user_classwork.id AS id
    #ユーザ名
    ,user_name
    #授業名
    ,lesson_name
    #受講料
    ,user_classwork.user_classwork_cost AS cost
    #使用ポイント
    ,user_classwork.use_point AS use_point
    #ステージ番号
    ,stage_inf.stage_no AS stage_no
    #レベル番号
    ,lesson_sub.level_no AS level_no
    #販売個数(デフォルトの1)
    ,1 AS sell_number 
    #テーブル出力時の追加欄用列
    ,'' AS content
    #ユーザID
    ,user_inf.id AS user_key
    #校舎情報テーブルID 
    ,lesson_inf.school_key AS school_key
    #商品情報マスタテーブルIDを隠して入れておくための列用
    , '' AS commodity_key 
    #現在の所持ポイント
    ,user_inf.get_point AS get_point 
#データ取得元のテーブルを指定する
FROM 
    #受講情報テーブル
    user_classwork 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業情報テーブル
    classwork 
#以下に指定した列を基に結合を行う
ON
    #授業情報テーブルID
    classwork.id = user_classwork.classwork_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業時間帯情報テーブル
    time_table_day 
#以下に指定した列を基に結合を行う
ON
    #授業時間帯情報テーブルID
    time_table_day.id = classwork.time_table_day_key
    #指定した期間(開始日付)
    AND time_table_day.lesson_date <= toDate
    #指定した期間(終了日付)
    AND time_table_day.lesson_date >= fromDate
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ユーザ情報テーブル
    user_inf
#以下に指定した列を基に結合を行う
ON
    #ユーザID
    user_inf.id = user_classwork.user_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    lesson_inf
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    lesson_inf.id = classwork.lesson_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ステージ情報
    stage_inf
#以下に指定した列を基に結合を行う
ON
    #ステージ情報テーブルID
    stage_inf.id = user_classwork.stage_key 
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #レッスン副情報テーブル
    lesson_sub
#以下に指定した列を基に結合を行う
ON
    #授業副情報テーブルID
    lesson_sub.id = user_classwork.level_key
#ソートを行う
ORDER BY
    #授業日付が新しい順番に並べる
    time_table_day.lesson_date DESC
;
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

#使用ポイントの更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitListPoint` $$
#受講承認一覧での使用ポイントの更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitListPoint
    (
        IN diffPoint int
        ,userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#以下のテーブルを更新する
UPDATE
    #ユーザ情報
    user_inf
#更新対象の列と値を指定する
SET
    #使用ポイントを加算する
    use_point = use_point + diffPoint
    #所持ポイントを減算する
    ,get_point = get_point - diffPoint
#検索条件を指定する
WHERE
    #指定したユーザID
    id = userKey;    
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# 受講情報の更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitListClasswork` $$
#受講承認一覧での受講情報の更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitListClasswork
    (
        IN userClassworkCost int
        ,usePoint int
        ,classworkId int
        ,diffPoint int
        ,userKey int
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#授業データの更新
#以下に指定したテーブルのレコードを更新する
UPDATE
    #受講情報テーブル
    user_classwork
#更新対象の列と値を指定する
SET
    #受講料
    user_classwork_cost = userClassworkCost
    #使用ポイント
    ,use_point = usePoint
    #更新時間を更新する
    ,update_datetime = NOW()
#検索条件を指定する
WHERE
    #指定した授業情報テーブルID
    id = classworkId
;
#受講承認における使用ポイントを更新する
CALL updateLecturePermitListPoint(diffPoint, userKey);
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;

# 商品代の更新
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `updateLecturePermitListCommodity` $$
#受講承認一覧での商品代の更新処理のプロシージャの登録を行う
CREATE PROCEDURE updateLecturePermitListCommodity(IN userClassworkCost int ,commodityKey int,usePoint int,commoditySellKey int,diff_point int,userKey int)
#以降にストアドプロシージャの処理を記述する
BEGIN
#以下のテーブルを更新する
UPDATE
    #商品売り上げ情報テーブル
    commodity_sell
#更新対象の列と値を指定する
SET
    #指定した支払額
    pay_cash = userClassworkCost
    #商品情報ID
    ,commodity_key=commodityKey
    #使用ポイント
    ,use_point = usePoint
    #現在時刻
    ,update_datetime = NOW()
#検索条件を指定する
WHERE
    #商品売り上げ情報テーブルID
    id = commoditySellKey
;
#受講承認での使用ポイントを更新する
CALL updateLecturePermitListPoint(@result, diffPoint, userKey);
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す
delimiter ;


#日ごと予約者一覧画面
#日ごと予約者一覧取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getEachDayLessonList` $$
#日ごと予約者一覧取得のためのプロシージャの登録を行う
CREATE PROCEDURE getEachDayLessonList
    (
        OUT result text
        ,IN date date
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
#出力対象の列を指定する
SELECT
    #授業時間帯情報テーブルID
    time_table_day.id AS time_table_key
    #授業日付
    ,time_table_day.lesson_date AS lesson_date
    #開始時間
    ,start_time
    #終了時管
    ,end_time
    #授業名
    ,lesson_name
    #ユーザ名
    ,user_name
    #ステージ番号
    ,stage_inf.stage_no
    #レベル番号
    ,lesson_sub.level_no
    #受講状態
    ,user_classwork.user_work_status AS user_work_status
    #ユーザID
    ,user_inf.id AS user_key
#データ取得元のテーブルを指定する
FROM
    #授業時間帯情報テーブル
    time_table_day
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業情報テーブル
    classwork
#以下に指定した列を基に結合を行う
ON
    #授業時間帯情報テーブルID
    time_table_day.id = classwork.time_table_day_key
#合致条件を追加指定する
AND
    #授業日付
    time_table_day.lesson_date = date
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #受講情報テーブル
    user_classwork
#以下に指定した列を基に結合を行う
ON
    #授業情報テーブルID
    classwork.id = user_classwork.classwork_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ユーザ情報テーブル
    user_inf
#以下に指定した列を基に結合を行う
ON
    #ユーザID
    user_inf.id = user_classwork.user_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    lesson_inf
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    lesson_inf.id = classwork.lesson_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業詳細情報テーブル
    timetable_inf
#以下に指定した列を基に結合を行う
ON
    #授業詳細情報テーブルID
    timetable_inf.id = time_table_day.timetable_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ステージ情報テーブル
    stage_inf
#以下に指定した列を基に結合を行う
ON
    #ステージ情報テーブルID
    stage_inf.id = user_classwork.stage_key
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #授業副情報テーブル
    lesson_sub
#以下に指定した列を基に結合を行う
ON
    #授業副情報テーブルID
    lesson_sub.id = user_classwork.level_key
;    
#ストアドプロシージャの処理を終える
END $$
#区切り文字をセミコロンに戻す

#受講一覧-商品の更新
DROP PROCEDURE IF EXISTS p_update_approval_list_purchase $$
CREATE PROCEDURE p_update_approval_list_purchase (
    IN in_sell_number INT
    ,IN in_pay_cash INT
    ,IN in_use_point INT
    ,IN in_commodity_sell_key INT
    ,IN in_user_key INT
    ,IN in_diff_point INT
    ,IN in_commodity_key INT
    ,OUT result INT
)
BEGIN

DECLARE latest_timestamp VARCHAR(25);
DECLARE updated_timestamp VARCHAR(25);
DECLARE latest_timestamp_user VARCHAR(25);
DECLARE updated_timestamp_user VARCHAR(25);
DECLARE diff_point int(11);

SELECT 
    MAX(update_datetime)
FROM
    commodity_sell
INTO
    latest_timestamp;

START TRANSACTION;

UPDATE
    commodity_sell
SET
    pay_cash = in_pay_cash
    ,use_point = in_use_point
    ,update_datetime = NOW()
    ,commodity_key = in_commodity_key
WHERE
    id = in_commodity_sell_key;

SELECT 
    MAX(update_datetime)
FROM
    commodity_sell
INTO
    updated_timestamp;

IF latest_timestamp < updated_timestamp THEN
    IF in_diff_point = 0 THEN
        SELECT 1 INTO result;
        COMMIT;
    ELSE
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            latest_timestamp_user;
            
        UPDATE
            user_inf
        SET
            get_point = get_point + in_diff_point
            ,update_datetime = NOW()
        WHERE
            id = in_user_key;
        
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            updated_timestamp_user;
        
        IF latest_timestamp_user < updated_timestamp_user THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            SELECT 0 INTO result;
            ROLLBACK;
        END IF;
    END IF;
ELSE 
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

#受講一覧-授業の更新
DROP PROCEDURE IF EXISTS p_update_approval_list_lesson $$
CREATE PROCEDURE p_update_approval_list_lesson (
    IN in_user_classwork_cost INT
    ,IN in_use_point INT
    ,IN in_diff_point INT
    ,IN in_user_key INT
    ,IN in_user_classwork_key INT
    ,OUT result INT
)
BEGIN

DECLARE latest_timestamp VARCHAR(25);
DECLARE updated_timestamp VARCHAR(25);
DECLARE latest_timestamp_user VARCHAR(25);
DECLARE updated_timestamp_user VARCHAR(25);

SELECT 
    MAX(update_datetime)
FROM
    user_classwork
INTO
    latest_timestamp;

START TRANSACTION;

UPDATE
    user_classwork
SET
    user_classwork_cost = in_user_classwork_cost
    ,use_point = in_use_point
    ,update_datetime = NOW()
WHERE
    id = in_user_classwork_key;

SELECT 
    MAX(update_datetime)
FROM
    user_classwork
INTO
    updated_timestamp;

IF latest_timestamp < updated_timestamp THEN
    IF in_diff_point = 0 THEN
        SELECT 1 INTO result;
        COMMIT;
    ELSE
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            latest_timestamp_user;
            
        UPDATE
            user_inf
        SET
            get_point = get_point + in_diff_point
            ,update_datetime = NOW()
        WHERE
            id = in_user_key;
        
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            updated_timestamp_user;
        
        IF latest_timestamp_user < updated_timestamp_user THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            SELECT 0 INTO result;
            ROLLBACK;
        END IF;
    END IF;
ELSE 
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

#受講承認-授業の更新
DROP PROCEDURE IF EXISTS p_update_approval_lesson $$
CREATE PROCEDURE p_update_approval_lesson (
    IN in_user_classwork_cost INT
    ,IN in_use_point INT
    ,IN in_get_point INT
    ,IN in_pay_price INT
    ,IN in_user_key INT
    ,IN in_user_classwork_key INT
    ,OUT result INT
)
BEGIN

DECLARE latest_timestamp VARCHAR(25);
DECLARE updated_timestamp VARCHAR(25);
DECLARE latest_timestamp_user VARCHAR(25);
DECLARE updated_timestamp_user VARCHAR(25);
DECLARE diff_point int(11);

SELECT 
    MAX(update_datetime)
FROM
    user_classwork
INTO
    latest_timestamp;

START TRANSACTION;

UPDATE
    user_classwork
SET
    user_classwork_cost = in_user_classwork_cost
    ,use_point = in_use_point
    ,get_point = in_get_point
    ,update_datetime = NOW()
    ,user_work_status = 3
    ,pay_price = in_pay_price
WHERE
    id = in_user_classwork_key;

SELECT 
    MAX(update_datetime) AS latest
FROM
    user_classwork
INTO
    updated_timestamp;

IF latest_timestamp < updated_timestamp THEN
    SELECT
        in_get_point - in_use_point
    INTO
        diff_point;
    SELECT
        MAX(update_datetime)
    FROM
        user_inf
    INTO
        latest_timestamp_user;
    IF diff_point = 0 THEN
        IF in_get_point = 0 THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            UPDATE
                user_inf
            SET
                use_point = use_point + in_use_point
                ,update_datetime = NOW()
            WHERE
                id = in_user_key;
            SELECT
                MAX(update_datetime) AS latest
            FROM
                user_inf
            INTO
                updated_timestamp_user;
            IF latest_timestamp_user < updated_timestamp_user THEN
                COMMIT;
            ELSE
                SELECT 0 INTO result;
                ROLLBACK;
            END IF;
        END IF;
    ELSE
        UPDATE
            user_inf
        SET
            use_point = use_point + in_use_point
            ,get_point = get_point + diff_point
            ,update_datetime = NOW()
        WHERE
            id = in_user_key;
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            updated_timestamp_user;
        IF latest_timestamp_user < updated_timestamp_user THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            SELECT 0 INTO result;
            ROLLBACK;
        END IF;
    END IF;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

#受講承認-商品の更新
DROP PROCEDURE IF EXISTS p_update_approval_purchase $$
CREATE PROCEDURE p_update_approval_purchase (
    IN in_id INT
    ,IN in_sell_number INT
    ,IN in_pay_cash INT
    ,IN in_use_point INT
    ,IN in_commodity_key INT
    ,IN in_purchase_status INT
    ,IN in_user_key INT
    ,OUT result INT
)
BEGIN

DECLARE latest_timestamp DATETIME;
DECLARE updated_timestamp DATETIME;
DECLARE latest_timestamp_user DATETIME;
DECLARE updated_timestamp_user DATETIME;

SELECT 
    MAX(update_datetime)
FROM
    commodity_sell
INTO
    latest_timestamp;

START TRANSACTION;

UPDATE
    commodity_sell
SET
    sell_number = in_sell_number
    ,pay_cash = in_pay_cash
    ,use_point = in_use_point
    ,commodity_key = in_commodity_key
    ,purchase_status = in_purchase_status
    ,update_datetime = NOW()
WHERE
    id = in_id;

SELECT 
    MAX(update_datetime)
FROM
    commodity_sell
INTO
    updated_timestamp;

IF latest_timestamp < updated_timestamp THEN
    IF in_purchase_status > 0 THEN
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            latest_timestamp_user;

        UPDATE
            user_inf
        SET
            use_point = use_point + in_use_point
            ,get_point = get_point - in_use_point
            ,update_datetime = NOW()
        WHERE
            id = in_user_key;

        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            updated_timestamp_user;

        IF latest_timestamp_user < updated_timestamp_user THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            SELECT 0 INTO result;
            ROLLBACK;
        END IF;
    ELSE
        SELECT 1 INTO result;
        COMMIT;
    END IF;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

DROP PROCEDURE IF EXISTS p_insert_approval_purchase $$
CREATE PROCEDURE p_insert_approval_purchase (
    IN in_sell_number INT
    ,IN in_pay_cash INT
    ,IN in_use_point INT
    ,IN in_user_key INT
    ,IN in_purchase_status INT
    ,OUT result INT
)
BEGIN

DECLARE old_count int;
DECLARE new_count int;
DECLARE latest_timestamp_user VARCHAR(25);
DECLARE updated_timestamp_user VARCHAR(25);
DECLARE last_purchase_id int;

SELECT 
    COUNT(id)
FROM
    commodity_sell
INTO
    old_count;

START TRANSACTION;

SELECT
    commodity_key
FROM
    commodity_sell
WHERE
    create_datetime = (
        SELECT
            MAX(create_datetime)
        FROM
            commodity_sell
    )
LIMIT 1
INTO last_purchase_id;

INSERT INTO
    commodity_sell (
        commodity_key
        ,school_key
        ,user_key
        ,purchase_status
        ,content
        ,sell_datetime
        ,sell_number
        ,pay_cash
        ,use_point
        ,create_datetime
        ,update_datetime
    )
VALUES (
    last_purchase_id
    ,1
    ,in_user_key
    ,in_purchase_status
    ,''
    ,NOW()
    ,in_sell_number
    ,in_pay_cash
    ,in_use_point
    ,NOW()
    ,NOW()
);

SELECT
    COUNT(id)
FROM
    commodity_sell
INTO
    new_count;

IF old_count < new_count THEN
    IF in_purchase_status > 0 THEN
        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            latest_timestamp_user;

        UPDATE
            user_inf
        SET
            use_point = use_point + in_use_point
            ,get_point = get_point - in_use_point
            ,update_datetime = NOW()
        WHERE
            id = in_user_key;

        SELECT
            MAX(update_datetime)
        FROM
            user_inf
        INTO
            updated_timestamp_user;

        IF latest_timestamp_user < updated_timestamp_user THEN
            SELECT 1 INTO result;
            COMMIT;
        ELSE
            SELECT 0 INTO result;
            ROLLBACK;
        END IF;
    ELSE
        SELECT 1 INTO result;
        COMMIT;
    END IF;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

SELECT ROW_COUNT();

END$$

DROP PROCEDURE IF EXISTS p_delete_approval_purchase $$
CREATE PROCEDURE p_delete_approval_purchase (
    IN in_id INT
    ,OUT result INT
)
BEGIN
    
DECLARE old_count int;
DECLARE new_count int;

SELECT 
    COUNT(id)
FROM
    commodity_sell
INTO
    old_count;

START TRANSACTION;

DELETE
FROM
    commodity_sell
WHERE
    id = in_id;

SELECT
    COUNT(id)
FROM
    commodity_sell
INTO
    new_count;

IF old_count > new_count THEN
    SELECT 1 INTO result;
    COMMIT;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

#商品購入承認一覧のデータ取得
#コード記述のため区切り文字を一時的に変更する
DELIMITER $$
#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `getSellCommodityPermitInfoList` $$
#商品購入承認一覧のデータ取得のプロシージャの登録を行う
CREATE PROCEDURE `getSellCommodityPermitInfoList`(OUT `result` TEXT, IN `fromDate` VARCHAR(10), IN `toDate` VARCHAR(10))
BEGIN
#出力対象の列を指定する
SELECT
    #商品売り上げ情報テーブルID
    commodity_sell.id AS id
    #ユーザ名
    ,user_name
    #授業名(一覧出力後入る)
    ,'' AS lesson_name
    #購入額
    ,pay_cash AS cost
    #商品購入での使用ポイント
    ,commodity_sell.use_point AS use_point
    #ステージ番号(デフォルト値)
    ,1 AS stage_no 
    #レベル番号(デフォルト値)
    ,1 AS level_no
    #売上個数
    ,sell_number
    #商品名
    ,content
    #ユーザID
    ,user_inf.id AS user_key
    #校舎情報テーブルID
    ,commodity_sell.school_key AS school_key
    #商品情報テーブルID
    ,commodity_sell.commodity_key
    #所持ポイント
    ,user_inf.get_point AS get_point 
#データ取得元のテーブルを指定する
FROM
    #商品売り上げ情報テーブル
    commodity_sell
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #ユーザ情報
    user_inf
#以下に指定した列を基に結合を行う
ON
    #ユーザID
    user_inf.id = commodity_sell.user_key 
    #売上期間(開始日付)
    AND sell_datetime <= toDate
    #売上期間(終了日付)
    AND sell_datetime >= fromDate
#結合対象の列の値がnullのデータを排除して結合する 
INNER JOIN
    #商品詳細情報テーブル
    commodity_inf
#以下に指定した列を基に結合を行う
ON
    #商品詳細情報テーブルID
    commodity_inf.id = commodity_sell.commodity_key
#ソートを行う
ORDER BY
    #売り上げ日付が新しい順番に並べる
    sell_datetime DESC
;
#ストアドプロシージャの処理を終える
END $$

#授業削除
DROP PROCEDURE IF EXISTS p_delete_classwork $$
CREATE PROCEDURE p_delete_classwork (
    IN in_id INT
    ,OUT result INT
)
BEGIN
    
DECLARE old_count int;
DECLARE new_count int;

SELECT 
    COUNT(id)
FROM
    classwork
INTO
    old_count;

START TRANSACTION;

DELETE
FROM
    classwork
WHERE
    id = in_id
AND
    order_students = 0;

SELECT
    COUNT(id)
FROM
    classwork
INTO
    new_count;

IF old_count > new_count THEN
    SELECT 1 INTO result;
    COMMIT;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

#メルマガ削除
DROP PROCEDURE IF EXISTS p_delete_mail_magazine $$
CREATE PROCEDURE p_delete_mail_magazine (
    IN in_id INT
    ,OUT result INT
)
BEGIN
    
DECLARE old_count int;
DECLARE new_count int;

SELECT 
    COUNT(id)
FROM
    mail_magazine
INTO
    old_count;

START TRANSACTION;

DELETE
FROM
    mail_magazine
WHERE
    id = in_id;

SELECT
    COUNT(id)
FROM
    mail_magazine
INTO
    new_count;

IF old_count > new_count THEN
    SELECT 1 INTO result;
    COMMIT;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

# 受講可能レッスンチェック
DROP PROCEDURE IF EXISTS check_userworkstatus $$
CREATE PROCEDURE check_userworkstatus(
	IN in_user_key INT
	,IN in_from_date varchar(10)
	,IN in_to_date varchar(10)
    ,OUT `result` TEXT
)
BEGIN
# ログインユーザの授業予約状況チェック用クエリ
SELECT DISTINCT 
	lesson_date
	,user_work_status 
FROM 
	`user_classwork` 
INNER JOIN 
	classwork 
ON 
	user_classwork.classwork_key = classwork.id 
INNER JOIN 
	time_table_day 
ON 
	classwork.time_table_day_key = time_table_day.id 
WHERE 
	lesson_date >= in_from_date 
AND 
	lesson_date <= in_to_date 
AND 
	user_key = in_user_key;
END$$

# 授業の最大人数、最小人数変更
DROP PROCEDURE IF EXISTS p_update_capacity_classwork $$
CREATE PROCEDURE p_update_capacity_classwork (
    IN in_id INT
    ,IN in_min_sutudents INT
    ,IN in_max_students INT
    ,OUT result INT
)
BEGIN

DECLARE updated_old DATETIME;
DECLARE updated DATETIME;

SELECT 
    MAX(update_datetime)
FROM
    classwork
INTO
    updated_old;

START TRANSACTION;

UPDATE
    classwork
SET
    min_students = in_min_sutudents
	,max_students = in_max_students
	,update_datetime = NOW()
WHERE
    id = in_id;

SELECT 
    MAX(update_datetime)
FROM
    classwork
INTO
    updated;

IF updated > updated_old THEN
    SELECT 1 INTO result;
    COMMIT;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

# 時間帯の最大人数、最小人数変更
DROP PROCEDURE IF EXISTS p_update_capacity_time_table_day $$
CREATE PROCEDURE p_update_capacity_time_table_day (
    IN in_id INT
    ,IN in_min_sutudents INT
    ,IN in_max_students INT
    ,OUT result INT
)
BEGIN

DECLARE updated_old DATETIME;
DECLARE updated DATETIME;

SELECT 
    MAX(update_datetime)
FROM
    time_table_day
INTO
    updated_old;

START TRANSACTION;

UPDATE
    time_table_day
SET
    min_num = in_min_sutudents
	,max_num = in_max_students
	,update_datetime = NOW()
WHERE
    id = in_id;

SELECT 
    MAX(update_datetime)
FROM
    time_table_day
INTO
    updated;

IF updated > updated_old THEN
    SELECT 1 INTO result;
    SELECT ROW_COUNT();
    COMMIT;
ELSE
    SELECT 0 INTO result;
    ROLLBACK;
END IF;

END$$

# 授業更新プロシージャ
DROP PROCEDURE IF EXISTS p_update_lesson_detail $$
CREATE PROCEDURE p_update_lesson_detail (
    IN in_max_students INT
    ,IN in_min_students INT
    ,IN in_classwork_status INT
    ,IN in_classroom VARCHAR(100)
    ,IN in_classwork_note TEXT
    ,IN in_classwork_key INT
    ,OUT result INT
)
# プロシージャ開始
BEGIN
	UPDATE 
		classwork 
	SET 
		max_students = in_max_students 
		,min_students = in_min_students
		,classwork_status = in_classwork_status 
		,classroom = in_classroom
		,classwork_note = in_classwork_note 
		,update_datetime = NOW() 
	WHERE 
		id = in_classwork_key
	;
	SELECT ROW_COUNT();
END$$

# お知らせ用ブログ記事取得

#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_get_new_blog` $$
#授業予約のプロシージャの登録を行う
CREATE PROCEDURE `p_get_new_blog`(
    OUT result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
SELECT 
    user_name AS name
    ,CONCAT('uploadImage/flowerImage/' , image_1) AS image
    ,title
    ,DATE_FORMAT(post_timestamp,GET_FORMAT(DATE,'JIS')) AS date
FROM 
    `user_blog`
INNER JOIN 
    user_inf
ON
    user_blog.user_key = user_inf.id
WHERE 
	disclosure_range = 0
ORDER BY
    post_timestamp DESC
LIMIT 3;

END$$

# お知らせ用ギャラリー記事取得

#当該プロシージャが既に登録されていた場合、登録し直すため一旦削除する
DROP PROCEDURE IF EXISTS `p_get_new_gallery_photo` $$
#授業予約のプロシージャの登録を行う
CREATE PROCEDURE `p_get_new_gallery_photo`(
    OUT result text
    )
#以降にストアドプロシージャの処理を記述する
BEGIN
SELECT 
    user_name AS name
    ,CONCAT('uploadImage/flowerImage/' , photo_title) AS image
    ,article_title AS title
    ,DATE_FORMAT(update_timestamp,GET_FORMAT(DATE,'JIS')) AS date
FROM 
    `user_image`
INNER JOIN 
    user_inf
ON
    user_image.user_key = user_inf.id
ORDER BY
    update_timestamp DESC
LIMIT 3;

END$$

# 授業時間帯作成プロシージャ
DROP PROCEDURE IF EXISTS p_insert_time_table_day $$
CREATE PROCEDURE p_insert_time_table_day (
    IN in_timetable_key INT
    ,IN in_lesson_date varchar(10)
    ,IN in_max_num INT
    ,IN in_min_num INT
    ,OUT result INT
)
# プロシージャ開始
BEGIN
# レコードを以下のテーブルに新規追加する
INSERT INTO 
	# 授業時間帯テーブル
	time_table_day
	# 値を設定する列を指定する
	( 
	    # 最小人数
		min_num 
		# 最大人数
		,max_num
		# 授業時間帯情報テーブルのID
		,timetable_key 
		# 授業の日付
		,lesson_date
		# レコード作成日時
		,create_datetime
		# レコード更新日時
		,update_datetime 
	)
# 以下の通りに値をセットする
VALUES
	( 
	    # 各々引数の通りに値をセットする
		in_min_num 
		,in_max_num 
		,in_timetable_key 
		,in_lesson_date
		# 作成日時、更新日時は現在時刻
		,NOW() 
		,NOW() 
	);
# プロシージャを終了する
END$$

# 時間帯作成と同時に授業作成するときに使うプロシージャ
DROP PROCEDURE IF EXISTS p_insert_new_classwork $$
CREATE PROCEDURE p_insert_new_classwork (
	IN in_max_students INT
	,IN in_min_students INT
	,IN in_classwork_status INT 
	,IN in_classroom VARCHAR(10)
	,IN in_classwork_note TEXT
    ,IN in_lesson_key VARCHAR(8)
    ,IN in_timetable_key INT
    ,OUT result INT
)
# プロシージャ開始
BEGIN
# レコードを以下のテーブルに新規追加する
INSERT INTO 
	classwork
		( 
			max_students 
			,min_students 
			,classwork_status 
			,classroom 
			,classwork_note 
			,teacher_key 
			,school_key 
			,lesson_key 
			,time_table_day_key 
			,create_datetime 
			,update_datetime
			,order_students
		) 
	VALUES
		(
			in_max_students 
			,in_min_students 
			,in_classwork_status 
			,in_classroom 
			,in_classwork_note
			,(select id from user_inf where authority = 10 limit 1)
			,(SELECT school_key FROM timetable_inf WHERE id = in_timetable_key) 
			,in_lesson_key 
			,(SELECT id FROM time_table_day WHERE timetable_key = in_timetable_key order by create_datetime DESC LIMIT 1) ,NOW() ,NOW(), 0 )
;
END $$

# 通常通り授業作成するときに使うプロシージャ
DROP PROCEDURE IF EXISTS p_insert_normal_classwork $$
CREATE PROCEDURE p_insert_normal_classwork (
	IN in_max_students INT
	,IN in_min_students INT
	,IN in_classwork_status INT 
	,IN in_classroom VARCHAR(10)
	,IN in_classwork_note TEXT
    ,IN in_lesson_key VARCHAR(8)
    ,IN in_timetable_key INT
    ,IN in_time_table_day_key INT
    ,OUT result INT
)
# プロシージャ開始
BEGIN
# レコードを以下のテーブルに新規追加する
INSERT INTO 
	classwork
	( 
		max_students 
		,min_students 
		,classwork_status 
		,classroom 
		,classwork_note 
		,teacher_key 
		,school_key 
		,lesson_key 
		,time_table_day_key 
		,create_datetime 
		,update_datetime
		,order_students
	) 
VALUES
	(
		in_max_students 
		,in_min_students 
		,in_classwork_status 
		,in_classroom 
		,classwork_note
		,(select id from user_inf where authority = 10 limit 1)
		,(SELECT school_key FROM timetable_inf WHERE id = in_timetable_key)
		,in_lesson_key
		,in_time_table_day_key
		,NOW()
		,NOW()
		,0)
;
END$$

# 時間帯一覧取得
DROP PROCEDURE IF EXISTS p_select_timetable_day $$
# 授業時間帯情報の変更に必要なデータを取り出す
CREATE PROCEDURE p_select_timetable_day (
    IN in_lesson_date varchar(10)
    ,OUT result text
)
# プロシージャ開始
BEGIN

# 以下の列を取得する
SELECT
    # ID
    time_table_day.id
    # 最小人数
    ,time_table_day.min_num
    # 最大人数
    ,time_table_day.max_num
    # 開始時間
    ,start_time
    # 終了時間
    ,end_time
# 以下のテーブルからデータを取得する
FROM
    # 授業時間帯テーブル
    time_table_day
# テーブルを結合する
INNER JOIN
    # 授業時間帯情報テーブル
    timetable_inf
# 以下の列を指定して結合する
ON
    # 各授業時間帯情報テーブルID
    time_table_day.timetable_key = timetable_inf.id
# 検索条件を指定する
WHERE
    # 指定した授業日時
    lesson_date = in_lesson_date
;
# プロシージャを終了する
END$$

#区切り文字をセミコロンに戻す
delimiter ;