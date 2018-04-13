package ddtflowers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/*
 * クラス名:ProcedureLogin
 * 概要    :ログインの手続きを行うクラスのファイル。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.04.10
 */
public class ProcedureGetList extends ProcedureBase {

    ///////////////////////////////////////////////////////////////////////////
    // 定数
    ///////////////////////////////////////////////////////////////////////////
    // JSONデータからテーブルデータを取得するキーです
    private static final String STR_TABLE_DATA = "tableData";
    // 処理に異常が発生した場合のエラーコードです
    private static final int ERROR_CODE = 1;

    ///////////////////////////////////////////////////////////////////////////
    // メンバ
    ///////////////////////////////////////////////////////////////////////////
    // クライアントにデータを渡すためのメンバです
    private HttpRequestController mController;

    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    // アカウントにコントローラを渡すためコントローラを受け取る
    public ProcedureGetList(HttpRequestController controller) throws ParseException {
        // 親クラスのコンストラクタにもコントローラを渡す
        super(controller);
        // クライアントにデータを渡す処理を持っているのでコントローラを保持する
        mController = controller;
    }

    /*
     * 関数名:init
     * 概要　:クラスの初期化関数。ログイン用のクラスの初期化関数をコールする。
     * 引数　:なし
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.31
     */
    @Override
    public void init() throws  ClassNotFoundException, SQLException, IOException{
        //親クラスのinit関数をコールする。
        super.init();
    }

    /*
     * 関数名:job
     * 概要　:クラス特有の処理を行う関数
     * 引数　:String jsonString:JSON文字列
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.11
     */
    @Override
    public void job(String jsonString) throws ParseException, SQLException, IOException {
        //親クラスのjobを実行し、メンバにJSONオブジェクトを格納する。
        super.job(jsonString);
        // 返却するJSON配列の文字列を格納する変数を用意する
        String retArrayString = "";

        //SQLによる例外の対処のためtryブロックで囲む
        try {
            //取得したJSON連想配列を走査する
            if(mJsonObject instanceof JSONObject && isHash(mJsonObject)) {
                // レコードのJSONを作る
                retArrayString = getListJSONPlusKey(mJsonObject, STR_TABLE_DATA);
            }
        // SQL例外を監視する
        } catch (SQLException e) {
            // 例外が発生したらログを出力するためのオブジェクトを作成する
            Logger logger = Logger.getAnonymousLogger();
            // ログを出力する
            logger.log(null, e.getMessage());
            // 最後に必ず行う
            System.exit(ERROR_CODE);
        }
        //作成したJSON文字列を出力する。
        mController.sendJsonData(retArrayString);
    }

    /*
     * 関数名：run
     * 概要  :クラスのinit、job関数をまとめて実行する。
     * 引数  :String jsonString:JSON文字列。
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.11
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    @Override
    public void run(String jsonString) throws ClassNotFoundException, SQLException, IOException, ParseException {
        // 初期化処理を行う
        init();
        // アプリのロジックを実行する
        job(jsonString);
    }
}
