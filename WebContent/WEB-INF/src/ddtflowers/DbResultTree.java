package ddtflowers;

import java.sql.ResultSet;

import org.json.simple.JSONObject;

/**
 * クラス名 :DB_ResultTree
 * 概要 :DBの結果セットのツリーのノードクラス
 * 設計者 :H.Kaneko
 * 作成者 :S.Nishiwaki
 * 作成日 :2018.01.10
 */
public class DbResultTree {
    // DBの結果セット
    public ResultSet    dbResultSet = null;
    // このノード(インスタンス)の親
    public DbResultTree parentTree  = null;
    // JSONデータの連想配列
    public JSONObject   jsonObject  = null;
    // メンバのjsonのキー
    public String       keyData     = "";
}
