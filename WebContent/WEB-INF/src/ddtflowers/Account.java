package ddtflowers;

import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * クラス名:Account
 *
 * 概要　:ログインのための関数を持ったクラスのファイル
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.xx.xx
 */
public class Account extends JSONDBManager{

    ///////////////////////////////////////////////////////////////////////////
    // 定数
    ///////////////////////////////////////////////////////////////////////////
    //返却用JSONの文字列です (前)
    private static final String ERROR_JSON_FRONT = "{\"createTagState\":\"";
    //返却用JSONの文字列です (後)
    private static final String ERROR_JSON_BACK = "\"}";
    // 権限があることを示す値です
    private static final boolean AUTHENTICATED_VALUE = true;
    // 権限がないことを示す値です
    private static final boolean PERMITION_DENITED_VALUE = false;
    // 処理に異常が発生した場合のエラーコードです
    private static final int ERROR_CODE = 1;
    // userIdの初期値を示す値です
    private static final String DEFAULT_USER_ID = "0";
    // ユーザーからポストされた key を示す値を取得するためのキーです
    private static final String USER_POSTED_KEY = "key";
    // ユーザーID(会員番号)オブジェクトを取得するためのキーです
    private static final String USER_ID_KEY = "userId";
    // ユーザーの閲覧権限オブジェクトを取得するためのキーです
    private static final String USER_AUTHORITY_KEY = "authority";
    // ユーザー権限の値を取得するためのキーです
    private static final String AUTHORITY_TEXT_KEY = "text";
    // ユーザーID(会員番号)の値を取得するためのキーです
    private static final String ID_VALUE_KEY = "value";
    // ページの閲覧権限を取得するためのキーです
    private static final String PAGE_AUTHORITY_KEY = "pageAuth";
    // 権限がないことを示すビットフラグです
    private static final int PERMISSION_DENIED_BIT_FLAG = 0;

    ///////////////////////////////////////////////////////////////////////////
    // メンバ
    ///////////////////////////////////////////////////////////////////////////
    // リクエストとレスポンスを管理するオブジェクトを保持するメンバです
    private HttpRequestController mHttpRequestController;
    // セッションオブジェクトを保持するメンバです
    private SessionManager mSessionManager;
    // クッキーオブジェクトを保持するメンバです
    private CookieManager mCookieManager;

    // ページ権限確認用の権限取得用のJSON文字列です
    private String mPageAuthorityCheckJson = "{"
            + "\"db_getQuery\" : \"SELECT authority FROM user_inf WHERE id='userId';\","
            + "\"userId\" : { \"value\" : \"\" },"
            + "\"authority\" : { \"text\" : \"\" }"
    + "}";

    // ページ権限確認用の権限取得用のJSONオブジェクトです
    private JSONObject mPageAuthorityCheck;

    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 概要　:セッションとクッキーを管理するオブジェクトを受け取ります
     * 引数　:SessionManager sessionManager:セッション管理を行うオブジェクト
     * 引数　:CookieManager cookieManager:クッキー管理を行うオブジェクト
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     * @throws ParseException
     */
    public Account(HttpRequestController controller) throws ParseException {
        // ポストデータを参照するためにコントローラーをメンバに保持する
        mHttpRequestController = controller;
        // セッションIDを再生成するためにSessionManagerオブジェクトを保持する
        mSessionManager = controller.getSessionManager();
        // requestからセッションを取得して、これを保持する
        mCookieManager = controller.getCookieManager();
        // ページ権限確認用の権限取得用のJSON文字列をオブジェクト化するパーサーを作成する
        JSONParser parser = new JSONParser();
        // ページ権限確認用のJSONオブジェクトを作成してメンバに保持する
        mPageAuthorityCheck = (JSONObject) parser.parse(mPageAuthorityCheckJson);

    }

