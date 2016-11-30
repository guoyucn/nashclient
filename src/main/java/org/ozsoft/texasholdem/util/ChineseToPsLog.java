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
import java.util.Map;
import java.util.HashMap;
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
	
	private static String psMoney = "((?<yi>\\d+)亿)?((?<wan>\\d+)万)?(?<one>\\d*)?";
	private static String psMoney2 = "((?<yi2>\\d+)亿)?((?<wan2>\\d+)万)?(?<one2>\\d*)?";
	private static String psMoney3 = "((?<yi3>\\d+)亿)?((?<wan3>\\d+)万)?(?<one3>\\d*)?";
	private static Pattern pHandBB = Pattern.compile("联众德州扑克牌局 (?<table>\\S+) 盘数编号：(?<hand>\\d+) 小盲/大盲/前注:"
			+ psMoney + "/" + psMoney2 + "/" + psMoney3 + " (?<year>\\d+)年(?<month>\\d+)月(?<day>\\d+)日 (?<time>\\S+)");
	private static Pattern pBtnSeat = Pattern.compile("第(?<seat>\\d+)号座位现在是庄家");
	private static Pattern pSeatPlayer = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>.+)\\(" + psMoney + "筹码\\)");
	private static Pattern pSmallBlind = Pattern.compile("玩家(?<name>.+)：下小盲" + psMoney + ".*");
	private static Pattern pBigBlind = Pattern.compile("玩家(?<name>.+)：下大盲" + psMoney + ".*");
	private static Pattern pHoleCards = Pattern.compile("发牌给 玩家(?<name>.+)【(?<hole>.+)】");
	private static Pattern pPreflop = Pattern.compile("\\*\\*\\*\\*\\*翻牌前\\*\\*\\*\\*\\*");
	private static Pattern pBet = Pattern.compile("玩家(?<name>.+)：下注" + psMoney + "(?<allin> 全下)?");
	//private static Pattern pRaise = Pattern.compile("玩家(?<name>.+)：加注至((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)");
	private static Pattern pRaise = Pattern.compile("玩家(?<name>.+)：加注至" + psMoney + "(?<allin> 全下)?");
	//private static Pattern pCall = Pattern.compile("玩家(\\S+)：跟注至(\\d+)万(\\d*)");
	private static Pattern pCall = Pattern.compile("玩家(?<name>.+)：跟注至" + psMoney + "(?<allin> 全下)?");
	private static Pattern pCheck = Pattern.compile("玩家(?<name>.+)：让牌");
	private static Pattern pFold = Pattern.compile("玩家(?<name>.+)：弃牌");
	private static Pattern pFlop = Pattern.compile("\\*\\*\\*\\*\\*翻牌\\*\\*\\*\\*\\*【(?<flop>.+)】.*");
	private static Pattern pTurn = Pattern.compile("\\*\\*\\*\\*\\*转牌\\*\\*\\*\\*\\*【.+】【(?<turn>\\S+)】.*");
	private static Pattern pRiver = Pattern.compile("\\*\\*\\*\\*\\*河牌\\*\\*\\*\\*\\*【.+】【.+】【(?<river>\\S+)】.*");
	private static Pattern pUncalledBet = Pattern.compile("无人跟注 退还" + psMoney + "筹码给玩家(?<name>.+)");
	private static Pattern pWinFromPot = Pattern.compile("(?<name>.+)从主池赢得" + psMoney + ".*");
	private static Pattern pSummary = Pattern.compile("\\*\\*\\*\\*\\*摘要\\*\\*\\*\\*\\*");
	private static Pattern pTotalPot = Pattern.compile("底池：" + psMoney + " 主池：" + psMoney2 + ".*");
	private static Pattern pBoard = Pattern.compile("发出公共牌【(?<board>.+)】");
	private static Pattern pLoser = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>[^\\s\\(]+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*败北.*");
	private static Pattern pLoserBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>[^\\s\\(]+) (\\((?<role>\\S+)\\))*.*败北.*");
	private static Pattern pWinner = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>[^\\s\\(]+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*胜出 \\(" + psMoney + "\\).*");
	private static Pattern pWinnerBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>[^\\s\\(]+) (\\((?<role>\\S+)\\))*.*胜出 \\(" + psMoney + "\\).*");
	
	private static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
	
	private static StringBuilder output = new StringBuilder();
	private static Matcher matcher;
	private static String tableNo = "";
	private static String handNo = "";
	private static BigDecimal sb = null;
	private static BigDecimal bb = null;
	private static BigDecimal ante = null;
	private static String year = "";
	private static String month = "";
	private static String day = "";
	private static String time = "";
	private static int btnSeat = -1;
	private static boolean bSB = false;
	private static boolean bBB = false;
	private static boolean bAntePaid = false;
	private static boolean bHoleCards = false;
	private static boolean bActioned = false;
	private static BigDecimal bet = null;
	private static String flop = "";
	private static String turn = "";
	private static String river = "";
	private static BigDecimal totalPot = null;
	private static BigDecimal pot = null;
	//private static BigDecimal uncalledBet = null;
	private static String[] names = new String[10];
	private static BigDecimal[] moneys = new BigDecimal[10];
	private static BigDecimal[] oriMoneys = new BigDecimal[10];
	
	static {
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	    defaultFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
	    defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	
	public static String process(List<String> psLogs, boolean debug) throws Exception{
		
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
					sb = new BigDecimal(getMoneyString(matcher));
					bb = new BigDecimal(getMoneyString2(matcher));
					ante = new BigDecimal(getMoneyString3(matcher));
					year = matcher.group("year");
					month = matcher.group("month");
					day = matcher.group("day");
					time = matcher.group("time");
	
					output.append("\r\n\r\n\r\n").append("PokerStars Hand #")
					.append(handNo).append(":  Hold'em No Limit ($").append(sb).append("/$")
					.append(bb).append(" USD) - ").append(year).append("/").append(month)
					.append("/").append(day).append(" " + time + " ET\r\n");
					
					//This is a new start, reset variables
					btnSeat = -1;
					bSB = false;
					bBB = false;
					bAntePaid = false;
					bHoleCards = false;
					bet = null;
					flop = "";
					turn = "";
					river = "";
					totalPot = null;
					pot = null;
					//uncalledBet = null;
					for(int i = 0; i < 10; i++){
						names[i] = null;
						moneys[i] = null;
						oriMoneys[i] = null;
					}
					
					continue;
				}
	
				matcher = pBtnSeat.matcher(psLog);
				if (matcher.matches()){
					btnSeat = Integer.parseInt(matcher.group("seat"));
					//Displaying seat number starting from 1 instead of 0 now
					output.append("Table '" + tableNo + "' 9-max Seat #").append(btnSeat+1).append(" is the button\r\n");
					continue;
				}
				
				matcher = pSeatPlayer.matcher(psLog);
				if (matcher.matches()){
					int seatNo = Integer.parseInt(matcher.group("seat"));
					String name = Chn2PinYin(matcher.group("name"));
					names[seatNo] = name;
					String money = getMoneyString(matcher);
					moneys[seatNo] = new BigDecimal(money);
					oriMoneys[seatNo] = new BigDecimal(money);
					 //Displaying seat number starting from 1 instead of 0 now
					output.append("Seat ").append(seatNo+1).append(": " + name + " ($" + money + " in chips)\r\n"); 
					continue;
				}
				
				matcher = pSmallBlind.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					BigDecimal money = new BigDecimal(getMoneyString(matcher));
					
					if (!bSB){
						bSB = paySB(name, money);
					}

					continue;
				}
	
				matcher = pBigBlind.matcher(psLog);
				if (matcher.matches()){
					if (!bSB){
						bSB = paySB(null, null);
					}

					String name = Chn2PinYin(matcher.group("name"));
					bet = new BigDecimal(getMoneyString(matcher));
					if (!bBB){
						bBB = payBB(name, bet);
					}
					
					if (!bAntePaid){
						bAntePaid = payAnte();
						bet = bet.add(ante);
					}
					
					continue;
				}
	
				matcher = pHoleCards.matcher(psLog);
				if (matcher.matches()){
					if (!bSB){
						bSB = paySB(null, null);
					}
					if (!bBB){
						bBB = payBB(null, null);
						bet = bb;
					}
					if (!bAntePaid){
						bAntePaid = payAnte();
						bet = bet.add(ante);
					}

					if (!bHoleCards){
						output.append("*** HOLE CARDS ***\r\n");
						bHoleCards = true;
					}

					String holeCards = matcher.group("hole").replace("10", "T");
					output.append("Dealt to " + Chn2PinYin(matcher.group("name")) + " [" + holeCards + "]\r\n"); 
					continue;
				}
				
				matcher = pPreflop.matcher(psLog);
				if (matcher.matches()){
					if (!bSB){
						bSB = paySB(null, null);
					}
					if (!bBB){
						bBB = payBB(null, null);
						bet = bb;
					}
					if (!bAntePaid){
						bAntePaid = payAnte();
						bet = bet.add(ante);
					}

					continue;
				}
				
				matcher = pBet.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					bet = new BigDecimal(getMoneyString(matcher));
					bActioned = true;
	
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
					BigDecimal newBet = new BigDecimal(getMoneyString(matcher));
					String increment = newBet.subtract(bet).toString();
					bet = newBet;
					bActioned = true;
					
					String allin = matcher.group("allin");
					if (allin == null || allin.length() == 0){
						output.append(name + ": raises $" + increment + " to $"
							+ bet + "\r\n");
					} else {
						output.append(name + ": raises $" + increment + " to $"
							+ bet + " and is all-in\r\n");
					}
					continue;
				}
				
				matcher = pCall.matcher(psLog);
				if (matcher.matches()){
					String name = Chn2PinYin(matcher.group("name"));
					bet = new BigDecimal(getMoneyString(matcher));
					bActioned = true;
	
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
					bActioned = true;
					String name = Chn2PinYin(matcher.group("name"));
					output.append(name + ": checks\r\n");
					continue;
				}
				
				matcher = pFold.matcher(psLog);
				if (matcher.matches()){
					bActioned = true;
					output.append(Chn2PinYin(matcher.group("name")) + ": folds\r\n");
					continue;
				}
				
				matcher = pFlop.matcher(psLog);
				if (matcher.matches()){
					if (!bActioned){
						//!!!TO DO!!!
						//I don't know this is correct or not, but requested by root anyway
						int index = getPlayerIndex(2); //Get the index of the person should say something first 
						String name = names[index];
						boolean bAllin = (moneys[index].compareTo(BigDecimal.ZERO) == 0);
						if (bAllin){
							output.append(name + ": raises $0 to $"	+ oriMoneys[index] + " and is all-in\r\n");
						} else {
							String increment = bet.subtract(oriMoneys[index].subtract(moneys[index])).toString();
							output.append(name + ": raises $" + increment + " to $"	+ bet + "\r\n");
						}
						
						//Loop everybody
						for (int i = 0, offset = 3; i < getNumberOfPlayer()-1; i++, offset++){
							index = getPlayerIndex(offset);
							name = names[index];
							bAllin = (moneys[index].compareTo(BigDecimal.ZERO) == 0);
							if (bAllin){
								output.append(name + ": calls $" + oriMoneys[index] + " and is all-in\r\n");
							} else {
								output.append(name + ": calls $" + bet + "\r\n");
							}
						}
					}
					
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
					int seatNo = Integer.parseInt(matcher.group("seat"));
					String name = Chn2PinYin(matcher.group("name"));
					String role = Chn2Eng(matcher.group("role"));
					String cards = matcher.group("cards").replace("10", "T");
					output.append("Seat ").append(seatNo+1).append(": " + name + role
							+ " showed [" + cards + "] and lost\r\n");
					continue;
				} else {
					matcher = pLoserBeforeFlop.matcher(psLog);
					if (matcher.matches()){
						int seatNo = Integer.parseInt(matcher.group("seat"));
						String name = Chn2PinYin(matcher.group("name"));
						String role = Chn2Eng(matcher.group("role"));
						output.append("Seat ").append(seatNo+1).append(": " + name + role
								+ " folded before Flop\r\n");
						continue;
					}
				}
	
				matcher = pWinner.matcher(psLog);
				if (matcher.matches()){
					int seatNo = Integer.parseInt(matcher.group("seat"));
					String name = Chn2PinYin(matcher.group("name"));
					String role = Chn2Eng(matcher.group("role"));
					String cards = matcher.group("cards").replace("10", "T");
					String money = getMoneyString(matcher);
					output.append("Seat ").append(seatNo+1).append(": " + name + role
							+ " showed [" + cards + "] and Won ($" + money + ")\r\n");
					continue;
				} else {
					matcher = pWinnerBeforeFlop.matcher(psLog);
					if (matcher.matches()){
						int seatNo = Integer.parseInt(matcher.group("seat"));
						String name = Chn2PinYin(matcher.group("name"));
						String role = Chn2Eng(matcher.group("role"));
						String money = getMoneyString(matcher);
						output.append("Seat ").append(seatNo+1).append(": " + name + role
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
		File inputFile = null;
		File replaceFile = null;
		boolean debug = false; 
		List<String> psLogs = new ArrayList<String>();
		HashMap<String, String> replaces = new HashMap<>(); 
		
		if (!(args == null || args.length < 1 || args.length > 3)) {
			try {
				if (args.length == 1){
					inputFile = new File(args[0]);					
				} else if (args.length == 2) {
					if ("/d".equalsIgnoreCase(args[0])){
						debug = true;
						inputFile = new File(args[1]);
					} else {
						inputFile = new File(args[0]);
						replaceFile = new File(args[1]);
					}
				} else {
					if (!"/d".equalsIgnoreCase(args[0])){
						throw new Exception("Wrong option - " + args[0]);
					}
					debug = true;
					inputFile = new File(args[1]);
					replaceFile = new File(args[2]);
				}
				
				if (replaceFile != null){
					try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(replaceFile), "utf-8"))) {
						for(String line; (line = br.readLine()) != null; ) {
							line = line.trim();
							if (line.length() > 0){
								String[] maps = line.split(" ");
								replaces.put(maps[0], maps[1]);
							}
						}
					}
				}

				try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"))) {
				    for(String line; (line = br.readLine()) != null; ) {
				    	if (!replaces.isEmpty()){
				    		for (Map.Entry<String, String> entry : replaces.entrySet()) {
				    			line = line.replace(entry.getKey(), entry.getValue());
				    		}
				    	}
				    	
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
	
	private static void deductMoney(String name, BigDecimal money){
		int index = java.util.Arrays.asList(names).indexOf(name);
		moneys[index] = moneys[index].subtract(money);
	}
	
	private static boolean paySB(String name, BigDecimal money){
		if (name == null) //Need to get sb name
		{
			name = names[getPlayerIndex(0)];
			money = sb;
		}
		
		deductMoney(name, money);
		output.append(name).append(": posts small blind $")
		.append(money).append("\r\n"); 

		return true;
	}
	
	private static boolean payBB(String name, BigDecimal money){
		if (name == null) //Need to get sb name
		{
			name = names[getPlayerIndex(1)];
			money = bb;
		}
		
		deductMoney(name, money);
		output.append(name).append(": posts big blind $").append(money).append("\r\n");

		return true;
	}

	private static boolean payAnte(){
		for (int i = 0; i < names.length; i++){
			if (names[i] != null){
				if (moneys[i].compareTo(ante) > 0){
					output.append(names[i] + ": posts the ante $").append(ante).append("\r\n");					
					moneys[i] = moneys[i].subtract(ante);
				} else {
					output.append(names[i] + ": posts the ante $").append(moneys[i]).append("\r\n");					
					moneys[i] = new BigDecimal("0");
				}
			}
		}
		return true;
	}
	
	//Get the player index, argument is the offset from small blind
	//E.g. when offset = 0 it returns small blind index,
	//offset = 1 get big blind index, etc
	private static int getPlayerIndex(int offset){
		int numberOfPlayer = getNumberOfPlayer();
		offset = offset % numberOfPlayer;
		
		//Small blind index
		int sbIndex = btnSeat;
		if (numberOfPlayer != 2){
			sbIndex = getPlayerIndex(btnSeat+1, 1);
		}
		
		//If we are looking for small blind index
		if (offset == 0)
			return sbIndex;
		
		return getPlayerIndex(sbIndex+1, offset);
	}
	
	private static int getPlayerIndex(int startIndex, int offset){
		for ( ; ; startIndex++){
			if (startIndex >= names.length)
				startIndex = startIndex%names.length;
			if (names[startIndex] != null){
				offset--;
				if (offset == 0)
					break;
			}
		}
		
		return startIndex;
	}
	
	private static int getNumberOfPlayer(){
		int numberOfPlayer = 0;
		for (int i = 0; i < names.length; i++){
			if (names[i] != null)
				numberOfPlayer++;
		}
		
		return numberOfPlayer;
	}
	
	private static String getMoneyString(Matcher matcher){
		double money = 0;
		String yi = matcher.group("yi");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one");
		if (sDecimal != null && !sDecimal.isEmpty()){
			money += Integer.parseInt(sDecimal) / 10000.0;
		}
		
		return Double.toString(money);
	}

	private static String getMoneyString2(Matcher matcher){
		double money = 0;
		String yi = matcher.group("yi2");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan2");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one2");
		if (sDecimal != null && !sDecimal.isEmpty()){
			money += Integer.parseInt(sDecimal) / 10000.0;
		}
		
		return Double.toString(money);
	}

	private static String getMoneyString3(Matcher matcher){
		double money = 0;
		String yi = matcher.group("yi3");
		if (yi != null && yi.length() > 0){
			money = Integer.parseInt(yi) * 10000;
		}
		
		String wan = matcher.group("wan3");
		if (wan != null && wan.length() > 0){
			money += Integer.parseInt(wan);
		}
		
		String sDecimal = matcher.group("one3");
		if (sDecimal != null && !sDecimal.isEmpty()){
			money += Integer.parseInt(sDecimal) / 10000.0;
		}
		
		return Double.toString(money);
	}
}
