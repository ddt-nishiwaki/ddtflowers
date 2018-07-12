package ddtflowers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * クラス名 :JSONDBManager
 * 概要 :JSONにDBから取得した値を与える、またはJSONのデータをDBに保存する役割のクラス
 * 設計者 :H.Kaneko
 * 作成者 :S.Nihsiwaki
 * 作成日 :2018.02.03
 */
public class JSONDBManager extends DbConnect {

    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // データベースのホスト設定です。
    private static final String   DB_HOST                 = "localhost";
    // 使用するデータベースの設定です。
    private static final String   DB_NAME                 = "ddthink-com00006";
    // データベースのユーザー設定です。
    private static final String   DB_USER                 = "root";
    // データベースのパスワード設定です。
    private static final String   DB_PASSWORD             = "";
    // データベースの文字コード設定です。
    private static final String   DB_CHARSET              = "UTF-8";
    // 使用するデータベースの種類です。
    private static final String   DB_TYPE                 = "mysql";
    // データベース接続に使用するドライバー名です。
    private static final String   DB_DRIVER               = "jdbc";
    // JDBCドライバーで接続するためのURL設定です。
    private static final String   DB_URL                  = DB_DRIVER + ":" + DB_TYPE + "://" + DB_HOST + "/" + DB_NAME;
    //DB上の文字列の扱いをUTF8にするクエリーです
    private static final String   CHARSET_UTF8            = "SET NAMES utf8";
    // 初期化用の空文字です
    private static final String   NULL_STRING             = "";
    // JSONのデータの区切り文字です
    private static final String   COMMA_DELIMITER         = ",";
    // 文字列としてのバックスラッシュです
    private static final String   STRING_BACKSLASH        = "\\\\";
    // Unix系改行コードです
    private static final String   LINE_FEED               = "\n";
    // 旧Mac系改行コードです
    private static final String   CARRIAGE_RETURN         = "\r";
    // DOS系改行コードです
    private static final String   DOS_RETURN              = "\r\n";
    // LINE_FEEDを示す文字列です
    private static final String   STRING_LINE_FEED        = "\\\\n";
    // ダブルクォートを示す文字列です
    private static final String   STRING_DOUBLE_QUOTES    = "\"";
    // JSONのdb_getQueryキーを示す文字列です
    private static final String   DB_GETQUERY             = "db_getQuery";
    // JSONのdb_setQueryキーを示す文字列です
    private static final String   DB_SETQUERY             = "db_setQuery";
    // JSONのdb_columnキーを示す文字列です
    private static final String   DB_COLUMN               = "db_column";
    // JSONのtextキーを示す文字列です
    private static final String   KEY_TEXT                = "text";
    // JSONのhtmlキーを示す文字列です
    private static final String   KEY_HTML                = "html";
    // JSONのsrcキーを示す文字列です
    private static final String   KEY_SRC                 = "src";
    // JSONのvalueキーを示す文字列です
    private static final String   KEY_VALUE               = "value";
    // JSONのpasswordキーを示す文字列です
    private static final String   KEY_PASSWORD            = "password";
    // キー文字列を分割する文字列です
    private static final String   STR_TWO_UNDERBAR        = "__";
    // 分割されたーキー文字列の後方をしめすインデックスです
    private static final int      AFTER_UNDERBER_INDEX    = 1;
    // クエリー文字列で使用するシングルクォートです
    private static final String   QUERY_SINGLE_QUOTES     = "'";
    // 文字列からセミコロンを探す為の正規表現です
    private static final String   REGEXP_SEARCH_SEMICOLON = ".*;.*";
    // 正規表現のORを示すメタ文字です
    private static final String   REGEXP_OR               = "|";
    // セミコロンを示す文字列です
    private static final String   STRING_SEMICOLON        = ";";
    // コロンを示す文字列です
    private static final String   STRING_COLON            = ":";
    // 最初のインデックスを示す値です
    private static final int      FIRST_INDEX             = 1;
    // 結果セットの初回インデックスを示す値です
    private static final int      START_RESULT_INDEX      = 1;
    // 存在しないインデックスを示す値です
    private static final int      NOT_INDEX               = -1;
    // 次のインデックスを指定する値です
    private static final int      SHIFT_NEXT_INDEX        = 1;
    // クエリー文字列としての最小の長さです
    private static final int      QUERY_MINIMUM_LENGTH    = 1;
    // データのブロック開始を示す文字です
    private static final String   START_BLOCK             = "{";
    // データのブロック終了を示す文字です
    private static final String   END_BLOCK               = "}";
    // データの開始を示す文字です
    private static final String   START_DATA              = "[";
    // データの終了を示す文字です
    private static final String   END_DATA                = "]";
    // JSONの値を入れるノードのキーの文字列配列です
    private static final String[] KEY_LIST                = { KEY_TEXT, KEY_HTML, KEY_SRC };
    // JSONの会員番号列キーを示す文字列です
    private static final String   COLUMN_NAME_USER_KEY    = "user_key";
    // 辞書型ではないことを示す値です
    private static final boolean  NOT_HASH                = false;
    // 辞書型であることを示す値です
    private static final boolean  EXISTS_HASH             = true;
    // 検索した値が存在しないことを示す値です
    private static final boolean  NOT_MATCH               = false;
    // 検索した値が存在することを示す値です
    private static final boolean  EXISTS_MATCH            = true;
    // 処理に異常が発生した場合のエラーコードです
    private static final int      ERROR_CODE              = 1;

