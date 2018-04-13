package ddtflowers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.mysql.jdbc.Statement;

/*
 * クラス名:ProcedureSet
 *
 * 概要　:クライアントから送られたJSONのクエリを実行し、
 * 　　　:DBへのレコード追加、変更、削除を行う役割のクラスのファイル。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.03.31
 */
public class ProcedureSet extends ProcedureBase{

    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // JSONのdb_setQueryキーを示す文字列です
    private static final String   DB_SETQUERY             = "db_setQuery";
    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    // アカウントにコントローラを渡すためコントローラを受け取る
    public ProcedureSet(HttpRequestController controller) throws ParseException {
        // 親クラスのコンストラクタにもコントローラを渡す
        super(controller);
    }

    /*
     * 関数名:init
     * 概要　:クラスの初期化関数
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
     * 関数名：job
     * 概要  :クラス特有の処理を行う関数
     * 引数  :String $jsonString:JSON文字列
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:T.Masuda
     * 作成日:2015.0728
     */
    @Override
    public void job(String jsonString) throws ParseException, SQLException, IOException {
        //親クラスのjobを実行し、メンバにJSONの連想配列を格納する。
        super.job(jsonString);
        // ステートメントを取得する
        Statement statement = (Statement) mDbConnect.createStatement();
        //SQLによる例外の対処のためtryブロックで囲む
        try {
            // クエリを実行する
            ResultSet resultSet = statement.executeQuery(DB_SETQUERY);
        // SQL例外を監視する
        } catch (SQLException e) {
            // 例外が発生したらログを出力するためのオブジェクトを作成する
            Logger logger = Logger.getAnonymousLogger();
            // ログを出力する
            logger.log(null, e.getMessage());
        }
        //最後に行う処理
        mDbHandler = null;
        //クライアントへ返すメッセージを作成する。
        String returnMessage = "{\"message\":\"" + mProcessedRecords + "\"}";
        // 作成したJson文字列を出力する
        mController.sendJsonData(returnMessage);
    }

    /*
     * 関数名：run
     * 概要  :クラスのinit、job関数をまとめて実行する。
     * 引数  :String $jsonString:JSON文字列。
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.nishiwaki
     * 作成日:2018.04.10
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
