# 1章

ただしく動く並行プログラムを書くことは超難しい。理由は単純でバグが発生しそうな箇所が多いから。

## 並行処理の歴史

古代のコンピュータにはオペレーティングシステム(以降、OSと呼ぶ)がなく、コンピュータは1本のプログラムを最初から最後まで実行し、  
そのプログラムが計算機のすべての資源に直接アクセスしていた。

当時、コンピュータは高価だったので一度に1本しか動かせないのは資源の無駄使いと思われていた。

OSが登場し、複数のプログラムを同時に動かせるようになった。個々のプログラムはプロセスと呼ばれ  
OSが一つ一つを隔離し、正しい管理のもとに、メモリやファイル、CPU使用時間、セキュリティ証明などの  
資源を各プロセスに与えるようになった。  

プロセス同士のコミュニケーションには、ソケット、シグナルハンドラ、共有メモリ、ファイルなど、  
大きな粒度の通信の仕組みが使われた。

複数のプログラムを同時に動かすOSの開発の背景には、次のような動機があった。

#### 資源の有効利用

入出力のような外部的な操作には待ち時間があるので、その間にほかのプログラムを動かしたほうが効率的である。

#### 公平性

複数のユーザーやプログラムが、資源を平等に使うべき。  
前のプログラムが終わるのを待つより、小さな粒度でコンピュータを共有できたほうが平等である。

#### 利便性

必要なタスクをまとめた1本のプログラムを書くより、1つのタスクだけを担当する  
シンプルなプログラムを複数書いて、それらを協調的に動かしたほうが簡単かつ良いことであるi

---

逐次的なプログラミングモデルは、わかりやすく自然で、次のように人間が仕事をするときと同じである。

ベットから出る → ガウンを着る → 一階に降りる → お茶を飲む

更に細かく捉えることもできる。

#### お茶を飲むという仕事

戸棚を開ける → 紅茶の種類を選ぶ → ポッドに茶葉を入れる → やかんに水が入っているか確認 →  
入っていなければ水を入れる → コンロに乗せて火を点ける → お湯が沸くまで待つ

最後の "お湯が沸くまで待つ" は一種の **非同期性** を表している。  
水が熱せられている間に何をやるか選べるからである。

じっと待つか、トースターでパンを焼く(これも非同期のタスク)、新聞を取りに行くなどなど。  
※やかんとトースターは製品が非同期で使われることが多いと知っているので、タスクが終わったら音がなるよう設計されている。

仕事の名人は、逐次性と非同期性のバランスのとり方が上手。プログラミングも同じ。

#### スレッドの誕生とその概念について

スレッドの誕生の同期は、プロセスを使う動機(資源の有効利用、公平性、利便性)と同じ。  

+ スレッドによって、一つのプロセスの中に複数の処理の流れが共存できる。  
+ スレッドはプロセスの資源(メモリ、ファイルハンドル)を共有するが、スタックとローカルの変数は各スレッドが独自に持つ。
+ スレッドを使うとマルチプロセッサのシステム上でハードウェアの並行処理機能を有効利用するためにプログラムを適切な単位に分割できる。
+ そして一つのプログラムの複数のスレッドが、複数のマルチプロセッサに対して同時並行的にスケジューリングされる。

スレッドは軽量プロセスとも呼ばれる。  
現代のOSはプロセスではなくスレッドをスケジューリングの単位として扱う。  
スレッド間の調停を明示的に行わない場合、スレッド達はお互い同時並行的に非同期的に実行される。  

###### 共有データへのアクセス

スレッドは自分たちのオーナーであるプロセスのメモリアドレス空間を共有するので、
一つのプロセス内の複数のスレッドが同じ変数にアクセスし、同じヒープの上にオブジェクトを作る。

このような共有データへのアクセスは、明示的に同期化して調停しないと、  
あるデータをあるスレッドが使っているときはほかのスレッドがその値を変えてしまったりして、  
とんでもない処理結果になることもある。

### スレッドのメリット

Java言語のメイン機能のひとつ。

#### スレッドの特徴

- 非同期の複雑なプログラムをメンテナンスしやすくシンプルで直線的なコードで表現できる。
- マルチプロセッサシステムの計算能力を有効活用できる。