    //////////////////////////////////////
    // member
    //////////////////////////////////////
    // DBへの追加、更新処理を行ったときに帰ってくる処理レコード数の数値を格納するメンバです
    public int                    mProcessedRecords       = 0;
    // JSONを変換したJSONObjectを格納するメンバです
    public JSONObject             mJsonObject             = null;

    //////////////////////////////////////
    // public method
    //////////////////////////////////////
    /**
     * fig :Fig0-0
     * 関数名:createJSON
     * 概要　:DBからデータを取得してJSONを作る
     * 　　　:引数に指定のキーがなければnull文字列として引数を設定する
     * 引数　:JSONObject jsonObject:カレントのJSON
     * 返り値:void
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.02.03
     * @throws NoSuchAlgorithmException
     */
    public void createJSON(JSONObject jsonObject) throws SQLException, NoSuchAlgorithmException {
        // 引数に指定のキーがなければnull文字列として引数を設定する
        String nullString = NULL_STRING;
        // 親のリザルトツリーの型だけ設定したものを引数に渡して実行する
        createJSON(jsonObject, nullString);
    }
    /**
     * fig :Fig0-1
     * 関数名:createJSON
     * 概要　:DBからデータを取得してJSONを作る
     * 　　　:初回実行時は親のリザルトツリーがないものとして実行させる
     * 引数　:JSONObject jsonObject:カレントのJSON
     * 引数　:String key:JSONのキー
     * 返り値:void
     * 設計者:S.Nishiwaki
     * 作成者:S.Nishiwaki
     * 作成日:2018.02.03
     * @throws NoSuchAlgorithmException
     */
    public void createJSON(JSONObject jsonObject, String key) throws SQLException, NoSuchAlgorithmException {
        // 引数に親のリザルトツリーがなければnullとして引数を設定する
        DbResultTree notExistTree = null;
        // 親のリザルトツリーの型だけ設定したものを引数に渡して実行する
        createJSON(jsonObject, key, notExistTree);
    }

