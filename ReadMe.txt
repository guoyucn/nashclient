1. 如何生成牌局数据文件。
	1) 运行Poker-Generate.bat文件，会直接生成10个牌局文件，每个文件100 game.
	或者
	java -cp poker-2.0.jar org.ozsoft.texasholdem.util.InputOutputMgr <numberOfFiles> <numberOfRows>
		<numberOfFiles> default set to 1,
		<numberOfRows> default set to 100.
	2) 两个子目录 Input 和 Output 会自动生成，所有生成的牌局数据文件都会放到子目录 Input 里面。 
	3） 牌局数据文件名：Input_<Index>_<TimeStamp>.csv。
2. 如何使用牌局数据文件。
	1） java org.ozsoft.texasholdem.gui.Main - 所有牌局随机生成。（缺省设置）
	2） 运行Poker-IO.bat文件，
	或者
	java -cp poker-2.0.jar org.ozsoft.texasholdem.gui.Main File - 所有牌局从子目录 Input 里的csv文件读取，结果自动记录到子目录 Output。
	3） 牌局结果记录文件名：<牌局数据文件名>_<TimeStamp>.txt。
3. 如何修改牌局。
	1） 所有牌局的初始设置都在牌局数据文件里。可以根据需要任意设置以下信息：
		DealerPosition, BigBlind, Money1, Money2, Cards
	2） 如果想让robot和human交换位置玩同一付牌局，只需将牌局数据文件中的DealerPosition值从0改成1，或1改成0即可。
4. 如何将PokerStar输出文件转化为桌面短格式。
	1） PokerStar输出文件格式：
	PokerStars Hand #136025860044: Tournament #1242190023, $1.44+$0.06 USD Hold'em No Limit - Match Round I, Level I (10/20) - 2015/05/31 0:05:30 CCT [2015/05/30 12:05:30 ET]
	Table '1242190023 1' 2-max Seat #2 is the button
	Seat 1: MrDan710 (540 in chips) 
	Seat 2: 1vcky188 (460 in chips) 
	1vcky188: posts small blind 10
	MrDan710: posts big blind 20
	*** HOLE CARDS ***
	Dealt to 1vcky188 [Js Ah]
	1vcky188: raises 20 to 40
	MrDan710: calls 20
	*** FLOP *** [Ac 5c 7s]
	MrDan710: bets 40
	1vcky188: calls 40
	*** TURN *** [Ac 5c 7s] [Jh]
	MrDan710: bets 100
	1vcky188: calls 100
	*** RIVER *** [Ac 5c 7s Jh] [4c]
	MrDan710: bets 360 and is all-in
	1vcky188: calls 280 and is all-in
	Uncalled bet (80) returned to MrDan710
	*** SHOW DOWN ***
	MrDan710: shows [9c 7c] (a flush, Ace high)
	1vcky188: shows [Js Ah] (two pair, Aces and Jacks)
	MrDan710 collected 920 from pot
	1vcky188 finished the tournament in 2nd place
	MrDan710 wins the tournament and receives $2.88 - congratulations!
	*** SUMMARY ***
	Total pot 920 | Rake 0 
	Board [Ac 5c 7s Jh 4c]
	Seat 1: MrDan710 (big blind) showed [9c 7c] and won (920) with a flush, Ace high
	Seat 2: 1vcky188 (button) (small blind) showed [Js Ah] and lost with two pair, Aces and Jacks
	
	2）桌面短格式：
	136025860044 20 540 460 S JsAh H.r20 V.c F.Ac5c7s V.r40 H.c T.Jh V.r100 H.c R.4c V.s H.c
	
	3）运行一下命令行将PokerStar输出文本文件转化为桌面短格式文本文件：
	java -cp poker-2.0.jar org.ozsoft.texasholdem.util.PsToShortLog H=HeroName pokerstarLog.txt
	输出文件名为原文件名+当前系统时间，例如，pokerstarLog_160308215916.txt
5. 如何将中文牌局记录文件转化为PokerStar文件格式。
	运行如下命令行将中文牌局记录文件转化为PokerStar文件格式：
	1）正常模式：
	java -cp poker-2.01.jar org.ozsoft.texasholdem.util.ChineseToPsLog InputFile rake.txt
	2)替换模式：
	java -cp poker-2.01.jar org.ozsoft.texasholdem.util.ChineseToPsLog InputFile rake.txt ReplaceFile
	3）正常Debug模式：
	java -cp poker-2.01.jar org.ozsoft.texasholdem.util.ChineseToPsLog /d InputFile rake.txt
	4）替换Debug模式：
	java -cp poker-2.01.jar org.ozsoft.texasholdem.util.ChineseToPsLog /d InputFile rake.txt ReplaceFile
	