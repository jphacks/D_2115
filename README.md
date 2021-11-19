# totte!（カメラマン探し × tech）

[![IMAGE ALT TEXT HERE](https://jphacks.com/wp-content/uploads/2021/07/JPHACKS2021_ogp.jpg)](https://youtu.be/7z7tzAmCYVw)

## 製品概要
### 背景(製品開発のきっかけ、課題等）

写真スポットで，遠くから自分を収めた映える写真を撮りたい！だけど，自撮り棒には限界があるし，道行く人に何度も撮り直しを頼むのは申し訳ない！
この課題を解決する，カメラマンを探している人同士をつなげるアプリを開発します．撮影者を探す人どうしの近距離無線通信をサポートし，コミュニケーションのきっかけを作り出します．


### 製品説明（具体的な製品の説明）

「totte!」は写真を撮影してもらいたい人同士をマッチングさせる，他撮り支援アプリです．  
撮影スポットで他の人に写真を撮ってもらいたいけど「何度も取り直しは申し訳ない」，「知らない人にスマホを渡すのは不安，渡されることも衛生的に...」というようなもやもやを解決します．  

#### 使い方

1. アプリを起動させたらまずはプロフィール画面を編集
    - 服装やどういう風に撮ってほしいかを書くとよい
1. 「Search」ボタンを押してマッチングを開始
    - 他に「Search」ボタンを押している人がいたらその人とマッチング
1. マッチングしたら相手のプロフィールを確認可能
    - 落ち合う際にはチャットを使用すると便利
1. 落ち合ったらお互いに写真を撮影
    ‐ 撮影順番，要望をチャットでやりとり
3. 双方納得したら「さようなら」ボタンを押して一連の流れを終了

### 特長

1. 同じ目的を持つ人を自動的に探し出します．お互い撮ってほしいのでいつでもwin-winに撮影できます．
2. 撮影は自分のスマホで行います．相手のスマホに触れる必要はないので感染対策もばっちりです．
3. 相手が撮影した写真は相手のスマホに保存されません．あなたのプライバシーを守ります．

### 解決出来ること

**写真スポットでよくあるもやもや**
- 遠くから自分を収めた映える写真を撮りたい！
    - 自撮り棒には限界があるし，道行く人に何度も撮り直しを頼むのは申し訳ない！
- 他人のスマホを触りたくない
- 写真交換の際にLINEとか交換したくない
- AirDropを使えない，使いたくない


### 今後の展望

- マッチング対象の拡張
    - 必ずしも同じ目的を持つ人でなくてもよい
    - 観光地に常駐するカメラマンや，無人の撮影スポットとのマッチング
- 接続の安定
    - Nearby APIの接続距離は30m
    - 可用性の向上のためには，別の接続方法が必要
- 高度な情報交換
    - 現在はチャットべ―スでしかやり取りができない
    - プロフィール画像や，お互いの位置情報を交換できると，会うことが容易になる

### 注力したこと（こだわり等）
* 画像の交換はNearbyAPIを使用．写真は自分のスマホ以外，どこにも残らない．
* チャットの機能の実装．マッチング後の落合や，撮影に関する相談，要望をスムーズにやりとり可能．
* プロフィールの登録．1日に何度も使用する場合，その日の服装などを登録すれば，何度も同じことを送る必要がない．

## 開発技術

### 使用言語

- Kotlin
  
### 活用した技術

#### API・データ

- Nearby Connections API
    - 30m以内で自動で接続してくれます
- Firebase
    - チャット機能に使用
![yellow (1)](https://user-images.githubusercontent.com/81406224/142598453-ea5949b5-7443-4256-b489-5528e04f33e6.png)

#### デバイス

- Android端末
 