    /**
     * fig :Fig0-2
     * 関数名 :createJSON
     * 概要 :DBからデータを取得してJSONを作る
     * 引数 :JSONObject jsonObject:カレントのJSON
     * 引数 :String key:JSONのキー
     * 引数 :DbResultTree:parentTree:DBから取得したデータを格納してツリー構造を作るためのクラスのインスタンス
     * 返り値 :void
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2018.02.03
     * @throws NoSuchAlgorithmException
     */
    public void createJSON(JSONObject jsonObject, String key, DbResultTree parentTree) throws SQLException, NoSuchAlgorithmException {
        // DBの結果から構築したツリーを構成するクラスのインスタンスを生成する
        DbResultTree dbResultTree = new DbResultTree();
        // ステートメントを作成する
        dbResultTree.dbResultSet = executeQuery(jsonObject, DB_GETQUERY);
        // DB_ResultTreeの親子関係を構築する
        dbResultTree.parentTree = parentTree;
        // カレントのJSONを保存する
        dbResultTree.jsonObject = jsonObject;
        // カレントのキーを保存する
        dbResultTree.keyData = key;

        // db_resultTreeから”key”に該当するデータを取得する
        String column = getDBColumn(key, dbResultTree);
        // jsonについて最下層の要素にたどり着くまでループしてデータを取得する
        for (Object keyObject : jsonObject.keySet()) {
            // データを取り出すためのkey文字列を取得する
            String keyString = keyObject.toString();
            // key文字列に対するデータを取得する
            if(isHash(jsonObject.get(keyString))) {
                JSONObject valueObject;
                valueObject = (JSONObject) jsonObject.get(keyString);
                // valueに子供がある時の処理(valueの型がオブジェクトの時の処理)
                if (!(valueObject.isEmpty()) && isHash(valueObject)) {
                    // fig0 再帰的にcreateJSONメソッドをコールする
                    createJSON(valueObject, keyString, dbResultTree);
                }
            } else if (column != null && (keyString.equals(KEY_TEXT)) || keyString.equals(KEY_SRC)) {
                jsonObject.put(keyString, column);
            }
        }
    }