    /*
     * 関数名:init
     * 概要　:初期化処理を行う。初期化としてセッションの開始とDBへの接続を行う。
     * 引数　:HttpRequestController サーブレットを扱うためのオブジェクト
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public boolean init(HttpRequestController controller) throws ClassNotFoundException, SQLException, IOException, ParseException, NoSuchAlgorithmException {
        // コントローラの参照を受け取る
        mHttpRequestController = controller;
        // プロシージャ経由で受け取ったコントローラからセッションマネージャを受け取る
        mSessionManager = controller.getSessionManager();
        // プロシージャ経由で受け取ったコントローラからクッキーマネージャを受け取る
        mCookieManager = controller.getCookieManager();
        // ページ権限確認用のJSON文字列をオブジェクト化するパーサーを作成する
        JSONParser parser = new JSONParser();
        // ページ権限確認用のJSONオブジェクトを作成してメンバに保持する
        mPageAuthorityCheck = (JSONObject) parser.parse(mPageAuthorityCheckJson);
        // ログイン状態を確認するためにクッキーから userId の値を取得する
        String userIdStatus = mCookieManager.getCookieValue(USER_ID_KEY);
        // COOKIE内のuserIdがnullもしくは初期値（0）であるか検証する（ゲストもしくは未ログイン状態であるか）
        if(userIdStatus == null || userIdStatus.equals(DEFAULT_USER_ID)) {
            // セッション情報内にuserIdを作成し、初期値として0を持たせる
            mSessionManager.setValue(USER_ID_KEY, DEFAULT_USER_ID);
            // COOKIE情報内にuserIdを作成し、初期値として0を持たせる
            mCookieManager.setCookie(USER_ID_KEY, DEFAULT_USER_ID);
        }
        //DBへの接続を開始する。
        super.connect();
    };

    /*
     * 関数名:login
     * 概要　:ログイン処理を行う。
     * 引数　:String jsonString: JSON文字列。ログイン情報が入っている必要がある。
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.27
     */
    public void login(String jsonString) throws ParseException, IOException{
        //クライアントから送信されたJSONのキーとJSON文字列を取得する。
        String key = mHttpRequestController.getPostValue(USER_POSTED_KEY);
        // 受け取ったJSON文字列をオブジェクト化してmJsonObjectに保持する
        getJSONMap(jsonString);

        //SQLによる例外の対処のためtryブロックで囲む
        try {
            // DBからデータを取得してJSONを作りmJsonObjectに保持する
            createJSON(mJsonObject, key, null);
            //SQL例外のcatchブロック
        } catch (SQLException error) {
            // エラーメッセージを表示する
            error.printStackTrace();
        }

        // 作成したJSONオブジェクトをクライアントに返却するため文字列変換し保持する
        String responseJson = mJsonObject.toJSONString();
        // セッションIDを更新する。
        mSessionManager.regenerateId();
        // JSONから会員番号を取り出す。
        String userId = (String) mSessionManager.getValue(USER_ID_KEY);
        // JSONからユーザー権限情を持つオブジェクトを取得する
        JSONObject authorityObject = (JSONObject) mJsonObject.get(USER_AUTHORITY_KEY);
        // ユーザー権限情を持つオブジェクトに設定されている値を取得する
        String authority = authorityObject.get(AUTHORITY_TEXT_KEY).toString();

        //会員番号(ユーザID)をセッションに入れる
        mSessionManager.setValue(USER_ID_KEY, userId);
        //ユーザの権限をセッションに入れる
        mSessionManager.setValue(USER_AUTHORITY_KEY, authority);
        //cookieにユーザIDをセットする
        mCookieManager.setCookie(USER_ID_KEY, userId);
        //cookieにユーザの権限をセットする
        mCookieManager.setCookie(USER_AUTHORITY_KEY, authority);
        // 作成したJSON文字列を出力する。
        mHttpRequestController.sendJsonData(responseJson);
    }

    /*
     * 関数名:logout
     * 概要　:ログアウト処理を行う。
     * 引数　:なし
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.27
     */
    public void logout(){
        /*
         * ログイン情報がセッションとクッキーに保存されているので
         * まず、セッションに紐づくクッキーを確認する
         */
        if(mCookieManager.hasCookie(mSessionManager.getSessionId())) {
            // クッキーが存在したら有効期限を無効にする
            mCookieManager.endLife(mSessionManager.getSessionId());
        }
        // ユーザのクッキーに付与したユーザIDを削除する
        mCookieManager.endLife(USER_ID_KEY);
        // ユーザのクッキーに付与したページ閲覧権限を削除する
        mCookieManager.endLife(USER_AUTHORITY_KEY);

        // クッキーからログイン情報を消したので最後にセッション自体も削除する
        mSessionManager.sessionDestroy();
    }

