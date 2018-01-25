package ddtflowers;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JSONDBManagerのisHashメソッドの動作確認を行うクラス
 */
public class TestIsHash {
    /**********************************************
     * テスト出力用の文字列を定義します
     *********************************************/
    // isHashメソッドのテストを行うメッセージです
    public static final String MESSAGE_TEST_START    = "isHashメソッドのテストを行います";
    // テスト結果の出力だとわかるようにする為の水平線です
    public static final String OUTPUT_SEPARATOR      = "-------------------------------";
    // 判定結果を出力するためのフォーマットです
    public static final String FORMAT_IS_HASH_RESULT = "%s の判定結果は %b です\n";
    /**********************************************
     * isHash用テストデータを定義します
     *********************************************/
    // 数値型のテストデータです
    public static final String INT_STRING            = "1";
    // 配列型のテストデータです
    public static final String ARRAY_STRING          = "[\"value\"]";
    // 辞書型のテストデータです
    public static final String HASH_STRING           = "{\"key\":\"value\"}";
    // 配列のような辞書型のテストデータです
    public static final String LIKE_ARRAY_STRING     = "{\"0\":\"value\"}";

    /**
     * JSONDBManagerのisHashメソッドの動作確認を行います
     * JSONデータが { key : value } 形式の場合のみ true を返すか確認します
     * @throws ParseException
     */
    public static void main(String arg[]) throws ParseException {
        // 動作確認用に数値型のテストデータを受け取る変数です
        Object intStringObject;
        // 動作確認用に配列型のテストデータを受け取る変数です
        Object arrayStringObject;
        // 動作確認用に辞書型のテストデータを受け取る変数です
        Object hashStringObject;
        // 動作確認用に配列のような辞書型のテストデータを受け取る変数です
        Object likeArrayStringObject;
        /**********************************************
         * 動作テストの準備を行います
         *********************************************/
        // テストデータをisHashの引数の型に合わせるオブジェクトを作成します
        JSONParser jsonParser = new JSONParser();
        // isHashメソッドをテストするためにJSONDBManagerインスタンスを作成します
        JSONDBManager jsonDbManager = new JSONDBManager();
        // 動作確認用に数値型のテストデータを受け取ります
        intStringObject = jsonParser.parse(INT_STRING);
        // 動作確認用に配列型のテストデータを受け取ります
        arrayStringObject = jsonParser.parse(ARRAY_STRING);
        // 動作確認用に辞書型のテストデータを受け取ります
        hashStringObject = jsonParser.parse(HASH_STRING);
        // 動作確認用に配列のような辞書型のテストデータを受け取ります
        likeArrayStringObject = jsonParser.parse(LIKE_ARRAY_STRING);
        /**********************************************
         * 動作テストを行います
         *********************************************/
        // 動作テスト開始のメッセージを出力します
        System.out.println(MESSAGE_TEST_START);
        // テスト結果の出力だとわかるように水平線で出力を区切ります
        System.out.println(OUTPUT_SEPARATOR);
        // 数値型のデータをテストします。期待値はfalseです
        System.out.printf(FORMAT_IS_HASH_RESULT, intStringObject, jsonDbManager.isHash(intStringObject));
        // 配列型のデータをテストします。期待値はfalseです
        System.out.printf(FORMAT_IS_HASH_RESULT, arrayStringObject, jsonDbManager.isHash(arrayStringObject));
        // 辞書型のデータをテストします。期待値はtrueです
        System.out.printf(FORMAT_IS_HASH_RESULT, hashStringObject, jsonDbManager.isHash(hashStringObject));
        // 辞書型のデータをテストします。期待値はfalseです
        System.out.printf(FORMAT_IS_HASH_RESULT, likeArrayStringObject, jsonDbManager.isHash(likeArrayStringObject));
    }
}
