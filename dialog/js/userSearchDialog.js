/* ファイル名:userSearchDialog.js
 * 概要　　　:管理者 会員一覧 ユーザ検索ダイアログのJSファイル
 * 作成者　:T.Masuda
 * 場所　　:dialog/js/userSearchDialog.js
 */

/* クラス名:userSearchDialog.js
 * 概要　　:管理者 会員一覧 ユーザ検索ダイアログ
 * 親クラス:baseDialog
 * 引数	 :Element dialog:ダイアログのDOM
 * 作成者　:T.Masuda
 * 場所　　:dialog/js/userSearchDialog.js
 */
function userSearchDialog(dialog){
	baseDialog.call(this, dialog);	//親クラスのコンストラクタをコールする

	/* 関数名:getJson
	 * 概要　:JSONを取得する(オーバーライドして内容を定義してください)
	 * 引数　:なし
	 * 返却値:なし
	 * 設計者　:H.Kaneko
	 * 作成日　:2015.0815
	 * 作成者　:T.Masuda
	 */
	this.getJson = function(){
		//会員にユーザ検索ダイアログのjsonを取得する
		this[VAR_CREATE_TAG].getJsonFile(USER_SEARCH_DIALOG_JSON);
	};

	/* 関数名:getDom
	 * 概要　:createTag用テンプレートHTMLを取得する(オーバーライドして内容を定義してください)
	 * 引数　:なし
	 * 返却値:なし
	 * 設計者　:H.Kaneko
	 * 作成日　:2015.0822
	 * 作成者　:T.Masuda
	 */
	this.getDom = function(){
		
		//会員にユーザ検索ダイアログのjsonを取得するのテンプレートを取得する
		this[VAR_CREATE_TAG].getDomFile(USER_SEARCH_DIALOG_HTML);
		
	};
	
	/* 関数名:dispContentsMain
	 * 概要　:画面パーツ設定用関数のメイン部分作成担当関数
	 * 引数　:なし
	 * 返却値:なし
	 * 設計者　:H.Kaneko
	 * 作成日　:2015.0814
	 * 作成者　:T.Masuda
	 */
	this.dispContentsMain = function(){
		//検索フォームを作る
		this[VAR_CREATE_TAG].outputTag(KEY_USER_SEARCH_FORM, KEY_USER_SEARCH_FORM, CURRENT_DIALOG);
		//検索ボタンをセットする
		commonFuncs.putCommonButton(CURRENT_DIALOG, 'searchUserButton fRight', 'search', true, false, true);
	}
	
	/* 関数名:setConfig
	 * 概要　:ダイアログの設定を行う。任意でオーバーライドして定義する
	 * 引数　:なし
	 * 返却値:なし
	 * 設計者　:H.Kaneko
	 * 作成者　:T.Masuda
	 * 作成日　:2015.0822
	 */
	this.setConfig = function(){
		//ユーザ一覧タブ内の検索フォームの値をダイアログにコピーする
		this.copyTextboxValues('#adminTab .searchUserList');
		//ボタンをjQueryUIのものにして見栄えを良くする
		$(SELECTOR_INPUT_BUTTON, this.dialog).button();
		//会員一覧の検索の中にあるテキストボックスにフォーカスしているときにエンターキー押下で検索ボタンを自動でクリックする
		commonFuncs.enterKeyButtonClick('.adminUserSearch', '.searchUserButton:last');
		//ダイアログの位置を修正する
		this.setDialogPosition(POSITION_CENTER_TOP);
	}

	/* 関数名:setCallback
	 * 概要　:ダイアログのイベントのコールバック関数を設定する
	 * 引数　:なし(オーバーライド時に定義する)
	 * 返却値:なし
	 * 作成日　:2015.0822
	 * 作成者　:T.Masuda
	 */
	this.setCallback = function(){
		//dialogExクラスインスタンスがあれば
		if(commonFuncs.checkEmpty(this[DIALOG_CLASS])){
			//デフォルトのコールバック関数をセットする
			this[DIALOG_CLASS].setCallbackCloseOnAfterOpen(this.callbackClose);
			//ユーザ一覧検索関数をボタンにセットする
			this.addClickSearchButtonEventCallback('.searchUserButton:last', '#userList', '#adminTab .searchUserList')
		}
	}

	/* 関数名:addClickSearchButtonEventCallback
	 * 概要　:ダイアログのイベントのコールバック関数を設定する。
	 *      :内容としては、テキストボックスの内容を元にユーザ一覧の検索を行い、検索に使ったテキストボックスを元
	 *      :ページの検索フォーム領域にコピーし、その後にダイアログを閉じるというものになる。
	 *      :コピーしたテキストボックスはenterキー押下で検索を実行する。
	 * 返却値  :String target : イベントコールバック登録を行う要素。基本的にボタン
	 *        :String targetPage : 対象となる画面のセレクタ。createTagの参照を持っている要素である必要あり
	 *        :String copyTarget : テキストボックスをコピーする先
	 * 返却値:なし
	 * 作成日　:2016.0410
	 * 作成者　:T.Masuda
	 */
	this.addClickSearchButtonEventCallback = function(target, targetPage, copyTarget){
		//クリックイベントコールバック内で当クラスインスタンスを使うため変数に格納しておく
		var thisElem = this;
		//検索ボタンをクリックしたときにテーブルの内容を更新する
		$(target).on(CLICK, function() {
			//ユーザ検索を行う
			userListSearch(targetPage);
			
			//コピー先領域を取得する
			$copyTarget = $(copyTarget);
			//テキストボックスのコピー先の既存のテキストボックスを一掃する
			$copyTarget.children(SELECTOR_LABEL_TAG).remove();
			
			//入力済みのテキストボックスをユーザ一覧タブ内の検索フォームにコピーする
			$(SELECTOR_INPUT, thisElem.dialog).filter(SELECTOR_NOT_BUTTON_TYPE).each(function(i){
				//テキストボックスに値が入っていたら
				if (commonFuncs.checkEmpty($(this).val())){
					//指定した先にテキストボックスとラベルをコピーする
					$copyTarget.append($(this).parent());
				}
			});

			//検索ボタン押下と共にダイアログを破棄する
			thisElem.dialogClass.destroy();
		});
	}	
	
	/* 関数名:copyTextboxValues
	 * 概要　:name属性が一致したテキストボックスの内容を一括コピーする
	 * 返却値  :String copyFrom : コピー元のテキストボックスのフォームのセレクタ
	 * 返却値:なし
	 * 作成日　:2016.0410
	 * 作成者　:T.Masuda
	 */
	this.copyTextboxValues = function(copyFrom){
		//each関数内で当該クラスインスタンスを利用するため変数に入れておく
		var thisElem = this;	
		//コピー元となる対象を走査する
		$(SELECTOR_INPUT, $(copyFrom)).each(function(i){
			//走査対象の要素からname属性を取得する
			var name = $(this).attr(STR_NAME);
			//当該ダイアログ内の検索フォーム内の同一name属性の要素を取得する
			var $targetElem = $(SELECTOR_INPUT, this.dialog).filter(ATTR_EQUAL_NAME_FRONT + name + ATTR_EQUAL_NAME_REAR);
			console.log($(STR_NAME, this.dialog));
			console.log($targetElem);
			//対象を取得できている場合
			if ($targetElem.length){
				//値をコピーする
				$targetElem.attr(VALUE, $targetElem.val());
			}
		});
	}	
	
	/* 関数名:sendQuery
	 * 概要　:DBヘテーブル操作のクエリを投げる
	 * 引数　:String sendUrl:DBへアクセスするアプリケーションのパス
	 * 		:Object sendObj:URLへ送信するデータのオブジェクト
	 * 返却値:int:処理したレコード数を返す
	 * 設計者　:H.Kaneko
	 * 作成日　:2015.0822
	 * 作成者　:T.Masuda
	 */
	this.sendQuery = function(sendUrl, sendObj){
	};
	
//ここまでクラスの記述
}

//継承の記述
userSearchDialog.prototype = new baseDialog();
//サブクラスのコンストラクタを有効にする
userSearchDialog.prototype.constructor = baseDialog;
