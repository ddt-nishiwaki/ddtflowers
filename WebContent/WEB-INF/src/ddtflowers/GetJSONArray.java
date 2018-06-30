package ddtflowers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.simple.parser.ParseException;

/*
 * クラス名：SaveJSONRecord.php
 *
 * 概要　:JSONの内容をもとにクエリを実行し、DBのレコードを追加、更新、削除する。
 * 設計者:H.Kaneko
 * 作成者:S.Nishiwaki
 * 作成日:2018.04.15
 */
public class GetJSONArray implements ApplicationService{

    // POSTされたJSONを受け取るためのキーです
    private static final String POST_JSON_KEY = "json";
    // POSTされたjson文字列を取得するためのメンバです
    private HttpRequestController mController;

    /*
     * コンストラクタ
     * 概要　:コントローラ(サーブレット)クラスごと受け取って
     * 　　　:インスタンス生成します
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.15
     */
    public GetJSONArray(HttpRequestController controller) {
        // コントローラ(サーブレット)クラスを受け取る
        mController = controller;
    }

    /*
     * 関数名:doProcedure
     * 概要　:プロシージャクラスを実行します
     * 戻り値:なし
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.04.15
     */
    @Override
    public void doProcedure(){
        // POStされたJSON文字列を保持する変数です
        String jsonString;
        // JSONのパース、DB接続、データ送信に関するエラーを監視する
        try {
            // POSTされたJSON文字列を取得する
            jsonString = mController.getPostValue(POST_JSON_KEY);
            // クライアントから送られたJSONのクエリを実行し、DBへのレコード追加、変更、削除を行うオブジェクトを生成する
            ProcedureBase procedureSet = new ProcedureGetList(mController);
            // 上記機能を実行する
            procedureSet.run(jsonString);
        // JSONのパース、DB接続、データ送信に関するエラーが発生した場合の処理を行う
        } catch (ParseException | ClassNotFoundException | SQLException | IOException | NoSuchAlgorithmException e) {
            // エラーを出力する
            e.printStackTrace();
        }
    }
}
