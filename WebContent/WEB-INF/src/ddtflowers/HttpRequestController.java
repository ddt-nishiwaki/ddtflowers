package ddtflowers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//import ddtflowers.GetJSONArray;
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
    private static final String PARAMETER_KEY = "service";
    // ProcedureBase型のクラス名を完全修飾名にするための文字列です
    private static final String PATH_TO_PROCEDURE = "ddtflowers.";
    // 取得したパラメータから余分な文字列を削除するための正規表現です
    private static final String PARAMETER_PARSE_REGEXP = "\\?.*";
    // 空文字を示す値です
    private static final String NULL_STRING = "";

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
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // requestオブジェクトを保持する
        mRequest = request;
        // responseオブジェクトを保持する
        mResponse = response;
        // リクエストURLによって行う処理試みる
        try {
            // ルーティングを行う
            doRouting(request);
            // ルーティングに失敗した時の処理を行う
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            // エラー内容を出力する
            e.printStackTrace();
        }
    }

    /**
     * 関数名:doGet
     * 概要　:Servletオブジェクトを取得してルーティングを行う
     * 引数　:HttpServletRequest:サーブレットから受け取るリクエストオブジェクト
     * 引数　:HttpServletResponse:サーブレットから受け取るレスポンスオブジェクト
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.14
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // requestオブジェクトを保持する
        mRequest = request;
        // responseオブジェクトを保持する
        mResponse = response;
        // リクエストURLによって行う処理試みる
        try {
            // ルーティングを行う
            doRouting(request);
            // ルーティングに失敗した時の処理を行う
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
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
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    private void doRouting(HttpServletRequest request)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        String parameter = request.getParameter(PARAMETER_KEY);
        // requestのURL情報からリクエストされた処理を示すパラメータを取得して保持する
        String serviceName = parseParameter(parameter);
        // アプリの処理クラスのロードを試みる
        try {
            // パラメータに対応した処理を行うクラスをロードする
            Class<?> applicationServiceClass = Class.forName(PATH_TO_PROCEDURE + serviceName);
            // ロードしたクラスのコンストラクタを取得する
            Constructor<?> constructor = applicationServiceClass.getConstructor(HttpRequestController.class);
            // POSTされたJSON文字列を渡してプロシージャを呼び出すインスタンスを作成する
            ApplicationService applicationService = (ApplicationService) constructor.newInstance(this);
            // アプリのロジックに自信を渡して実行する
            applicationService.doProcedure();
        // クラスのロードに失敗した場合の処理を行う
        } catch (ClassNotFoundException ex) {
            // エラーログを出力する
            ex.printStackTrace();
        }
    }

    /**
     * 関数名:perseParameter
     * 概要　:URLパラメータ文字列から不要な文字列をパースして取り除きます
     * 引数　:String URLに付与されたパラメータ
     * 戻り値:String
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.19
     */
    public String parseParameter(String parameterString) {
        // パラメータに余計な文字列が付与されることがあるのでパースします
        String parsedParameter = parameterString.replaceAll(PARAMETER_PARSE_REGEXP, NULL_STRING);
        // メンバに保持したHttpServletRequestを返します
        return parsedParameter;
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

    /**
     * 関数名:getPostValue
     * 概要　:POSTされたデータから指定した値を取得します
     * 引数　:String keyName
     * 戻り値:String keyValue
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.26
     */
    public String getPostValue(String keyString) {
        // POSTされたデータから指定された値の取得を試みる
        String keyValue = mRequest.getParameter(keyString);
        // 取得結果を返す
        return keyValue;
    }

    /**
     * 関数名:sendJsonData
     * 概要　:リクエストに対してデータを返します
     * 引数　:String responseData
     * 戻り値:void
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.26
     * @throws IOException
     */
    public void sendJsonData(String responseData) throws IOException{
        // JSONデータを返すようにレスポンスを設定する
        mResponse.setContentType("application/json;charset=UTF-8");
        // データ返却のためのオブジェクトを取得して保持する
        PrintWriter printWriter = mResponse.getWriter();
        // リクエストに対してデータを返す
        printWriter.print(responseData);
    }

    /**
     * 関数名:getSessionManager
     * 概要　:セッション管理オブジェクトを生成して返す
     * 引数　:void
     * 戻り値:SessionManager
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.06
     */
    public SessionManager getSessionManager() {
        // セッション管理オブジェクトを生成して返します
        return new SessionManager(mRequest);
    }

    /**
     * 関数名:getCookieManager
     * 概要　:クッキー管理オブジェクトを生成して返す
     * 引数　:void
     * 戻り値:CookieManager
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.06
     */
    public CookieManager getCookieManager() {
        // クッキー管理オブジェクトを生成して返します
        return new CookieManager(mRequest, mResponse);
    }
}
