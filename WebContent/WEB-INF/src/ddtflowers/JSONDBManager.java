package ddtflowers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * クラス名 :JSONDBManager
 * 概要 :JSONにDBから取得した値を与える、またはJSONのデータをDBに保存する役割のクラス
 * 設計者 :H.Kaneko
 * 作成者 :S.Nihsiwaki
 * 作成日 :2017.12.xx
 */
public class JSONDBManager extends DbConnect {

    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // JSONのdb_getQueryキーの文字列を定数にセットする
    private static final String   DB_GETQUERY             = "db_getQuery";
    // JSONのdb_setQueryキーの文字列を定数にセットする
    private static final String   DB_SETQUERY             = "db_setQuery";
    // JSONのdb_columnキーの文字列を定数にセットする
    private static final String   DB_COLUMN               = "db_column";
    // JSONのtextキーの文字列を定数にセットする
    private static final String   KEY_TEXT                = "text";
    // JSONのhtmlキーの文字列を定数にセットする
    private static final String   KEY_HTML                = "html";
    // JSONのsrcキーの文字列を定数にセットする
    private static final String   KEY_SRC                 = "src";
    // JSONのvalueキーの文字列を定数にセットする
    private static final String   KEY_VALUE               = "value";
    // JSONのpasswordキーの文字列を定数にセットする
    private static final String   KEY_PASSWORD            = "password";
    // アンダーバー二つを定数に入れる
    private static final String   STR_TWO_UNDERBAR        = "__";
    // クエリー文字列で使用するシングルクォートを定数にセットする
    private static final String   QUERY_SINGLE_QUOTES     = "'";
    // 文字列からセミコロンを探す為の正規表現です
    private static final String   REGEXP_SEARCH_SEMICOLON = ".*;.*";
    // セミコロンを示す文字列を定数にセットする
    private static final String   STRING_SEMICOLON        = ";";
    // 次のインデックスを指定する為の値を設定する
    private static final int      SHIFT_NEXT_INDEX        = 1;
    // JSONの値を入れるノードのキーの文字列リストを配列にセットする
    private static final String[] KEY_LIST                = { KEY_TEXT, KEY_HTML, KEY_SRC };
    // 会員番号列を定数に入れる
    private static final String   COLUMN_NAME_USER_KEY    = "user_key";
    // 辞書型ではないことを示す値を設定する
    private static final boolean  NOT_HASH                = false;
    // 辞書型であることを示す値を設定する
    private static final boolean  EXISTS_HASH             = true;
    // 検索した値が存在しないことを示す値を設定する
    private static final boolean  NOT_MATCH               = false;
    // 検索した値が存在することを示す値を設定する
    private static final boolean  EXISTS_MATCH            = true;

    //////////////////////////////////////
    // member
    //////////////////////////////////////
    // DBへの追加、更新処理を行ったときに帰ってくる処理レコード数の数値を格納するメンバ
    public int                    processedRecords        = 0;
    // JSONを変換した連想配列を格納する
    public JSONArray              jsonArray               = null;

