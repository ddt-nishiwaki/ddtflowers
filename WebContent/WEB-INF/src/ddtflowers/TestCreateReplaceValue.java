package ddtflowers;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JSONDBManagerのcheckColumnメソッドの動作確認を行うクラス
 */
public class TestCreateReplaceValue {
    /**********************************************
     * テスト出力用の文字列を定義します
     *********************************************/
    // isHashメソッドのテストを行うメッセージです
    public static final String MESSAGE_TEST_START    = "createReplaceValueメソッドのテストを行います";
    // テスト結果の出力だとわかるようにする為の水平線です
    public static final String OUTPUT_SEPARATOR      = "-------------------------------";
    // 置換結果の出力をする際のメッセージフォーマットです
    public static final String FORMAT_REPLACE_RESULT = "%s の置換結果を出力します\n\t↓\n\t%s\n\n";
    /**********************************************
     * isHash用テストデータを定義します
     *********************************************/
    // 数値型のテストデータです
    public static final String INT_STRING            = "1";
    // 配列型のテストデータです
    public static final String ARRAY_STRING          = "[\"value1\",\"value2\"]";
    // クエリーを壊すシングルクォートを含んだテストデータです
    public static final String ARRAY_COUTION_STRING  = "[\"'value1'\",\"'value2'\"]";

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
        // 動作確認用にクエリーを壊すシングルクォートを含んだテストデータを受け取る変数です
        Object arrayCoutionObject;
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
        // 動作確認用に配列型のテストデータを受け取ります
        arrayCoutionObject = jsonParser.parse(ARRAY_COUTION_STRING);
        /**********************************************
         * 動作テストを行います
         *********************************************/
        // 動作テスト開始のメッセージを出力します
        System.out.println(MESSAGE_TEST_START);
        // テスト結果の出力だとわかるように水平線で出力を区切ります
        System.out.println(OUTPUT_SEPARATOR);
        // 数値型のデータをテストします。
        System.out.printf(FORMAT_REPLACE_RESULT, intStringObject, jsonDbManager.createReplaceValue(intStringObject));
        // 配列型のデータをテストします。
        System.out.printf(FORMAT_REPLACE_RESULT, arrayStringObject,
                jsonDbManager.createReplaceValue(arrayStringObject));
        // クエリーを壊すシングルクォートを含んだテストデータをテストします。
        System.out.printf(FORMAT_REPLACE_RESULT, arrayCoutionObject,
                jsonDbManager.createReplaceValue(arrayCoutionObject));

    }
}