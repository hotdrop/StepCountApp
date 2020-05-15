## Step Counter App
Androidの歩行センサーがどの程度の精度か検証するために作成したアプリです。   
ついでにGoogleFitAPIだとどうなるかも確認したかったので機能として実装しました。    
検証用なのでだいぶ適当に作っています。  

## 歩行センサー
端末の`TYPE_STEP_COUNTER`を使って歩数と精度を取得します。    
日々の歩数が取得したかったのですが、このセンサーで取得できるのは「端末再起動後からのトータル歩数」なので計算に必要な値をSharedPreferencesやRoom経由でLocalDBに保存しています。  
（GoogleFitAPIで歩行センサーも扱えるのですが検証アプリなので自力でやっています。）  


## GoogleFit
GoogleFitAPIでも歩数を取得できるとのことで、自力で歩行センサーを使った場合とどの程度差が出るかを調査するために使いました。      
こちらは便利で日毎の歩数が取得できるのでそのまま表示しています。  
この機能を使うにはGCPのプロジェクトを作って認証情報を設定する必要があります。    
動作検証したい場合は自前でプロジェクトを作って認証情報を登録してください。