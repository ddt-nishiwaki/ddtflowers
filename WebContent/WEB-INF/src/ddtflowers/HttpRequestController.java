package ddtflowers;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * クラス名:HttpRequestController
 * 
 * 概要　:HttpRequestのコントローラです。
 * 　　　 JavaでSessionやCookieを利用するためにはサーブレットクラスを設定して
 * 　　　 HttpServletRequest,HttpServletResponseオブジェクトを受け取る必要があるため
 * 　　　 アプリロジックの入り口として作成しました。
 * 設計者:S.Nishiwaki
 * 作成者:S.Nishiwaki
 * 作成日:2018.03.14
 */
public class HttpRequestController extends HttpServlet {
    ///////////////////////////////////////////////////////////////////////////
    // 定数
    ///////////////////////////////////////////////////////////////////////////
    // URLからProcedureBase型となるクラスの名前を取得するパラメータのキーです。
    private static final String PARAMETER_KEY = "procedure";
    // ProcedureBase型のクラス名を完全修飾名にするための文字列です
    private static final String PATH_TO_PROCEDURE = "ddtflowers.";

    ///////////////////////////////////////////////////////////////////////////
    // メンバ
    ///////////////////////////////////////////////////////////////////////////
    // HttpRequestオブジェクトを保持するメンバです
    private static HttpServletRequest mRequest;
    // HttpResponseオブジェクトを保持するメンバです
    private static HttpServletResponse mResponse;

    ///////////////////////////////////////////////////////////////////////////
    // メソッド
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 関数名:doGet
     * 概要　:Servletオブジェクトを取得してルーティングを行う
     * 引数　:HttpServletRequest:サーブレットから受け取るリクエストオブジェクト
     * 引数　:HttpServletResponse:サーブレットから受け取るレスポンスオブジェクト
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        // requestオブジェクトを保持する
        mRequest = request;
        // responseオブジェクトを保持する
        mResponse = response;
        // リクエストURLによって行う処理試みる
        try {
            // ルーティングを行う
            doRouting(request);
            // ルーティングに失敗した時の処理を行う
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // エラー内容を出力する
            e.printStackTrace();
        }
    }

    /**
     * 関数名:doRouting
     * 概要　:リクエストURLを元にルーティングを行います
     * 　　　:想定するパラメータ => ddtflowers/java?procedure=プロシージャ名
     * 引数　:HttpServletRequest:URL情報を持つリクエストオブジェクト
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void doRouting(HttpServletRequest request)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // requestのURL情報からリクエストされた処理を示すパラメータを取得して保持する
        String procedureName = request.getParameter(PARAMETER_KEY);
        // プロシージャクラスのロードを試みる
        try {
            // パラメータに対応した処理を行うプロシージャクラスをロードする
            Class<?> procedureClass = Class.forName(PATH_TO_PROCEDURE + procedureName);
            // ロードしたプロシージャクラスを実行するためにインスタンス化する
            ProcedureBase procedure = (ProcedureBase) procedureClass.newInstance();
            // リクエストされたプロシージャクラスを実行する
            procedure.run(this);
        // クラスのロードに失敗した場合の処理を行う
        } catch (ClassNotFoundException ex) {
            // エラーログを出力する
            ex.printStackTrace();
        }
    }

    /**
     * 関数名:getRequest
     * 概要　:HttpServletRequestを返します
     * 引数　:なし
     * 戻り値:HttpServletRequest mRequest
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    public HttpServletRequest getRequest() {
        // メンバに保持したHttpServletRequestを返します
        return mRequest;
    }

    /**
     * 関数名:getResponse
     * 概要　:メンバに保持したHttpServletResponsを返します
     * 引数　:なし
     * 戻り値:HttpServletResponse mResponse
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     */
    // HttpServletResponsを返します
    public HttpServletResponse getResponse() {
        // メンバに保持したHttpServletResponsを返します
        return mResponse;
    }
}