    //////////////////////////////////////
    // public method
    //////////////////////////////////////
    /**
     * fig :Fig0
     * 関数名 :createJSON
     * 概要 :DBからデータを取得してJSONを作る
     * 引数 :jsonObject:カレントのJSON
     * 引数 :String key:JSONのキー
     * 引数 :DbResultTree:parentTree:DBから取得したデータを格納してツリー構造を作るためのクラスのインスタンス
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
     * 引数 :JSONObject jsonObject :カレントのJSON連想配列
     * 引数 :String queryKey :実行するクエリのベースとなる文字列
     * 返り値 :JSONArray resultSetArray :DBから取得した結果セットを返す。
     * 設計者 :H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2017.12.xx
     * @throws SQLException
     */
    public ResultSet executeQuery(JSONObject jsonObject, String queryKey) throws SQLException {
        // 返却する結果セットの変数を作成する
        ResultSet resultSet = null;
        //ユーザ情報を保護するためパスワードがkeyにあればハッシュ化する
        if (jsonObject.containsKey(KEY_PASSWORD)) {
            // パスワードをハッシュ化したデータで上書きする
            ((JSONObject) jsonObject.get(KEY_PASSWORD)).put(KEY_PASSWORD, jsonObject.get(KEY_PASSWORD).hashCode());
        }
        // $queryKeyが$jsonに存在していれば$queryに値を入れる
        if (jsonObject.containsKey(queryKey)) {
            // カレントjsonから"queryKey"を持つキーを取得する
            String query = jsonObject.get(queryKey).toString();
            // queryに正しい値が入っていれば
            if (query != null && query.length() >= 1) {
                // jsonについて最下層の要素にたどり着くまでループしてデータを取り出す
                for (Object keyObject : jsonObject.keySet()) {
                    // 現在のvalueデータを扱うためにキャッシュする
                    Object valueObject = jsonObject.get(keyObject);
                    // $valueに子供がある時の処理($valueの型がオブジェクトの時の処理)
                    if (valueObject instanceof JSONObject && isHash(valueObject)) {
                        // 子オブジェクトを取得する
                        JSONObject childObject = (JSONObject) valueObject;
                        //子オブジェクトがvalueを持っていたら
                        if (childObject.containsKey(KEY_VALUE)) {
                            //SQL実行できなくなるため、置換対象の値のうち、シングルクォートをエスケープする。 2016.10.14 r.shibata 追加 2016.12.27 clientから置換文字として配列が来るため処理を追加
                            String replaceValue = createReplaceValue(childObject.get(KEY_VALUE));
                            //子オブジェクトのkey文字列と一致するqueryの文字列を置換する 置換するvalueの値を置換後の変数に変更 2016.10.14 r.shibata
                            query = query.replace(QUERY_SINGLE_QUOTES + keyObject + QUERY_SINGLE_QUOTES,
                                    QUERY_SINGLE_QUOTES + replaceValue + QUERY_SINGLE_QUOTES);
                        }
                    }
                }
                // クエリにセミコロンが含まれている場合
                if (query.matches(REGEXP_SEARCH_SEMICOLON) != false) {
                    // SQL1行に2種類以上含まれている場合動作しないため、後半のSQLを削除する
                    query = query.substring(0, query.lastIndexOf(STRING_SEMICOLON) + SHIFT_NEXT_INDEX);
                }
                // ステートメントを取得する
                Statement statement = (Statement) mDbConnect.createStatement();
                // クエリを実行する
                resultSet = statement.executeQuery(query);
                // 件数を取得するために結果セットを最後の行に移動させる
                resultSet.last();
                // 処理を行ったレコード数を結果セットより取得してメンバに保存する
                processedRecords = resultSet.getRow(); // 2016.09.20 r.shibata rowCountから取得していたものを修正(指示:金子)
                // 結果セットを最初の行に戻す
                resultSet.first();
            }
        }
        // 結果セットを返す
        return resultSet;
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
    public String getDBColumn(String keyString, DbResultTree dbResultTree) {
        // 返却値を格納する変数を初期化する
        String column = null;
        // 取得対象が列の何行目かをセットする
        int columnNumber = 0;
        // dbrTreeの親のキーが、これが配列の要素であるということを示す~の文字を含んでいれば
        if (dbResultTree.parentTree != null && dbResultTree.parentTree.keyData.indexOf(STR_TWO_UNDERBAR) != -1) {
            //keyを~を境に分離する
            String[] keyStringArray = dbResultTree.parentTree.keyData.split(STR_TWO_UNDERBAR);
            //デミリタを元に行数のトークンに分ける
            columnNumber = Integer.parseInt(keyStringArray[1]); //行数をセットする
        }
        // 親がなくなるまでDBレコードツリーを走査する
        while (dbResultTree != null) {
            // dbrTreeに結果セットが登録されていれば
            if (checkColumn(dbResultTree.dbResultObject, keyString)) {
                //カラムデータを取得する
                JSONObject columnObject = (JSONObject) dbResultTree.dbResultObject.get(columnNumber);
                //カラムの値を取得する
                column = columnObject.get(keyString).toString();
                //ループを抜ける
                break;
                // dbrTreeに結果セットが登録されていない場合は
            } else {
                // 親をセットする
                dbResultTree = dbResultTree.parentTree;
            }
        }
        //カラムの値を返す
        return column;
    }

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
