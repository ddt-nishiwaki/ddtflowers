<?php

// データベースに接続するための値を定数として宣言する
require_once 'EnvSettings.php';
EnvSettings::create()->loadEnvSettings('DbDefine');

/*
 * ファイル名:dbConnect.php
 * 概要	:DBに接続する関数を持つクラスのPHPファイル。
 * 設計者:H.Kaneko
 * 作成者:T.Masuda
 * 作成日:2015.0728
 * パス	:/php/dbConnect.php
 */

/*
 * クラス名:dbConnect
 * 概要  :DBに接続する関数を持つクラス。
 * 設計者:H.Kaneko
 * 作成者:T.Masuda
 * 作成日:2015.0728
 */
class dbConnect{
	
	// データベースハンドラ-(DB接続に使う)
	public $dbh = "";
		
	/*
	 * 関数名：connect
	 * 概要  :DBとの接続を行う。
	 * 引数  :なし
	 * 戻り値:なし
	 * 設計者:H.Kaneko
	 * 作成者:T.Masuda
	 * 作成日:2015.0728
	 */
	function connect(){
		// DBに接続する。
		$this->dbh = new PDO(DSN, DB_USER, DB_PASSWORD);
		// クエリをUTF8で設定する
		$this->dbh->query('SET NAMES utf8');
	}
		
	/*
	 * 関数名：disconnect
	 * 概要  :DBとの接続を閉じる。
	 * 引数  :なし
	 * 戻り値:なし
	 * 設計者:H.Kaneko
	 * 作成者:T.Masuda
	 * 作成日:2015.0728
	 */
	function disconnect(){
		//DBとの接続を閉じる
		$this->dbh = null;
	}
	
}