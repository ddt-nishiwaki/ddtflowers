package ddtflowers;

import java.sql.ResultSet;

import com.mysql.jdbc.ResultSetMetaData;

/**
 * JSONDBManagerのcheckColumnメソッドの動作確認を行うクラス
 */
public class TestCheckColumn {

    /**********************************************
     * テスト出力用の文字列を定義します
     *********************************************/
    // isHashメソッドのテストを行うメッセージです
    public static final String   MESSAGE_TEST_START        = "checkColumnメソッドのテストを行います";
    // テスト結果の出力だとわかるようにする為の水平線です
    public static final String   OUTPUT_SEPARATOR          = "-------------------------------";
    // 出力内容を示すメッセージです
    public static final String   FORMAT_SHOW_TARGET_DATA   = "検索対象の結果セット列名一覧を出力します";
    // 判定結果を出力するためのフォーマットです
    public static final String   FORMAT_RESULT_SEARCH      = "%s は%s\n";
    // カラムが見つかったことを示すメッセージです
    public static final String   MESSAGE_RESULT_MATCH      = "データ上に存在します";
    // カラムが見つからないことを示すメッセージです
    public static final String   MESSAGE_RESULT_NOT_MATCH  = "見つかりませんでした";
    // テストデータ取得用のクエリーです
    public static final String   TEST_DATA_QUERY           = "SELECT * FROM `user_inf` WHERE 1 LIMIT 0, 1";
    // テスト用の検索カラム「hoge」です
    public static final String   SEARCH_STRING_HOGE        = "hoge";
    // テスト用の検索カラム「update_user」です
    public static final String   SEARCH_STRING_UPDATE_USER = "update_user";
    // テスト用の検索カラム「payment_level4」です
    public static final String   SEARCH_STRING_PAYMENT4    = "payment_level4";
    // テスト用の検索カラムの一覧です
    public static final String[] SEARCH_COLUMN_LIST        = { SEARCH_STRING_HOGE, SEARCH_STRING_UPDATE_USER,
            SEARCH_STRING_PAYMENT4 };

    /**
     * JSONDBManagerのcheckColumnメソッドの動作確認を行います
     * 結果セットのデータから検索カラムがあるかどうかを出力します
     */
    public static void main(String arg[]) throws Exception {
        /**********************************************
         * 動作テストの準備を行います
         *********************************************/
        // テスト結果を受け取る変数です
        boolean isColumn;
        // テスト用結果セットを受け取る為のオブジェクトを作成します。
        DbConnect dbConnect = new DbConnect();
        // テストするメソッドを持つオブジェクトを作成します
        JSONDBManager jsonDbManager = new JSONDBManager();
        // 結果セット取得のためDBに接続します
        dbConnect.connect();
        // テスト用に結果セットを受け取ります
        ResultSet resultSet = dbConnect.mDbHandler.executeQuery(TEST_DATA_QUERY);
        /**********************************************
         * 動作テストを行います
         *********************************************/
        // 取得したカラムの数を確認する為のデータを取得します
        ResultSetMetaData resultSetMetaData = (ResultSetMetaData) resultSet.getMetaData();
        // クエリ結果出力の為、取得したカラムの数を取得します
        int columnLength = resultSetMetaData.getColumnCount();
        // 検索対象を出力する旨を出力します
        System.out.println(FORMAT_SHOW_TARGET_DATA);
        // 結果セットのカラムを走査します
        for (int columnCount = 1; columnCount < columnLength; columnCount++) {
            // 各カラム名を出力します
            System.out.println("\t" + resultSetMetaData.getColumnName(columnCount));
        }
        // テスト結果が見やすくなるように区切り線を出力します
        System.out.println(OUTPUT_SEPARATOR);
        // 検索一覧を走査するため、検索数を取得します
        int searchColumnLength = SEARCH_COLUMN_LIST.length;
        // 検索一覧を走査してテストメソッドの動作確認を行います
        for (int outputCount = 0; outputCount < searchColumnLength; outputCount++) {
            // 検索結果を取得します
            isColumn = jsonDbManager.checkColumn(resultSet, SEARCH_COLUMN_LIST[outputCount]);
            // 検索カラムがマッチした場合の処理を行う
            if (isColumn) {
                // 検索結果を表示します
                System.out.printf(FORMAT_RESULT_SEARCH, SEARCH_COLUMN_LIST[outputCount], MESSAGE_RESULT_MATCH);
                // 検索カラムが見つからない場合の処理を行う
            } else {
                // 検索結果を表示します
                System.out.printf(FORMAT_RESULT_SEARCH, SEARCH_COLUMN_LIST[outputCount], MESSAGE_RESULT_NOT_MATCH);
            }

        }
    }
}
