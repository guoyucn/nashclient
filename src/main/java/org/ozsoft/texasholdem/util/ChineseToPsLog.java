/**
 * 
 */
package org.ozsoft.texasholdem.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.math.BigDecimal;

/**
 * @author sheng
 *
 */
public class ChineseToPsLog {
	//private static Pattern pHandBB = Pattern.compile("PokerStars Hand #(\\d+):.*Level.*\\(\\d+/(\\d+)\\).*");
	//private static Pattern pBtnSeat = Pattern.compile(".*Seat #(\\d+) is the button");
	//private static Pattern pSeatPlayer = Pattern.compile("Seat (\\d+): (\\S+) \\((\\d+) in chips\\)");
	//private static Pattern pHoleCards = Pattern.compile("Dealt to \\S+ \\[(.+)\\]");
	//private static Pattern pRaise = Pattern.compile("(\\S+): raises (\\d+) to (\\d+)");
	//private static Pattern pCall = Pattern.compile("(\\S+): calls (\\d+).*");
	//private static Pattern pFlop = Pattern.compile("\\*\\*\\* FLOP \\*\\*\\* \\[(.+)\\]");
	//private static Pattern pCheck = Pattern.compile("(\\S+): checks");
	//private static Pattern pBet = Pattern.compile("(\\S+): bets (\\d+)");
	//private static Pattern pTurn = Pattern.compile("\\*\\*\\* TURN \\*\\*\\*.+\\[(\\S{2})\\]");
	//private static Pattern pRiver = Pattern.compile("\\*\\*\\* RIVER \\*\\*\\*.+\\[(\\S{2})\\]");
	//private static Pattern pFold = Pattern.compile("(\\S+): folds");
	private static Pattern pAllIn = Pattern.compile("(\\S+): (bets|raises).+and is all-in");
	
