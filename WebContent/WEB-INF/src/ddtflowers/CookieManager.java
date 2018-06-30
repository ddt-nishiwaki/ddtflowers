package ddtflowers;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




/*
 * クラス名:CookieManager
 *
 * 概要　:クッキーを扱うクラス
 * 設計者:S.Nishiwaki
 * 作成者:S.Nishiwaki
 * 作成日:2018.03.23
 */
public class CookieManager {
    ///////////////////////////////////////////////////////////////////////////
    // 定数
    ///////////////////////////////////////////////////////////////////////////
    // 空文字を示すメンバです
    private static final String NULL_STRING = "";
    // 存在することを示す値です
    private static final boolean EXIST = true;
    // 存在しないことを示す値です
    private static final boolean NOT_EXIST = false;
    // クッキーの有効期限を伸ばすための値です
    private static final int EXTEND_LIFE_VALUE = 3600;
    // クッキーの有効期限を無効にするための値です
    private static final int END_LIFE_VALUE = 0;

    ///////////////////////////////////////////////////////////////////////////
    // メンバ
    ///////////////////////////////////////////////////////////////////////////
    // クッキーを取得するためのレスポンスを保持するメンバです
    HttpServletRequest mRequest;
    // クッキーを返すためのレスポンスを保持するメンバです
    HttpServletResponse mResponse;
    // 現在管理しているクッキーの一覧です
    Set<Cookie> mCookies;
    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 概要　:サーブレットリクエストからクッキー情報を取得して保持します
     * 引数　:HttpServletRequest request
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public CookieManager(HttpServletRequest request, HttpServletResponse response) {
        // cookie情報を取得するためのリクエスト情報を取得する
        mRequest = request;
        // クッキー情報を追加するためにresponseオブジェクトを保持する
        mResponse = response;
        // リクエストのクッキーを受け取る
        mCookies = Arrays.stream(request.getCookies()).collect(Collectors.toSet());
    }

    ///////////////////////////////////////////////////////////////////////////
    // メソッド
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 関数名:setCookie
     * 概要　:クッキーを作成してクッキー情報に加えます
     * 引数　:String keyString
     * 　　　:String valueString
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public void setCookie(String keyString, String valueString) {
        // keyStringとvalueStringからクッキーを作成します
        Cookie cookie = new Cookie(keyString, valueString);
        // クッキーの有効期限を設定する
        cookie.setMaxAge(EXTEND_LIFE_VALUE);
        // 作成したクッキーをメンバに追加します
        mResponse.addCookie(cookie);
        // 管理対象のクッキーリストに追加する
        mCookies.add(cookie);
    }

    /**
     * 関数名:getCookieValue
     * 概要　:指定したクッキーの値を取得します
     * 　　　:指定したクッキーが存在しない場合はnullを返します
     * 引数　:String targetName
     * 戻り値:String targetCookieValue
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public String getCookieValue(String targetName) {
        // 返却値を null で初期化する
        String targetCookieValue = null;
        // 各クッキーの名前を確認していく
        for(Cookie cookie : mCookies) {
            // 各クッキーの名前を、確認のため取り出して保持する
            String cookieName = cookie.getName();
            // targetNameとcookieNameの名前が一致しているか確認する
            if(targetName.equals(cookieName)) {
                // クッキーの値を返却値に設定する
                targetCookieValue = cookie.getValue();
                // クッキーの走査を終了する
                break;
            }
        }
        // 返却値を返す
        return targetCookieValue;
    }

    /**
     * 関数名:hasCookie
     * 概要　:クッキーに指定のキーが存在するか確認する
     * 引数　:String targetName
     * 戻り値:boolean isCookie
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public boolean hasCookie(String targetName) {
        // 返却値をを「存在しない」判定で初期化する
        boolean isCookie = NOT_EXIST;
        // 指定された名前のクッキー取得を試みて結果を targetCookieName に保持する
        String targetCookieName = getCookieValue(targetName);
        // targetCookie が 取得できているか確認する
        if(targetCookieName != null) {
            // 返却値に「存在する」判定を設定する
            isCookie = EXIST;
        }
        // 返却値を返す
        return isCookie;
    }

    /**
     * 関数名:extendAge
     * 概要　:指定されたクッキーの有効期限を更新する
     * 引数　:String targetName
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public void extendAge(String targetName) {
        // 現在のクッキーを取得する
        Cookie[] cookies = mRequest.getCookies();
        // 各クッキーの名前を確認していく
        for(Cookie cookie : cookies) {
            // 各クッキーの名前を確認するために取り出して保持する
            String cookieName = cookie.getName();
            // targetNameとcookieNameの名前が一致しているか確認する
            if(targetName.equals(cookieName)) {
                // クッキーの有効期限を延長する
                cookie.setMaxAge(EXTEND_LIFE_VALUE);
                // 延長したクッキーをブラウザに設定する
                mResponse.addCookie(cookie);
                // クッキーの走査を終了する
                break;
            }
        }
    }

    /**
     * 関数名:endLife
     * 概要　:指定されたクッキーの有効期限を無効にする
     * 引数　:String targetName
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.23
     */
    public void endLife(String targetName) {
        // 現在のクッキーを取得する
        Cookie[] cookies = mRequest.getCookies();
        // 各クッキーの名前を確認していく
        for(Cookie cookie : cookies) {
            // 各クッキーの名前を確認するために取り出して保持する
            String cookieName = cookie.getName();
            // targetNameとcookieNameの名前が一致しているか確認する
            if(targetName.equals(cookieName)) {
                // クッキーの有効期限を無効にする
                cookie.setMaxAge(END_LIFE_VALUE);
                // 無効にしたクッキーをブラウザに設定する
                mResponse.addCookie(cookie);
                // クッキーの走査を終了する
                break;
            }
        }
    }
}
