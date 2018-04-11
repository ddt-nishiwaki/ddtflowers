package ddtflowers;

import javax.servlet.http.Cookie;

public class LoginCheckException extends Exception {
    // タイムアウト状態を示す値です
    private static final int    TIMEOUT_STATE   = 1;
    // ログイン状態を示す値です
    private static final int    LOGIN_STATE     = 0;
    // ログイン状態を格納しているキー文字列です
    private static final String LOGIN_STATE_KEY = "user";

    /**
     * 関数名 :checkLoginState
     * 概要  :ログイン状態を調べて数値で返す。
     * 返り値 :int
     * 作成者 :S.Nishiwaki
     * 作成日 :2018.02.03
     */
    public int checkLoginState(HttpRequestController httpRequestController) {
        //返却値の変数に初回ログインの値0をセットする
        int loginStateCode = LOGIN_STATE;
        // クッキー情報を取得します
        Cookie[] cookies = (httpRequestController.getRequest()).getCookies();
        // クッキーがない場合は返却地の変更を行わないようにする
        if (cookies != null) {
            // クッキー情報を検索する
            for (Cookie cookie : cookies) {
                // ログイン状態を確認する為の文字列を見つけた場合の処理を行う
                if (LOGIN_STATE_KEY.equals(cookie.getName())) {
                    // タイムアウト状態を示す値を返却地にセットする
                    loginStateCode = TIMEOUT_STATE;
                    // クッキーの検索を終了する
                    break;
                }
            }
        }
        // ログイン状態コードを返す
        return loginStateCode;
    }
}