	private static String psMoney = "((?<yi>\\d+)亿)?((?<wan>\\d+)万)?(?<one>\\d*)";
	private static String psMoney2 = "((?<yi2>\\d+)亿)?((?<wan2>\\d+)万)?(?<one2>\\d*)";
	private static String psMoney3 = "((?<yi3>\\d+)亿)?((?<wan3>\\d+)万)?(?<one3>\\d*)";
	private static Pattern pHandBB = Pattern.compile("联众德州扑克牌局 (?<table>\\S+) 盘数编号：(?<hand>\\d+) 小盲/大盲/前注:"
			+ psMoney + "/" + psMoney2 + "/" + psMoney3 + " (?<year>\\d+)年(?<month>\\d+)月(?<day>\\d+)日 (?<time>\\S+)");
	private static Pattern pBtnSeat = Pattern.compile("第(?<seat>\\d+)号座位现在是庄家");
	private static Pattern pSeatPlayer = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+)\\(" + psMoney + "筹码\\)");
	private static Pattern pSmallBlind = Pattern.compile("玩家(?<name>\\S+)：下小盲" + psMoney + ".*");
	private static Pattern pBigBlind = Pattern.compile("玩家(?<name>\\S+)：下大盲" + psMoney + ".*");
	private static Pattern pHoleCards = Pattern.compile("发牌给 玩家(?<name>\\S+)【(?<hole>.+)】");
	private static Pattern pBet = Pattern.compile("玩家(?<name>\\S+)：下注" + psMoney + "(?<allin> 全下)?");
	//private static Pattern pRaise = Pattern.compile("玩家(?<name>\\S+)：加注至((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)");
	private static Pattern pRaise = Pattern.compile("玩家(?<name>\\S+)：加注至" + psMoney + "(?<allin> 全下)?");
	//private static Pattern pCall = Pattern.compile("玩家(\\S+)：跟注至(\\d+)万(\\d*)");
	private static Pattern pCall = Pattern.compile("玩家(?<name>\\S+)：跟注至" + psMoney + "(?<allin> 全下)?");
	private static Pattern pCheck = Pattern.compile("玩家(?<name>\\S+)：让牌");
	private static Pattern pFold = Pattern.compile("玩家(?<name>\\S+)：弃牌");
	private static Pattern pFlop = Pattern.compile("\\*\\*\\*\\*\\*翻牌\\*\\*\\*\\*\\*【(?<flop>.+)】.*");
	private static Pattern pTurn = Pattern.compile("\\*\\*\\*\\*\\*转牌\\*\\*\\*\\*\\*【.+】【(?<turn>\\S+)】.*");
	private static Pattern pRiver = Pattern.compile("\\*\\*\\*\\*\\*河牌\\*\\*\\*\\*\\*【.+】【.+】【(?<river>\\S+)】.*");
	private static Pattern pUncalledBet = Pattern.compile("无人跟注 退还" + psMoney + "筹码给玩家(?<name>\\S+)");
	private static Pattern pWinFromPot = Pattern.compile("(?<name>\\S+)从主池赢得" + psMoney + ".*");
	private static Pattern pSummary = Pattern.compile("\\*\\*\\*\\*\\*摘要\\*\\*\\*\\*\\*");
	private static Pattern pTotalPot = Pattern.compile("底池：" + psMoney + " 主池：" + psMoney2 + ".*");
	private static Pattern pBoard = Pattern.compile("发出公共牌【(?<board>.+)】");
	private static Pattern pLoser = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*败北.*");
	private static Pattern pLoserBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*败北.*");
	private static Pattern pWinner = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*胜出 \\(" + psMoney + "\\).*");
	private static Pattern pWinnerBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*胜出 \\(" + psMoney + "\\).*");
	
	private static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
	
	static {
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	    defaultFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
	    defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	
	public static String process(List<String> psLogs, boolean debug) throws Exception{
		StringBuilder output = new StringBuilder();
		Matcher matcher;
		String tableNo = "";
		String handNo = "";
		String sb = "";
		String bb = "";
		String ante = "";
		String year = "";
		String month = "";
		String day = "";
		String time = "";
		String btnSeat = "";
		String bet = "";
		String flop = "";
		String turn = "";
		String river = "";
		BigDecimal totalPot = null;
		BigDecimal pot = null;
		//BigDecimal uncalledBet = null;
		String[] names = new String[10];
		
		for(String psLog : psLogs){
			if (psLog == null)
				continue;
			psLog = psLog.trim();
			if(psLog.isEmpty())
				continue;
			
			try {
				matcher = pHandBB.matcher(psLog);
				if (matcher.matches()){
					tableNo = Chn2PinYin(matcher.group("table"));
					handNo = matcher.group("hand");
					sb = getMoneyString(matcher);
					bb = getMoneyString2(matcher);
					ante = getMoneyString3(matcher);
					year = matcher.group("year");
					month = matcher.group("month");
					day = matcher.group("day");
					time = matcher.group("time");
	
					output.append("\r\n\r\n\r\n").append("PokerStars Hand #")
					.append(handNo).append(":  Hold'em No Limit ($").append(sb).append("/$")
					.append(bb).append(" USD) - ").append(year).append("/").append(month)
					.append("/").append(day).append(" " + time + " ET\r\n");
					
					//This is a new start, reset variables
					btnSeat = "";
					bet = "";
					flop = "";
					turn = "";
					river = "";
					totalPot = null;
					pot = null;
					//uncalledBet = null;
					for(int i = 0; i < 10; i++){
						names[i] = null;
					}
					
					continue;
				}
	
				matcher = pBtnSeat.matcher(psLog);
				if (matcher.matches()){
					btnSeat = matcher.group("seat");
					output.append("Table '" + tableNo + "' 9-max Seat #" + btnSeat + " is the button\r\n");
					continue;
				}
				
				matcher = pSeatPlayer.matcher(psLog);
				if (matcher.matches()){
					int seatNo = Integer.parseInt(matcher.group("seat"));
					String name = Chn2PinYin(matcher.group("name"));
					names[seatNo] = name;
					output.append("Seat ").append(seatNo).append(": " + name + " ($" + getMoneyString(matcher)
					+ " in chips)\r\n"); 
					continue;
				}
				
				matcher = pSmallBlind.matcher(psLog);
				if (matcher.matches()){
					output.append(Chn2PinYin(matcher.group("name"))).append(": posts small blind $")
					.append(getMoneyString(matcher)).append("\r\n"); 
					continue;
				}
	
				matcher = pBigBlind.matcher(psLog);
				if (matcher.matches()){
					bet = getMoneyString(matcher);
					output.append(Chn2PinYin(matcher.group("name")) + ": posts big blind $" + bet + "\r\n");
					for (int i = 0; i < 10; i++){
						if (names[i] != null){
							output.append(names[i] + ": posts the ante $" + ante + "\r\n");
						}
					}
					output.append("*** HOLE CARDS ***\r\n");
					continue;
				}
	
				matcher = pHoleCards.matcher(psLog);
				if (matcher.matches()){
					String holeCards = matcher.group("hole").replace("10", "T");
					output.append("Dealt to " + Chn2PinYin(matcher.group("name")) + " [" + holeCards + "]\r\n"); 
					continue;
				}
				
				matcher = pBet.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					bet = getMoneyString(matcher);
	
					String allin = matcher.group("allin");
					if (allin == null || allin.length() == 0){
						output.append(name + ": bets $" + bet + "\r\n");
					} else {
						output.append(name + ": bets $" + bet + " and is all-in\r\n");
					}
					continue;
				}
				
				matcher = pRaise.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					String newBet = getMoneyString(matcher);
					BigDecimal dNewBet = new BigDecimal(newBet);
					BigDecimal dBet = new BigDecimal(bet);
					String increment = dNewBet.subtract(dBet).toString();
					bet = newBet;
					
					String allin = matcher.group("allin");
					if (allin == null || allin.length() == 0){
						output.append(name + ": raises " + increment + " to "
							+ bet + "\r\n");
					} else {
						output.append(name + ": raises " + increment + " to "
							+ bet + " and is all-in\r\n");
					}
					continue;
				}
				
				matcher = pCall.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					bet = getMoneyString(matcher);
	
					String allin = matcher.group("allin");
					if (allin == null || allin.length() == 0){
						output.append(name + ": calls $" + bet + "\r\n");
					} else {
						output.append(name + ": calls $" + bet + " and is all-in\r\n");
					}
					continue;
				}
				
				matcher = pCheck.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					output.append(name + ": checks\r\n");
					continue;
				}
				
				/*
				matcher = pCheck.matcher(psLog);
				if (matcher.matches()){
					output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.k" : "V.k");
					continue;
				}
				
				matcher = pBet.matcher(psLog);
				if (matcher.matches()){
					output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.r" : "V.r")
						.append(matcher.group(2));
					continue;
				}
				*/
				
				matcher = pFold.matcher(psLog);
				if (matcher.matches()){
					output.append(Chn2PinYin(matcher.group("name")) + ": folds\r\n");
					continue;
				}
				
				matcher = pFlop.matcher(psLog);
				if (matcher.matches()){
					flop = matcher.group("flop").replace("10", "T");
					output.append("*** FLOP *** [" + flop + "]\r\n");
					continue;
				}
				
				matcher = pTurn.matcher(psLog);
				if (matcher.matches()){
					turn = matcher.group("turn").replace("10", "T");
					output.append("*** TURN *** [" + flop + "] [" + turn + "]\r\n");
					continue;
				}
				
				matcher = pRiver.matcher(psLog);
				if (matcher.matches()){
					river = matcher.group("river").replace("10", "T");
					output.append("*** RIVER *** [" + flop + " " + turn + "] [" + river + "]\r\n");
					continue;
				}
				
				matcher = pUncalledBet.matcher(psLog);
				if (matcher.matches()){
					String money = getMoneyString(matcher);
					String name = Chn2PinYin(matcher.group("name"));
					output.append("Uncalled bet ($" + money + ") returned to " + name + "\r\n");
					continue;
				}
				
				matcher = pWinFromPot.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					String money = getMoneyString(matcher);
					
					//We have to use a placeholder for uncalledBet
					//output.append("Uncalled bet (${uncalledBet}) returned to " + name + "\r\n");
					output.append(name + " collected $" + money + " from pot\r\n");
					continue;
				}
				
				matcher = pSummary.matcher(psLog);
				if (matcher.matches()){
					output.append("*** SUMMARY ***\r\n");
					continue;
				}
	
				matcher = pTotalPot.matcher(psLog);
				if (matcher.matches()){
					String money = getMoneyString(matcher);
					totalPot = new BigDecimal(money);
					
					money = getMoneyString2(matcher);
					pot = new BigDecimal(money);
					/*
					uncalledBet = totalPot.subtract(pot);
					if (uncalledBet.intValue() == 0){
						int start = output.indexOf("Uncalled bet (${uncalledBet}) returned to ");
						if (start != -1){
							int end = output.indexOf("\r\n", start) + "\r\n".length();
							output.replace(start, end, "");
						}
					} else {
						int start = output.indexOf("{uncalledBet}");
						if (start != -1){
							int end = start + "{uncalledBet}".length();
							output.replace(start, end, uncalledBet.toPlainString());
						}
					}
					*/
					output.append("Total pot $" + money + " | Rake $"
							+ sb + "\r\n");
					continue;
				}
	
				matcher = pBoard.matcher(psLog);
				if (matcher.matches()){
					String board = matcher.group("board").replace("10", "T");
					output.append("Board [" + board + "]\r\n");
					continue;
				}
	
				matcher = pLoser.matcher(psLog);
				if (matcher.matches()){
					String seatNo = matcher.group("seat");
					String name = Chn2PinYin(matcher.group("name"));
					String role = Chn2Eng(matcher.group("role"));
					String cards = matcher.group("cards").replace("10", "T");
					output.append("Seat " + seatNo + ": " + name + role
							+ " showed [" + cards + "] and lost\r\n");
					continue;
				} else {
					matcher = pLoserBeforeFlop.matcher(psLog);
					if (matcher.matches()){
						String seatNo = matcher.group("seat");
						String name = Chn2PinYin(matcher.group("name"));
						String role = Chn2Eng(matcher.group("role"));
						output.append("Seat " + seatNo + ": " + name + role
								+ " folded before Flop\r\n");
						continue;
					}
				}
	
				matcher = pWinner.matcher(psLog);
				if (matcher.matches()){
					String seatNo = matcher.group("seat");
					String name = Chn2PinYin(matcher.group("name"));
					String role = Chn2Eng(matcher.group("role"));
					String cards = matcher.group("cards").replace("10", "T");
					String money = getMoneyString(matcher);
					output.append("Seat " + seatNo + ": " + name + role
							+ " showed [" + cards + "] and Won ($" + money + ")\r\n");
					continue;
				} else {
					matcher = pWinnerBeforeFlop.matcher(psLog);
					if (matcher.matches()){
						String seatNo = matcher.group("seat");
						String name = Chn2PinYin(matcher.group("name"));
						String role = Chn2Eng(matcher.group("role"));
						String money = getMoneyString(matcher);
						output.append("Seat " + seatNo + ": " + name + role
								+ " collected ($" + money + ")\r\n");
						continue;
					}
				}
	
				/*
				matcher = pAllIn.matcher(psLog);
				if (matcher.matches()){
					output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.s" : "V.s");
					continue;
				}
				*/
			} catch (Exception e) {
				output.append("---!!! ERROR !!!--->>" + psLog + "\r\n")
					.append(e.getMessage() + "\r\n");
			}
			
			if (debug)
				output.append("------Unhandled------>>" + psLog + "\r\n");
		}
		return output.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File inputFile;
		boolean debug = false; 
		List<String> psLogs = new ArrayList<String>();
		
		if (!(args == null || args.length < 1 || args.length > 2)) {
			try {
				if (args.length == 1){
					inputFile = new File(args[0]);					
				} else {
					if (!"/d".equalsIgnoreCase(args[0])){
						throw new Exception("Wrong option - " + args[0]);
					}
					debug = true;
					inputFile = new File(args[1]);
				}
				
				try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"))) {
				    for(String line; (line = br.readLine()) != null; ) {
				    	psLogs.add(line);
				    }
				}
				
				String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
				String inputFileStr = inputFile.getAbsolutePath();
				String outputFileStr = String.format("%s_%s.txt",
						inputFileStr.substring(0, inputFileStr.lastIndexOf(".")), time);
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileStr), "utf-8"))) {
					writer.write(ChineseToPsLog.process(psLogs, debug));
				}

			} catch (Exception e) {
				logError(e);
				return;
			}
		}

	}

	private static void logError(Exception e) {
		System.out.println("*****************************************");
		System.out.println("Usage (normal): ChineseToPsLog InputFile");
		System.out.println("-OR-");
		System.out.println("(debug mode): ChineseToPsLog /d InputFile");
		System.out.println("*****************************************");
		e.printStackTrace();
	}
	
	private static String Chn2Eng(String chn){
		if (chn == null)
			return "";
		else if ("小盲注".equals(chn))
			return " (small blind)";
		else if ("大盲注".equals(chn))
			return " (big blind)";
		else if ("庄家".equals(chn))
			return " (button)";
		else
			return chn;
	}
	
	private static String Chn2PinYin(String src){
		StringBuilder pinyin = new StringBuilder();
	    try {
	        for (char element : src.toCharArray()) {
	            if (Character.toString(element).matches("[\\u4E00-\\u9FA5]+")) {
	                String[] srcArry = PinyinHelper.toHanyuPinyinStringArray(element, defaultFormat);
	                pinyin.append(srcArry[0]);
	            } else {
	                pinyin.append(Character.toString(element));
	            }
	        }

	    } catch (BadHanyuPinyinOutputFormatCombination e1) {
	        // e1.printStackTrace();
	    }

	    return pinyin.toString();
	}
	
	private static String removeTrailingZero(String sDecimal){
		return sDecimal.replaceAll("0*$", "");
	}
	
	private static String getMoneyString(Matcher matcher){
		int money = 0;
		String yi = matcher.group("yi");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one");
		if (sDecimal == null || sDecimal.isEmpty()){
			sDecimal = "";
		} else {
			sDecimal = "." + removeTrailingZero(sDecimal);
		}
		
		return Integer.toString(money) + sDecimal;
	}

	private static String getMoneyString2(Matcher matcher){
		int money = 0;
		String yi = matcher.group("yi2");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan2");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one2");
		if (sDecimal == null || sDecimal.isEmpty()){
			sDecimal = "";
		} else {
			sDecimal = "." + removeTrailingZero(sDecimal);
		}
		
		return Integer.toString(money) + sDecimal;
	}

	private static String getMoneyString3(Matcher matcher){
		int money = 0;
		String yi = matcher.group("yi3");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan3");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one3");
		if (sDecimal == null || sDecimal.isEmpty()){
			sDecimal = "";
		} else {
			sDecimal = "." + removeTrailingZero(sDecimal);
		}
		
		return Integer.toString(money) + sDecimal;
	}
}
