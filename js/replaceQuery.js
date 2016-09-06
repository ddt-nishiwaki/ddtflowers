// クエリのテンプレと置き換え対象文字列を渡して初期化する。
// replaceThenGetQueryに置き換え後の文字列を渡すとテンプレ中の置き換え対象文字列を置き換えてくれる。
var QuerySupplier = function(queryBase, targetStr) {
	this.replaceThenGetQuery = function(col_value){
		if(!col_value) {
			return "";
		}
		return queryBase.replace(new RegExp(targetStr,"g"), col_value);
	};
};

// 第一引数と第二引数を第三引数でつなげるだけconditionが空ならqueryをそのままリターン
function connectConditions(query, condition, conjunction) {
	if(condition) {
		return query + " " + conjunction + " " + condition;
	}
	return query;
}


// 管理者側、ユーザ一覧での検索クエリ生成
/**
 * @param ユーザ一覧テーブルのJSON(Object)
 */
var adminUserSearcher = function(userListInfoTable) {

	// クエリ固定部分
	// var baseQuery = "SELECT * FROM user_inf";
	//クエリをjsonから取り出したものにする
	var baseQuery = userListInfoTable.db_getQuery;
	var result = baseQuery;	// 結果

	// ユーザIDのクエリと置き換え対象
	var userKeyReplaceTarget = "{{user_key}}";
	var userKeyQuery = "id = " + userKeyReplaceTarget;

	// 氏名のクエリと置き換え対象
	var userNameReplaceTarget = "{{user_name}}";
	var userNameQuery = "user_name LIKE '%" + userNameReplaceTarget + "%'";

	// 氏名カナのクエリと置き換え対象
	var nameKanaReplaceTarget = "{{name_kana}}";
	var nameKanaQuery = "name_kana LIKE '%" + nameKanaReplaceTarget + "%'";

	// 電話番号のクエリと置き換え対象
	var telephoneReplaceTarget = "{{telephone}}";
	var telephoneQuery = "telephone LIKE '%" + telephoneReplaceTarget + "%'";

	// メールアドレスのクエリと置き換え対象
	var mailAddressReplaceTarget = "{{mail_address}}";
	var mailAddressQuery = "mail_address LIKE '%" + mailAddressReplaceTarget + "%'";

	// 期間FROMのクエリと置き換え対象
	var lessonDateFromReplaceTarget = "{{lesson_date_from}}";
	var lessonDateFromQuery = "lesson_date >= '" + lessonDateFromReplaceTarget + "'";

	// 期間TOのクエリと置き換え対象
	var lessonDateToReplaceTarget = "{{lesson_date_to}}";
	var lessonDateToQuery = "lesson_date <= '" + lessonDateToReplaceTarget + "'";

	// レッスンのクエリと置き換え対象
	var lessonReplaceTarget = "{{lesson_key}}";
	var lessonQuery = "id IN(SELECT user_key FROM user_lesson WHERE lesson_key = " + lessonReplaceTarget + ")";

	// 期間のクエリ用ワーク領域
	var workTurmQuery = "";
	
	// 期間のクエリ固定部分
	var turmQueryBase = 
		"id IN(" +
			"SELECT " +
				"user_key " +
			"FROM " +
				"user_classwork " +
			"INNER JOIN " +
				"classwork " +
			"ON " +
				"user_work_status = 3 " +
			"AND " +
				"classwork.id = user_classwork.classwork_key " +
			"INNER JOIN " +
				"time_table_day " +
			"ON " +
				"time_table_day.id = classwork.time_table_day_key ";
	
	// 期間Fromのクエリ取得関数
	var lessonDateFromQueryGetter = function(from) {
		// まずはjoinの中の部分クエリを取って
		var fromQuery = new QuerySupplier(
			lessonDateFromQuery, lessonDateFromReplaceTarget).replaceThenGetQuery(from);
		// 結果が空文字なら処理は中断(特にエラーじゃあない)
		if(!fromQuery) {
			return "";
		}
		// もう期間全体のクエリが出来てるなら普通につなげればいいのでそのまま返却
		if(workTurmQuery) {
			result = result.slice(0,-1);	// 「)」で閉じてるはずなんで削っておく
			return fromQuery + ")";
		}
		// まだ期間全体のクエリが無い場合は全体クエリを作ってから返却
		workTurmQuery = connectConditions(turmQueryBase, fromQuery, "AND");
		return workTurmQuery+ ")";
	}
	
	// 期間Toのクエリ取得関数
	var lessonDateToQueryGetter = function(to) {
		// まずはjoinの中の部分クエリを取って
		var toQuery = new QuerySupplier(
			lessonDateToQuery, lessonDateToReplaceTarget).replaceThenGetQuery(to);
		// 結果が空文字なら処理は中断(特にエラーじゃあない)
		if(!toQuery) {
			return "";
		}
		// もう期間全体のクエリが出来てるなら普通につなげればいいのでそのまま返却
		if(workTurmQuery) {
			result = result.slice(0,-1);	// 「)」で閉じてるはずなんで削っておく
			return toQuery + ")";
		}
		// まだ期間全体のクエリが無い場合は全体クエリを作ってから返却
		workTurmQuery = connectConditions(turmQueryBase, toQuery, "AND");
		return workTurmQuery + ")";
	}


	// クエリ取得用メソッドを検索対象名がキーの連想配列に格納していく(基本は単純にQuerySupplierで処理)
	var adminUserSearchConditions = {
			user_key : new QuerySupplier(userKeyQuery, userKeyReplaceTarget).replaceThenGetQuery
			,user_name : new QuerySupplier(userNameQuery, userNameReplaceTarget).replaceThenGetQuery
			,name_kana : new QuerySupplier(nameKanaQuery, nameKanaReplaceTarget).replaceThenGetQuery
			,telephone : new QuerySupplier(telephoneQuery, telephoneReplaceTarget).replaceThenGetQuery
			,mail_address : new QuerySupplier(mailAddressQuery, mailAddressReplaceTarget).replaceThenGetQuery
			,lesson_date_from : lessonDateFromQueryGetter
			,lesson_date_to : lessonDateToQueryGetter
			,lesson_key : new QuerySupplier(lessonQuery, lessonReplaceTarget).replaceThenGetQuery
	};

	// 条件をANDでつなげていく(最初だけWHERE)
	var connectEach = function(query, condition) {
		if(query == baseQuery) {
			return connectConditions(query, condition, baseQuery.indexOf("WHERE") != -1 ? "AND" : "WHERE");
		}
		return connectConditions(query, condition, "AND");
	};
	
	// 検索用テキスト全体をぶん回して「data-col_name」属性の値が
	// adminUserSearchConditionsに対応していればクエリ生成メソッドが呼ばれる。
	this.execute = function(form) {
		//親要素が指定してあったらセレクタに追記する
		var target = form ? form + " .adminUserSearch" : ".adminUserSearch";
		result = baseQuery;	// 呼ばれるたびに結果は初期化
		$(target).each(function(){
			var col_name = $(this).data("col_name");
			var value = $(this).val();
			if(col_name in adminUserSearchConditions) {
				var partQuery = adminUserSearchConditions[col_name](value);
				result = connectEach(result, partQuery);
			}
		});
		//変更者:T.Yamamoto 変更日:2015.08.04 内容:ifの中をコメントにしてなにも入力されていないときは全検索を行う
		// if(baseQuery == result) {
		// 	throw new Error("no condition was specified");
		// }
		return result;
	};	
}

