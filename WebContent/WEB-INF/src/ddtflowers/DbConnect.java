package ddtflowers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * クラス名:dbConnect
 * 概要  :DBに接続する関数を持つクラス。
 * 設計者:H.Kaneko
 * @author S.Nihsiwaki
 * @since 20171205
 */
public class DbConnect {
    //////////////////////////////////////
    // constants
    //////////////////////////////////////
    // データベースのホスト設定です。
    private static final String DB_HOST = "localhost";
    // 使用するデータベースの設定です。
    private static final String DB_NAME = "ddthink-com00006";
    // データベースのユーザー設定です。
    private static final String DB_USER = "root";
    // データベースのパスワード設定です。
    private static final String DB_PASSWORD = "";
    // データベースの文字コード設定です。
    private static final String DB_CHARSET = "UTF-8";
    // 使用するデータベースの種類です。
    private static final String DB_TYPE = "mysql";
    // データベース接続に使用するドライバー名です。
    private static final String DB_DRIVER = "jdbc";
    // JDBCドライバーで接続するためのURL設定です。
    private static final String DB_URL = DB_DRIVER + ":" + DB_TYPE + "://" + DB_HOST + "/" + DB_NAME;
    //////////////////////////////////////
    // member
    //////////////////////////////////////
    // コネクションを保持するためのメンバーです。
    private Connection m_dbConnection = null;
    //////////////////////////////////////
    // getter
    //////////////////////////////////////
    /**
     * 関数名：getConnect
     * 概要  :DBとの接続を返す
     * 引数  :なし
     * @author S.Nihsiwaki
     * @since 20171205
     * @return Connection
     */
    public Connection getConnect () {
        return m_dbConnection;
    }
    //////////////////////////////////////
    // method
    //////////////////////////////////////
    /**
     * 関数名：connect
     * 概要  :DBとの接続をメンバーに保持する
     * 引数  :なし
     * 戻り値:なし
     * 設計者:H.Kaneko
     * @author S.Nishiwaki
     * @since 20171205
     * @throws ClassNotFoundException
     * @throws SQLException
     * @return void
     */
    public void connect() throws Exception{
        // DB接続時の例外を監視する
        try{
            // JDBCドライバで扱うDBを設定する(今回はmysql)
            Class.forName("com.mysql.jdbc.Driver");
            // DBとの接続を取得する
            m_dbConnection =  DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        // SQLException 発生時の処理を行う
        } catch (SQLException exception) {
            // エラーメッセージを出力する
            System.out.println("Connection failed. : " + exception.toString() );
        // ClassNotFoundException 発生時の処理を行う
        } catch (ClassNotFoundException exception){
            // エラーメッセージを出力する
            System.out.println("Connection failed. : " + exception.toString() );
        }
    }
    /**
     * DB接続を切断する
     * 関数名：disconnect
     * 概要  :メンバに保持した接続を破棄する
     * 設計者:H.Kaneko
     * @author S.Nishiwaki
     * @since 20171205
     * @throws SQLException
     * @return void
     * @param
     * @return void
     */
    public void disconnect() {
        // 切断処理時の例外を監視する
        try {
            // コネクションがガベージコレクトされていない場合の処理を行う
            if( this.m_dbConnection != null ) {
                // DBとのコネクションを切断する
                m_dbConnection.close();
            }
        // 切断処理時に例外が発生した場合の処理を行う
        } catch ( SQLException exception ) {
            // エラーメッセージを出力する
            System.out.println("Connection failed. : " + exception.toString() );
        }
    }
}
