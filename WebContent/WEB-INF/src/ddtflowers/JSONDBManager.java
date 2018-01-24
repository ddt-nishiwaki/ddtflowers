package ddtflowers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mysql.jdbc.ResultSetMetaData;

/**
 * クラス名 :JSONDBManager
 * 概要 :JSONにDBから取得した値を与える、またはJSONのデータをDBに保存する役割のクラス
 * 設計者 :H.Kaneko
 * 作成者 :S.Nihsiwaki
 * 作成日 :2017.12.xx
 */
public class JSONDBManager {

    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // JSONのdb_getQueryキーの文字列を定数にセットする
    private static final String   DB_GETQUERY          = "db_getQuery";
    // JSONのdb_setQueryキーの文字列を定数にセットする
    private static final String   DB_SETQUERY          = "db_setQuery";
    // JSONのdb_columnキーの文字列を定数にセットする
    private static final String   DB_COLUMN            = "db_column";
    // JSONのtextキーの文字列を定数にセットする
    private static final String   KEY_TEXT             = "text";
    // JSONのhtmlキーの文字列を定数にセットする
    private static final String   KEY_HTML             = "html";
    // JSONのsrcキーの文字列を定数にセットする
    private static final String   KEY_SRC              = "src";
    // JSONのvalueキーの文字列を定数にセットする
    private static final String   KEY_VALUE            = "value";
    // アンダーバー二つを定数に入れる
    private static final String   STR_TWO_UNDERBAR     = "__";
    // JSONの値を入れるノードのキーの文字列リストを配列にセットする
    private static final String[] KEY_LIST             = { KEY_TEXT, KEY_HTML, KEY_SRC };
    // 会員番号列を定数に入れる
    private static final String   COLUMN_NAME_USER_KEY = "user_key";
    // 辞書型ではないことを示す値を設定する
    private static final boolean  NOT_HASH             = false;
    // 辞書型であることを示す値を設定する
    private static final boolean  EXISTS_HASH          = true;
    // 検索した値が存在しないことを示す値を設定する
    private static final boolean  NOT_MATCH            = false;
    // 検索した値が存在することを示す値を設定する
    private static final boolean  EXISTS_MATCH         = true;

    //////////////////////////////////////
    // member
    //////////////////////////////////////
    // DBへの追加、更新処理を行ったときに帰ってくる処理レコード数の数値を格納するメンバ
    public int                    processedRecords     = 0;
    // JSONを変換した連想配列を格納する
    public JSONArray              jsonArray            = null;