#### スレッドは有効性

- GUIアプリケーションにおける応答性の向上
- バックエンドにおける資源の有効利用とスループット向上

### マルチプロセッサの有効利用

1スレッドしかないプログラムは1つのプロセッサ上で動くだけ。  
プロセッサが2つあるシステムではシングルスレッドのプログラムはもう一つのプロセッサを見捨てる。  
100プロセッサのシステムでは99のプロセッサを見捨てる。

複数のアクティブスレッドのあるプログラムは複数のプロセッサを有効的に利用し、スループットを向上させる。

シングルスレッドのプログラムは同期的なI/Oが終わるのを待っている間はプロセッサがアイドリングしてしまう。  
マルチスレッドなら別のスレッドがI/Oをやっている間、別スレッドがプロセッサを使って仕事をするのでブロッキングI/Oをやっている間も、  
アプリケーションは前にすすめる。

やかんのお湯が沸くのを待ちながら新聞を読むことに似ている。  
シングルスレッドの場合、お湯が湧いてからやっと新聞を読み始める。

### 設計の単純化

仕事の種類が一つしかないほうが、時間を有効的に使える。

+ 「これらの12のバグを直せ」という命令
   + 1種類のしごとを集中するだけ
   + 効率的にバグフィックスできる
+ 「バグを直して、システム管理者のために応募してきた人を面接して、開発チームの実績報告書を書いて、来週のプレゼン資料を作れ」
   + 優先順位、締切、納期などを意識する必要があり、余分なエネルギーを消費する
   
 ソフトウェアの場合も同じ。
 
 一つの仕事を逐次的に処理するプログラムの方が、複数の異なる種類の仕事を管理するプログラムより簡単に書ける。  
 エラーが少なく、試験も容易。
 
 #### 一つのスレッドに一つの種類のタスクを割り当てる
 
 一つ一つのタスクシンプルな逐次処理として表現でき、ドメインロジックをスケジューリングや操作の順序、  
 非同期I/O、資源待機といった周辺的・非ドメイン的なロジックから隔離できる。
 
 複雑で非同期なワークフローを複数のシンプルで同期的なワークフローに分解して、それぞれを別々のスレッドとして動かし、  
 お互いを一定の同期点だけで対話させるのです。

#### 非同期イベントの処理を単純化

ネットワークの彼方の複数のクライアントからソケット接続を受け付けるサーバアプリケーションは、  
一つ一つのクライアント接続にそれを担当するスレッドを割り当てて同期I/Oさせたほうが、楽に開発できる。

アプリケーションがデータを読もうとしたとき、データが何もなければ、その呼出はデータが得られるまでブロックする。  
シングルスレッドのアプリケーションでは一つのクライアントがブロックしたら、そのリクエストが立ち往生するだけでなく、  
その単一のスレッドがブロックしている間、全てのリクエストが立ち往生する。

### 応答性の良いインターフェイス



## 用語Tips

### フォンノイマン型コンピュータ

プロセスが自分専用のメモリ空間をもち、そこに命令とデータの両方を保持するようなコンピュータ。

### クロックレート

CPU の命令実行タイミングのことで、一秒間に何回の処理を実行できるのかを表す。  
一秒間に6回命令を実行できれば6Hz、3回なら3Hzとなる。クロック数が大きいほど処理速度も向上する。

ただし、コアの数やスレッド数、CPU設計の最適化によって性能が大きく向上します。
新型のCPUはクロック周波数の高い旧式CPUより、総合的な性能が高い場合もあり、単純にクロック周波数だけで性能を比較することはできません。

### ノンブロッキングI/O、非同期I/O

ノンブロッキングI/Oは処理がすぐできない時はエラーを返し、ブロック状態にさせない方式。  
一方、非同期I/Oは処理がすぐできない時は処理が完了するまでバックグラウンドで待機して、終了したタイミングで通知を返す方式(通知が来たら既に処理が終わっている)。

 ### FIFO
 
 + First in first out.
 + 先に入ってきたものは先に処理し、後に入ってきたものは後に処理する
 + 日本語の俗な慣用表現では「ところてん式」と呼ばれることもある
