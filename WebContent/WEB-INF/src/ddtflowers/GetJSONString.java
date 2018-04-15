package ddtflowers;

import java.io.IOException;
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
public class GetJSONString implements ApplicationService{

    // POSTされたJSONを受け取るためのキーです
    private static final String POST_JSON_KEY = "json";
    // ユーザ名を取得するキーです
    private static final String USER_NAME_KEY = "userName";
    // パスワードを取得するキーです
    private static final String PASSWORD_KEY = "password";
    // IDを取得するキーです
    private static final String ID_KEY = "id";
    // 文字列に特定のワードが存在しないことを示す値です
    private static final int NOT_EXSIST_WORD = -1;


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
    public GetJSONString(HttpRequestController controller) {
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
        ProcedureBase jsonDBGetter;
        // POStされたJSON文字列を保持する変数です
        String jsonString;
        // POSTされたJSON文字列を取得する
        jsonString = mController.getPostValue(POST_JSON_KEY);
        // 受け取ったJSONがログイン用のJSONかチェックする

        // JSONのパース、DB接続、データ送信に関するエラーを監視する
        try {
            if(jsonString.indexOf(USER_NAME_KEY) != NOT_EXSIST_WORD
                && jsonString.indexOf(PASSWORD_KEY) != NOT_EXSIST_WORD
                && jsonString.indexOf(ID_KEY) != NOT_EXSIST_WORD) {
                // チェックが正ならログイン用の処理を行うクラスのインスタンスを生成する。
                jsonDBGetter = new ProcedureLogin(mController);
            // ログイン用のJSONではない場合
            } else {
                //JSONにDBから取得したデータを追加するクラスをのインスタンスを生成する。
                jsonDBGetter = new ProcedureGet(mController);
            }
            //生成したインスタンスの処理関数を実行する。
            jsonDBGetter.run(jsonString);
        // JSONのパース、DB接続、データ送信に関するエラーが発生した場合の処理を行う
        } catch (ParseException | ClassNotFoundException | SQLException | IOException e) {
            // エラーを出力する
            e.printStackTrace();
        }
    }
}
