package ddtflowers;

import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

/*
 * クラス名:ProcedureLogin
 * 概要	:ログインの手続きを行うクラスのファイル。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.04.10
 */
public class ProcedureLogin extends ProcedureBase{

    ///////////////////////////////////////////////////////////////////////////
    // コンストラクタ
    ///////////////////////////////////////////////////////////////////////////
    // アカウントにコントローラを渡すためコントローラを受け取る
    public ProcedureLogin(HttpRequestController controller) throws ParseException {
        // 親クラスのコンストラクタにもコントローラを渡す
        super(controller);
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
     * 概要　:クラス特有の処理を行う関数。ログイン処理を行う。
     * 引数　:String $jsonString:JSON文字列
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2015.0728
     */
    @Override
    public void job(String jsonString) throws ParseException, SQLException, IOException {
        //ログインを実行する。
        login(jsonString);
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