    /**
     * fig :Fig1
     * 関数名 :executeQuery
     * 概要 :クエリを実行してDBから結果セットを取得する。
     * 引数 :JSONObject jsonObject :カレントのJSON連想配列
     * 引数 :String queryKey :実行するクエリのベースとなる文字列
     * 返り値 :JSONArray resultSetArray :DBから取得した結果セットを返す。
     * 設計者 :H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2018.02.03
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    @SuppressWarnings("unchecked")
    public ResultSet executeQuery(JSONObject jsonObject, String queryKey) throws SQLException, NoSuchAlgorithmException {
        // 返却する結果セットの変数を作成する
        ResultSet resultSet = null;
        //ユーザ情報を保護するためパスワードがkeyにあればハッシュ化する
        if (jsonObject.containsKey(KEY_PASSWORD)) {
            // パスワードをハッシュ化したデータで上書きする
            JSONObject passObj = ((JSONObject) jsonObject.get(KEY_PASSWORD));
            Object pass = passObj.get(KEY_VALUE);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytePassword = digest.digest(pass.toString().getBytes());
            String hashedPassword = DatatypeConverter.printHexBinary(hashBytePassword).toLowerCase();
            passObj.put(//
                KEY_VALUE,
                hashedPassword
            );
        }
        // $queryKeyが$jsonに存在していれば$queryに値を入れる
        if (jsonObject.containsKey(queryKey)) {
            // カレントjsonから"queryKey"を持つキーを取得する
            String query = jsonObject.get(queryKey).toString();
            // queryに正しい値が入っていれば
            if (query != null && query.length() >= QUERY_MINIMUM_LENGTH) {
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
                if (query.matches(REGEXP_SEARCH_SEMICOLON) != NOT_MATCH) {
                    // SQL1行に2種類以上含まれている場合動作しないため、後半のSQLを削除する
                    query = query.substring(0, query.indexOf(STRING_SEMICOLON) + SHIFT_NEXT_INDEX);
                }
                // ステートメントを取得する
                Statement statement = (Statement) mDbConnect.createStatement();
                // クエリを実行する
                resultSet = statement.executeQuery(query);
                // 件数を取得するために結果セットを最後の行に移動させる
                resultSet.last();
                // 処理を行ったレコード数を結果セットより取得してメンバに保存する
                mProcessedRecords = resultSet.getRow(); // 2016.09.20 r.shibata rowCountから取得していたものを修正(指示:金子)
                // 結果セットを最初の行に戻す
                resultSet.beforeFirst();
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
     * 作成日 :2018.02.03
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
            returnReplaceString += (returnReplaceString == NULL_STRING) ? NULL_STRING
                    : QUERY_SINGLE_QUOTES + COMMA_DELIMITER + QUERY_SINGLE_QUOTES;
            //配列の文字列を、エスケープ処理を行い置換文字列に付与する
            returnReplaceString += value.replace(QUERY_SINGLE_QUOTES,
                    STRING_BACKSLASH + STRING_BACKSLASH + QUERY_SINGLE_QUOTES);
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
     * 作成日 :2018.02.03
     */
    public String getDBColumn(String keyString, DbResultTree dbResultTree) throws SQLException {
        // 返却値を格納する変数を初期化する
        String column = null;
        // 取得対象が列の何行目かをセットする
        int columnNumber = FIRST_INDEX;
        // dbrTreeの親のキーが、これが配列の要素であるということを示す~の文字を含んでいれば
        if (dbResultTree.parentTree != null && dbResultTree.parentTree.keyData.indexOf(STR_TWO_UNDERBAR) != NOT_INDEX) {
            //keyを~を境に分離する
            String[] keyStringArray = dbResultTree.parentTree.keyData.split(STR_TWO_UNDERBAR);
            //デミリタを元に行数のトークンに分ける
            columnNumber = Integer.parseInt(keyStringArray[AFTER_UNDERBER_INDEX]); //行数をセットする
        }
        // 親がなくなるまでDBレコードツリーを走査する
        while (dbResultTree != null) {
            // dbrTreeに結果セットが登録されていれば
            if (checkColumn(dbResultTree.dbResultSet, keyString)) {
                // 行のカーソルの位置を最初の行に合わせる
                dbResultTree.dbResultSet.beforeFirst();
                // カラムの値を取得する
                while(dbResultTree.dbResultSet.next()){
                    column = dbResultTree.dbResultSet.getString(keyString);
                }
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
     * 作成日 :2018.02.03
     */
    public String getListJSON(JSONObject jsonObject) throws SQLException, NoSuchAlgorithmException {
        // 返却値としてJSON文字列を作成するための変数を設定する
        String stringAll = NULL_STRING;
        // 各レコードのデータ設定用変数を設定する
        String stringBlock = NULL_STRING;
        // 各フィールドのデータ設定用変数を設定する
        String stringLine = NULL_STRING;
        // JSONに格納されているクエリー文字列の結果セットを取得する fig 2-2 executeQuery
        ResultSet resultSet = executeQuery(jsonObject, DB_GETQUERY);
        // 結果セットのレコード数を取得する
        int resultCount = mProcessedRecords;
        // 取得したカラムの数を確認する為のデータを取得します
        ResultSetMetaData resultSetMetaData = (ResultSetMetaData) resultSet.getMetaData();
        // クエリ結果出力の為、取得したカラムの数を取得します
        int columnLength = resultSetMetaData.getColumnCount();

        // 結果セットのレコード(行)を走査する
        while (resultSet.next()) {
            // レコード文字列を空文字で初期化する
            stringLine = NULL_STRING;
            // 結果セットのフィールド(列)を走査する
            for (int columnCount = START_RESULT_INDEX; columnCount < columnLength; columnCount++) {
                // 列名を取得する
                String columnName = resultSetMetaData.getColumnName(columnCount);
                // 列のデータを取得する
                String columnValue = resultSet.getString(columnCount);
                // フィールドデータ設定用の変数が空文字以外の場合
                if (stringLine != NULL_STRING) {
                    // JSONデータの区切り文字としてカンマを追加する
                    stringLine += COMMA_DELIMITER;
                }
                // 出力時、バックスラッシュによる誤作動を防ぐための置換を行う
                columnValue = columnValue.replace(STRING_BACKSLASH, STRING_BACKSLASH + STRING_BACKSLASH);
                // JSONデータに改行コードを保持するための置換を行う
                columnValue = columnValue.replaceAll(DOS_RETURN + REGEXP_OR + CARRIAGE_RETURN + REGEXP_OR + LINE_FEED,
                        STRING_LINE_FEED);
                // JSONデータが壊れないようにダブルクォートをエスケープする
                columnValue = columnValue.replace(STRING_DOUBLE_QUOTES, STRING_BACKSLASH + STRING_DOUBLE_QUOTES);
                // フィールドデータ設定用変数にエスケープした文字列を対応するキーで設定して追加する
                stringLine += STRING_DOUBLE_QUOTES + columnName + STRING_DOUBLE_QUOTES + STRING_COLON
                        + STRING_DOUBLE_QUOTES + columnValue + STRING_DOUBLE_QUOTES;
            }
            // レコードデータ設定用変数が空文字の場合はカンマで区切る
            stringBlock += (stringBlock != NULL_STRING) ? COMMA_DELIMITER : NULL_STRING;
            // レコードに対する各フィールドデータをブロックで囲む
            stringBlock += START_BLOCK + stringLine + END_BLOCK;
        }
        // 作成した各レコードのデータをJSONオブジェクトの形にまとめる
        stringAll = START_DATA + stringBlock + END_DATA;
        // 作成した書くレコードのデータをJSON文字列の形にまとめる
        // 作成したJSON文字列を返す
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
     * 作成日 :2018.02.03
     */
    public void outputJSON(String jsonString, String keyString) throws ParseException, NoSuchAlgorithmException {
        // JSON文字列をJSONオブジェクトに変換する fig2-7 getJSONMap
        getJSONMap(jsonString);
        // DBとの接続を試みる
        try {
            mDbConnect = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // 文字コード設定を行うクエリ発行のため、ステートメントを取得する
            mDbHandler = mDbConnect.createStatement();
            // クエリの文字コード設定をUTF8に設定する
            mDbHandler.executeQuery(CHARSET_UTF8);
            // JSONオブジェクトの各キーに値を設定する fig2-1 createJSON
            createJSON(mJsonObject, keyString, null);
            // DBとの接続を切る
            mDbConnect = null;
        } catch (SQLException error) {
            // エラーメッセージを表示する
            error.printStackTrace();
            // 最後に必ず行う
            System.exit(ERROR_CODE);
        }
    }

    /*
     * Fig5
     * 関数名:getJSONMap
     * 概要  :JSON文字列から連想配列を生成する。
     * 引数  :String jsonString:変換するJSON文字列
     * 返却値:なし
     * 設計者:H.Kaneko
     * 作成者 :S.Nishiwaki
     * 作成日 :2018.02.03
     */
    public void getJSONMap(String jsonString) throws ParseException {
        // JSON文字列をJSONオブジェクトに変換する為のオブジェクトを作成する
        JSONParser parser = new JSONParser();
        // JSON文字列をJSONオブジェクトに変換する
        JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
        // 変換したJSONオブジェクトをJSONDBManagerクラスのメンバに格納する
        mJsonObject = jsonObject;
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
     * 作成日 :2018.02.03
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
            for (int columnCount = 0; columnCount < columnLength; columnCount++) {
                // 検索列名が結果セットにあった場合の処理を行う
                if (columnData.getColumnName(columnCount + 1).equals(columnName)) {
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
            // 配列ののような辞書型データをチェックするためのカウンターを初期化する
            int likeArrayChecker = FIRST_INDEX;
            // 引数の配列についてループする
            for (Object keyObject : ((JSONObject) valueObject).keySet()) {
                // データを取り出すためのkey文字列を取得する
                String keyString = keyObject.toString();
                // 配列のキーが数字でないとき
                if (!NumberUtils.isNumber(keyString) || Integer.parseInt(keyString) != likeArrayChecker++) {
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
     * 作成日 :2018.02.03
     */
    public String getListJSONPlusKey(JSONObject jsonObject, String keyString) throws ParseException, SQLException, NoSuchAlgorithmException {
        // 文字列をデコードするためのパーサーを作成する
        JSONParser parser = new JSONParser();
        // JSONに設定されたクエリーからテーブルデータを取得する fig2-5 getListJSON
        String tableJsonString = getListJSON(jsonObject);
        // 取得したデータをJSONに追加する
        mJsonObject.put(keyString, parser.parse(tableJsonString));
        //追加を行った引数のJSONを文字列に変換する
        tableJsonString = mJsonObject.toJSONString(mJsonObject);
        // 追加したJSONを文字列として返す
        return tableJsonString;
    }
}
