package ddtflowers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

/*
 * クラス名:ProcedureLogin
 * 概要	:ログインの手続きを行うクラスのファイル。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.04.10
 */
public class ProcedureGet extends ProcedureBase{

    // クライアントにデータを渡すためのメンバです
    private HttpRequestController mController;

    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    // アカウントにコントローラを渡すためコントローラを受け取る
    public ProcedureGet(HttpRequestController controller) throws ParseException {
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
    public boolean init(HttpRequestController controller) throws  ClassNotFoundException, SQLException, IOException, ParseException, NoSuchAlgorithmException{
        //親クラスのinit関数をコールする。
        return super.init(controller);
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
    public void job(String jsonString) throws ParseException, SQLException, IOException, NoSuchAlgorithmException {
        //親クラスのjobを実行し、メンバにJSONオブジェクトを格納する。
        super.job(jsonString);
        // SQLによる例外の対処のためtryブロックで囲む
        try {
            // JSON文字列の作成を行う
            createJSON(mJsonObject);
        // SQL例外を監視する
        } catch (SQLException e) {
            // 例外が発生したらログを出力するためのオブジェクトを作成する
            Logger logger = Logger.getAnonymousLogger();
            // ログを出力する
            logger.log(null, e.getMessage());
        }
		//DBとの接続を閉じる
		disconnect();
		// 連想配列をjsonに変換して変数に入れる
		String jsonOutputString = mJsonObject.toJSONString();
		// 作成したJSON文字列をクライアントに渡す
		mController.sendJsonData(jsonOutputString);
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
    public void run(String jsonString) throws ClassNotFoundException, SQLException, IOException, ParseException, NoSuchAlgorithmException {
        // 初期化処理を行う
        if(init(mController)) {
            // アプリのロジックを実行する
            job(jsonString);
        }
    }
}
