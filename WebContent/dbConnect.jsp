<%@page import="ddtflowers.DbConnect, java.sql.Connection, java.sql.ResultSet, java.sql.ResultSetMetaData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%
    /*
     * DbConnectクラスの動作テストを行います
     */
    // 動作チェックを行うことをログに出力します。
    System.out.println("\n\n--------------------\n DbConnect動作確認 \n--------------------");
    // クラスのインスタンス生成チェックを行います
    DbConnect dbConnect = new DbConnect();
    try {
        // DB接続の動作チェックを行います
        dbConnect.connect();
        // 取得したConnectionを確認の為ログに出力することをログに出力します
        System.out.println("取得したConnectionを確認の為ログに出力します");
        // 取得したConnectionを確認の為ログに出力します
        System.out.println("\t" + dbConnect.getConnect());
        // 外部からのクエリー発行動作を確認します
        ResultSet resultSet = dbConnect.sDbh.executeQuery("SELECT * FROM `user_inf` WHERE 1 LIMIT 0, 1");
        // 取得したカラムの数を確認する為のデータを取得します
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        // クエリ結果出力の為、取得したカラムの数を取得します
        int columnLength = resultSetMetaData.getColumnCount();
        // クエリ結果を確認の為ログに出力することをログに出力します
        System.out.println("クエリ結果を確認の為ログに出力します");
        // クエリ結果を確認の為ログに出力します
        while ( resultSet.next() ) {
            for(int columnCount = 1; columnCount < columnLength; columnCount++){
                System.out.println("\t" + resultSet.getString(columnCount));
            }
        }
        // DBとの切断動作を確認します
        dbConnect.disconnect();
    // 動作しない場合の処理を行う
    } catch (Exception exception) {
        // エラー内容を出力します
        exception.printStackTrace();
    }
%>