    /*
     * 関数名:loginCheck
     * 概要　:ログインチェックを行う。
     * 引数　:なし
     * 戻り値:boolean:ログインしているか否かの真理値を返す
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.27
     */
    public boolean loginCheck() throws IOException, SQLException{
        // 返却値を未ログインを示す値で初期化する
        boolean isLogin = PERMITION_DENITED_VALUE;
        // クラスにより例外でログインチェック時の失敗の処理を分岐させるため、try catch文を使う
        try{
            // ログイン状態の確認のためにセッションとクッキーの状態を確認する
            if(mSessionManager.hasSessionKey(USER_ID_KEY) && mCookieManager.hasCookie(USER_ID_KEY) &&
                    mSessionManager.getValue(USER_ID_KEY).equals(mCookieManager.getCookieValue(USER_ID_KEY))) {
                // ログイン認証済みであることを示す値を返却値に設定する
                isLogin = AUTHENTICATED_VALUE;
                // ページに対する権限チェックを行い、権限がない場合は例外を投げる
                pageCheck();

            //セッションがない状態であれば
            } else {
                //ログインチェックエラーの例外を投げる
                throw new LoginCheckException();
            }
        //例外をキャッチした場合は以下のブロックに入る
        } catch (LoginCheckException error){
            // ログインエラーステータス情報を含むメッセージを作成する
            String errorMessage = ERROR_JSON_FRONT + error.checkLoginState(mHttpRequestController) + ERROR_JSON_BACK;
            // 作成したJSON文字列をブラウザに返す
            mHttpRequestController.sendJsonData(errorMessage);
            // 処理を終了する
            System.exit(ERROR_CODE);
        }

        //真理値を返す
        return isLogin;
    }

    /*
     * 関数名:pageCheck
     * 概要　:ページの権限チェックを行う。
     * 引数　:なし
     * 戻り値:なし
     * 設計者:k.urabe
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.27
     */
    public void pageCheck() throws SQLException, LoginCheckException {
        // 検証するユーザー権限をキャッシュする変数です
        int userAuthority;
        // 検証するページ閲覧権限をキャッシュする変数です
        int pageAuthority;
        // ゲストユーザによるリクエストか検証する
        System.out.println(mCookieManager.getCookieValue(USER_ID_KEY));
        if(!mCookieManager.getCookieValue(USER_ID_KEY).equals(DEFAULT_USER_ID)) {
            // ユーザ権限取得用のJSONObjectからユーザーIDのオブジェクトを取得する
            JSONObject userIdObject = (JSONObject) mPageAuthorityCheck.get(USER_ID_KEY);
            // ユーザーIDのオブジェクトにセッション内のユーザIDを追加する
            userIdObject.put(ID_VALUE_KEY, mSessionManager.getValue(USER_ID_KEY));

            // クエリを実行してユーザに紐付くauthorityを取得する
            createJSON(mPageAuthorityCheck);
            // ユーザ権限取得用のJSONObjectから閲覧権限のオブジェクトをキャッシュする
            JSONObject authorityObject = (JSONObject) mPageAuthorityCheck.get(USER_AUTHORITY_KEY);
            // 閲覧権限のオブジェクトの値を変数に保存する(整数化)
            userAuthority = Integer.parseInt((String) mPageAuthorityCheck.get(USER_AUTHORITY_KEY), 10);
        } else {
            // ゲストユーザであるため、ゲスト用のユーザ権限を設定する
            userAuthority = 1;
        }

        // COOKIEにセットされている対象ページの権限を取得する(整数化)
        pageAuthority = Integer.parseInt(mCookieManager.getCookieValue(PAGE_AUTHORITY_KEY), 10);

        // 当該ユーザで開けるページなのか検証する
        if(PERMISSION_DENIED_BIT_FLAG == (userAuthority & pageAuthority)) {
            //ログインチェックエラーの例外を投げる
            throw new LoginCheckException();
        }

    }
}
