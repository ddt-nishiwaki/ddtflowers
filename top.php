<!-- container -->
<div id="container" class="window" name="usuall">

<header class="header">
</header>
	<div class="main">
	</div>
<script>
		try{
			commonFuncs = new common();						//汎用共通関数定義クラスインスタンスを生成する

			var create_tag = new createLittleContents();		//createTagクラスのインスタンスを生成する
			
			create_tag.getJsonFile('source/index.json');		// ファイルのデータをjsonを用いて持ってくる
			create_tag.getJsonFile('source/commonJson.json');	// ファイルのデータをjsonを用いて持ってくる
			create_tag.getDomFile('template/common.html');		// 共通パーツのDOMを取得する。
			create_tag.getDomFile('template/toppage.html');	// トップページのパーツのDOMを取得する。
			//ヘッダー内のタグが作成済みでなければ
		
			create_tag.createNormalHeaderContent();	//ヘッダー内のタグが作成済みでなければ作る。
		
			create_tag.outputTag('flowerBackground', 'createImage');	// トップページ背景を作る

			//ログイン状態をチェックした上でDBにアクセスする
			create_tag.doGuestLoginProcedure(function() {
				create_tag.getJsonFile(URL_GET_JSON_ARRAY_PHP, create_tag.json.getTopicGallery, 'getTopicGallery');
				create_tag.getJsonFile(URL_GET_JSON_ARRAY_PHP, create_tag.json.getTopicBlog, 'getTopicBlog');
			});

			//ギャラリー、ブログの新着記事情報をセットする
			commonFuncs.setDataToNoticeContents(create_tag.json.topicGallery, create_tag.json.getTopicGallery.tableData, commonFuncs.sampleNoticeDataOrganizeKeyArray, commonFuncs.sampleNoticeDataOrganizeSettingObj);
			commonFuncs.setDataToNoticeContents(create_tag.json.topicBlog, create_tag.json.getTopicBlog.tableData, commonFuncs.sampleNoticeDataOrganizeKeyArray, commonFuncs.sampleNoticeDataOrganizeSettingObj);
			//各お知らせを表示するボタンを作る
			create_tag.outputTag('topicShowCampaign','topicShow', '.flowerBackground');
			create_tag.outputTag('topicShowGallery','topicShow', '.flowerBackground');
			create_tag.outputTag('topicShowBlog','topicShow', '.flowerBackground');
			//各お知らせを作る。
			create_tag.outputTag('topicGallery','topic', '.flowerBackground');
			create_tag.outputTag('topicCampaign','topic', '.flowerBackground');
			create_tag.outputTag('topicBlog','topic', '.flowerBackground');
			
			create_tag.outputTag('footer');							// フッターを作る
			
			// トップメニューにマウスオーバーのフィルターを配置する。
			functionFilter('.topMenu li');
		
			//0ミリ秒後にキャンペーンお知らせ表示ボタンをスライド表示する。
			commonFuncs.moveNoticeWindows();
			//お知らせウィンドウを開閉させるイベントを登録する
			commonFuncs.toggleNoticeWindows();


			commonFuncs.setShowSelectedBlogArticleEvent();			
			
			//例外処理
			}catch(e){
				//エラーログを出す
				console.log(e);
			}
		</script>
</div>
<!-- JavaScriptのコードを記述する -->