    //////////////////////////////////////
    // public method
    //////////////////////////////////////
    /**
     * fig :Fig0
     * 関数名 :createJSON
     * 概要 :DBからデータを取得してJSONを作る
     * 引数 :jsonArray:カレントのJSON
     * 引数 :String key:JSONのキー
     * 引数 :DBResultTree:parentTree:DBから取得したデータを格納してツリー構造を作るためのクラスのインスタンス
     * 返り値 :void
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    // public void createJSON(JSONArray jsonArray, String keyString, DbResultTree parentTree) {
    // }

    /**
     * fig :Fig1
     * 関数名 :executeQuery
     * 概要 :クエリを実行してDBから結果セットを取得する。
     * 引数 :JSONArray jsonArray :カレントのJSON連想配列
     * 引数 :String queryKey :実行するクエリのベースとなる文字列
     * 返り値 :JSONArray resultSetArray :DBから取得した結果セットを返す。
     * 設計者 :H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public JSONArray executeQuery(JSONArray jsonArray, String queryKey) {
        JSONArray resultSetArray = jsonArray;
        return resultSetArray;
    }

    /*
     * 関数名:createReplaceValue
     * 概要  :クライアントより受け取った置換対象の値を、置換可能文字列に変換する
     * 引数  :Object childJsonObject:クライアントより受け取った置換対象の値、StringとArrayが存在する
     * 返り値:String returnReplaceString :作成した返却用の文字列
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public static String createReplaceValue(Object childJsonObject) {
        // 受け取ったオブジェクトにより、返却する文字列を作成するための変数を宣言する
        String returnReplaceString = "";
        // データ作成のための文字列配列を用意する
        ArrayList<String> childObjectArray = new ArrayList<String>();
        //取得した引数が配列であれば
        if (childJsonObject instanceof JSONArray) {
            //走査用文字列配列として引数をセットする
            childObjectArray = (JSONArray) childJsonObject;
            //配列以外であれば
        } else {
            //査用文字列配列に引数の値を追加する
            childObjectArray.add(childJsonObject.toString());
        }
        //取得、作成した配列を走査する
        for (String value : childObjectArray) {
            //置換文字列が空白であれば何もしない、値があれば区切り文字を付与する
            returnReplaceString += returnReplaceString == "" ? "" : "','";
            //配列の文字列を、エスケープ処理を行い置換文字列に付与する
            returnReplaceString += value.replace("'", "\\\'");
        }
        //作成した文字列を返却する
        return returnReplaceString;
    }

    /*
     * Fig2
     * 関数名:getDBColumn
     * 概要  :指定したkey(列)の値を結果セットから取得して返す。
     * 引数  :String key:JSONのオブジェクトのキー
     * 引数  :DBResultTree dbrTree:DBから取得した結果をツリー構造にするクラスのインスタンス
     * 返却値:String column:取得した列の値を返す
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    // public String getDBColumn(String key, DbResultTree dbResultTree) {
    //    String column = "";
    //    return column;
    // }

    /*
     * Fig3
     * 関数名：getListJSON
     * 概要  :リスト形式のJSONを作成して返す
     * 引数  :JSONObject json:JSONのオブジェクト。
     * 返却値 :String stringAll:JSONの文字列配列を文字列で返す
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public String getListJSON(JSONObject jsonObject) {
        String stringAll = "";
        return stringAll;
    }

    /*
     * Fig4
     * 関数名:outputJSON
     * 概要  :DBから取得したレコードでJSONを作る。
     * 引数  :String jsonString:クライアントから受け取ったJSON文字列
     * 引数  :String key:JSONのトップのノードのキー。
     * 返却値:なし
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public void outputJSON(String jsonString, String key) {
    }

    /*
     * Fig5
     * 関数名:getJSONMap
     * 概要  :JSON文字列から連想配列を生成する。
     * 引数  :String jsonString:変換するJSON文字列
     * 返却値:なし
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public void getJSONMap(String jsonString) {
    }

    /*
     * Fig8
     * 関数名:checkColumn
     * 概要  :結果セットに指定した列名を持つ列があるかをチェックする
     * 引数  :ResultSet resultSet:指定した列があるかをチェックする対象の結果セット
     * 引数  :String columnName:チェック対象の列名
     * 返却値:boolean:列の存在を判定して返す
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.01.28
     */
    public boolean checkColumn(ResultSet resultSet, String columnName) throws SQLException {
        // 返却用の真理値の変数を宣言、falseで初期化する
        boolean isColumn = NOT_MATCH;
        // 結果セットがnullでない時の処理
        if (resultSet != null) {
            // 検索対象の列名に関するデータを取得する
            ResultSetMetaData columnData = (ResultSetMetaData) resultSet.getMetaData();
            // 検索のために列名の数を取得する
            int columnLength = columnData.getColumnCount();
            // 列名データから検索列名を探す
            for (int columnCount = 1; columnCount < columnLength; columnCount++) {
                // 検索列名が結果セットにあった場合の処理を行う
                if (columnData.getColumnName(columnCount).equals(columnName)) {
                    // 存在することを示す値を設定する
                    isColumn = EXISTS_MATCH;
                    // 検索を終了する
                    break;
                }
            }
        }
        // 指定した列の存在判定を返す
        return isColumn;
    }

    /*
     * Fig9
     * 関数名：isHash
     * 概要  :引数を配列であるか連想配列であるか判定する
     * 引数  :Object jsonObject:判定するJSONデータ
     * 返却値:boolean:列の型を判定して返す。trueが連想配列、falseが配列
     * 設計者:http://kihon-no-ki.com/is-hash-or-associative-array
     * 作成者 :S.Nishiwaki
     * 作成日 :2018.01.25
     */
    public boolean isHash(Object valueObject) {
        // 文字列が辞書形式かどうかを判定する
        if (valueObject instanceof JSONObject) {
            // カウンター変数を0で初期化する
            int i = 0;
            // 引数の配列についてループする
            for (Object keyObject : ((JSONObject) valueObject).keySet()) {
                // データを取り出すためのkey文字列を取得する
                String keyString = keyObject.toString();
                // 配列のキーが数字でないとき
                if (!NumberUtils.isNumber(keyString) || Integer.parseInt(keyString) != i++) {
                    // 連想配列なのでtrueを返す
                    return EXISTS_HASH;
                }
            }
        }
        //連想配列ではないのでfalseを返す
        return NOT_HASH;
    }

    /*
     * Fig.10
     * 関数名:String getListJSONPlusKey(Object $json, String $key)
     * 概要  :getListJSONで作成した配列を、クライアントから送信されたJSONに格納して文字列で返す。
     * 引数  :Object jsonObject:JSONのオブジェクト。
     * 　　  :String key:キー名
     * 返却値  :String jsonStringPlusKey:オブジェクトで囲んだ配列のJSON文字列を返す
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     */
    public String getListJSONPlusKey(JSONObject jsonObject, String key) {
        String jsonStringPlusKey = "";
        return jsonStringPlusKey;
    }
}
