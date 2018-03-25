package ddtflowers;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/*
 * クラス名:SessionManager
 * 
 * 概要　:セッションを扱うクラス
 * 設計者:S.Nishiwaki
 * 作成者:S.Nishiwaki
 * 作成日:2018.03.14
 */
public class SessionManager {
    
    ///////////////////////////////////////////////////////////////////////////
    // 定数
    ///////////////////////////////////////////////////////////////////////////
    // 存在していることを示す値です
    private static final boolean EXIST = true;
    // 存在していないことを示す値です
    private static final boolean NOT_EXIST = true;

    ///////////////////////////////////////////////////////////////////////////
    // メンバ
    ///////////////////////////////////////////////////////////////////////////
    // セッションオブジェクトのID再生成のためのメンバです
    private HttpServletRequest mRequest;
    // セッションオブジェクトを保持するメンバです
    private HttpSession mSession;

    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 概要　:引数のリクエスト情報を保持します 
     * 引数　:HttpServletRequest:サーブレットから受け取るリクエストオブジェクト
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public SessionManager(HttpServletRequest request) {
        // セッションIDを再生成するためにrequestオブジェクト自体を保持する
        mRequest = request;
        // requestからセッションを取得して、これを保持する
        mSession = mRequest.getSession();
    }

    ///////////////////////////////////////////////////////////////////////////
    // メソッド
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 関数名:sessionDestroy
     * 概要　:セッションを破棄します
     * 引数　:なし
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public void sessionDestroy() {
        // セッションを破棄する
        mSession.invalidate();
    }

    /**
     * 関数名:hasSession
     * 概要　:セッションに指定のキーがあるか確認する
     * 引数　:なし
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public boolean hasSessionKey(String sessionKey) {
        // 返却値として存在しない場合の値をデフォルトで設定する
        boolean isSessionValue = NOT_EXIST;
        // セッションのvalue一覧を取得する
        Enumeration<String> sessionValues = mSession.getAttributeNames();
        // sessionKey に 該当する値を取得するため走査する
        while(sessionValues.hasMoreElements()) {
            // 現在のセッションに設定されているキーを順次取得する
            String currentKey = sessionValues.nextElement();
            // 一致するキーがあるか確認する
            if(sessionKey.equals(currentKey)) {
                // 一致するセッションキーがあればその判定を返却値に設定する
                isSessionValue = EXIST;
            }
        }
        // 存在判定を返却する
        return isSessionValue;
    }

    /**
     * 関数名:setValue
     * 概要　:セッションに指定のキーがあるか確認する
     * 引数　:String sessionKey
     * 　　　:Object sessionValue
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public void setValue(String sessionKey, Object sessionValue) {
        // セッションに sessionKey と sessionValue の マッピングをセットする
        mSession.setAttribute(sessionKey, sessionValue);
    }

    /**
     * 関数名:getValue
     * 概要　:セッションから 指定したkeyに該当する値を取得する
     * 引数　:String sessionKey
     * 戻り値:Object セッションのvalueオブジェクト
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public Object getValue(String sessionKey) {
        // セッションに sessionKey と sessionValue の マッピングをセットする
        return mSession.getAttribute(sessionKey);
    }
    
    /**
     * 関数名:regenerateId
     * 概要　:セッションのIDを再生成、登録する
     * 引数　:なし
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public void regenerateId() {
        // セッションIDを変更する
        mRequest.changeSessionId();
    }

    /**
     * 関数名:getSessionId
     * 概要　:現在のセッションのID取得する
     * 引数　:なし
     * 戻り値:String セッションID
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public String getSessionId() {
        // mSession から セッションIDを取得して返す
        return mSession.getId();
    }
}
