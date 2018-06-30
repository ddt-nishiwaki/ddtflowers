package ddtflowers;

import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

/*
 * クラス名:ProcedureBase
 *
 * 概要　:procedure〜クラスの親クラスのファイル。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.03.30
 */
public class ProcedureBase extends Account{

    // jsonを返すためのオブジェクトを保持するメンバです
    HttpRequestController mController;

    // Accountクラスにサーブレットクラスを渡すためのコンストラクタです
	public ProcedureBase(HttpRequestController controller) throws ParseException {
        // jsonクライアントに返す処理を持っているため コントローラを保持する
        mController = controller;
    }

    /*
	 * 関数名:init
	 * 概要　:クラスの初期化関数。accountクラスの初期化関数とログインチェック関数をコールする。
	 * 引数　:HttpRequestController サーブレットを扱うためのオブジェクト
	 * 戻り値:なし
	 * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.30
	 */
    @Override
    public void init() throws ClassNotFoundException, SQLException, IOException {
        // Accountクラスからセッションの開始とDBへの接続を行う処理を呼び出す
        super.init();
        // ログインチェックを行う
        loginCheck();
    }

	/*
	 * 関数名:job
	 * 概要　:クラス特有の処理を行う関数。JSON文字列から連想配列を取得してメンバに格納する。
	 * 引数　:String jsonString:JSON文字列
	 * 戻り値:なし
	 * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:2018.03.30
	 */
    public void job(String jsonString) throws ParseException, SQLException, IOException{
        // 受け取ったJSON文字列をJSONObjectに変換してメンバに保持する
        getJSONMap(jsonString);
    }

	/*
	 * 関数名:run
	 * 概要　:クラスのinit、job関数をまとめて実行する。
	 * 引数　:なし
	 * 戻り値:なし
	 * 設計者:H.Kaneko
	 * 作成者:T.Masuda
	 * 作成日:2015.0728
	 */
    public void run(String jsonString) throws ClassNotFoundException, ParseException, SQLException, IOException {
        // セッション開始、DB接続、ログインチェックを行う
        init();
        // 受け取ったJSON文字列をJSONObjectに変換してメンバに保持する
        job(jsonString);
    }
}
