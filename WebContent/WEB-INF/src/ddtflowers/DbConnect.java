package ddtflowers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * クラス名:dbConnect
 * 概要  :DBに接続する関数を持つクラス。
 * 設計者:H.Kaneko
 * 作成者:S.Nihsiwaki
 * 作成日:20171205
 */
public class DbConnect {
    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // データベースのホスト設定です。
    private static final String DB_HOST     = "localhost";
    // 使用するデータベースの設定です。
    private static final String DB_NAME     = "ddthink-com00006";
    // データベースのユーザー設定です。
    private static final String DB_USER     = "root";
    // データベースのパスワード設定です。
    private static final String DB_PASSWORD = "";
    // データベースの文字コード設定です。
    private static final String DB_CHARSET  = "UTF-8";
    // 使用するデータベースの種類です。
    private static final String DB_TYPE     = "mysql";
    // データベース接続に使用するドライバー名です。
    private static final String DB_DRIVER   = "jdbc";
    // JDBCドライバーで接続するためのURL設定です。
    private static final String DB_URL      = DB_DRIVER + ":" + DB_TYPE + "://" + DB_HOST + "/" + DB_NAME;
    //////////////////////////////////////
    // member
    //////////////////////////////////////
    // コネクションを保持するためのメンバーです。
    private Connection          mDbConnect  = null;
    //////////////////////////////////////
    // class variable
    //////////////////////////////////////
    // データベースハンドラを保持するためのクラス変数です。
    public Statement sDbh = null;
    //////////////////////////////////////
    // getter
    //////////////////////////////////////
    /**
     * 関数名:getConnect
     * 概要  :DBとの接続を返す
     * 引数  :なし
     * 作成者:S.Nihsiwaki
     * 作成日:20171205
     * 戻り値:Connection
     */
    public Connection getConnect() {
        return mDbConnect;
    }

    //////////////////////////////////////
    // method
    //////////////////////////////////////
    /**
     * 関数名:connect
     * 概要  :DBとの接続をメンバーに保持する
     * 引数  :なし
     * 戻り値:なし
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:20171205
     * 戻り値:void
     */
    public void connect() throws Exception {
        // JDBCドライバで扱うDBを設定する(今回はmysql)
        Class.forName("com.mysql.jdbc.Driver");
        // DBとの接続を取得する
        mDbConnect = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        // 文字コード設定を行うクエリ発行のため、ステートメントを取得する
        sDbh = mDbConnect.createStatement();
        // クエリの文字コード設定をUTF8に設定する
        sDbh.executeQuery("SET NAMES utf8");
    }

    /**
     * DB接続を切断する
     * 関数名:disconnect
     * 概要  :メンバに保持した接続を破棄する
     * 設計者:H.Kaneko
     * 作成者:S.Nishiwaki
     * 作成日:20171205
     * 例外:SQLException
     * 戻り値:void
     */
    public void disconnect() throws SQLException {
        // DBとのコネクションを切断する
        mDbConnect.close();
        // DBとのコネクション情報を破棄する
        mDbConnect = null;
    }
}
