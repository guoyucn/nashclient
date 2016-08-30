/**
 * 
 */
package org.ozsoft.texasholdem.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	private static Pattern pBet = Pattern.compile("(\\S+): bets (\\d+)");
	//private static Pattern pTurn = Pattern.compile("\\*\\*\\* TURN \\*\\*\\*.+\\[(\\S{2})\\]");
	//private static Pattern pRiver = Pattern.compile("\\*\\*\\* RIVER \\*\\*\\*.+\\[(\\S{2})\\]");
	//private static Pattern pFold = Pattern.compile("(\\S+): folds");
	private static Pattern pAllIn = Pattern.compile("(\\S+): (bets|raises).+and is all-in");
	
	//((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)
	private static Pattern pHandBB = Pattern.compile("联众德州扑克牌局 (?<table>\\S+) 盘数编号：(?<hand>\\d+) 小盲/大盲/前注:(?<sb>\\d+)万/(?<bb>\\d+)万/(?<ante>\\d+)万 (?<year>\\d+)年(?<month>\\d+)月(?<day>\\d+)日 (?<time>\\S+)");
	private static Pattern pBtnSeat = Pattern.compile("第(?<seat>\\d+)号座位现在是庄家");
	private static Pattern pSeatPlayer = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+)\\(((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)筹码\\)");
	private static Pattern pSmallBlind = Pattern.compile("玩家(?<name>\\S+)：下小盲(?<sb>\\d+)万");
	private static Pattern pBigBlind = Pattern.compile("玩家(?<name>\\S+)：下大盲(?<bb>\\d+)万");
	private static Pattern pHoleCards = Pattern.compile("发牌给 玩家(?<name>\\S+)【(?<hole>.+)】");
	//private static Pattern pRaise = Pattern.compile("玩家(?<name>\\S+)：加注至((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)");
	private static Pattern pRaise = Pattern.compile("玩家(?<name>\\S+)：加注至((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)(?<allin> 全下)?");
	//private static Pattern pCall = Pattern.compile("玩家(\\S+)：跟注至(\\d+)万(\\d*)");
	private static Pattern pCall = Pattern.compile("玩家(?<name>\\S+)：跟注至((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)(?<allin> 全下)?");
	private static Pattern pCheck = Pattern.compile("玩家(?<name>\\S+)：让牌");
	private static Pattern pFold = Pattern.compile("玩家(?<name>\\S+)：弃牌");
	private static Pattern pFlop = Pattern.compile("\\*\\*\\*\\*\\*翻牌\\*\\*\\*\\*\\*【(?<flop>.+)】.*");
	private static Pattern pTurn = Pattern.compile("\\*\\*\\*\\*\\*转牌\\*\\*\\*\\*\\*【.+】【(?<turn>\\S+)】.*");
	private static Pattern pRiver = Pattern.compile("\\*\\*\\*\\*\\*河牌\\*\\*\\*\\*\\*【.+】【.+】【(?<river>\\S+)】.*");
	private static Pattern pUncalledBet = Pattern.compile("无人跟注 退还((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)筹码给玩家(?<name>\\S+)");
	private static Pattern pWinFromPot = Pattern.compile("(?<name>\\S+)从主池赢得((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)");
	private static Pattern pSummary = Pattern.compile("\\*\\*\\*\\*\\*摘要\\*\\*\\*\\*\\*");
	private static Pattern pTotalPot = Pattern.compile("底池：((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*) 主池：((?<yi2>\\d+)亿)?(?<wan2>\\d+)万(?<one2>\\d*)");
	private static Pattern pBoard = Pattern.compile("发出公共牌【(?<board>.+)】");
	private static Pattern pLoser = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*败北.*");
	private static Pattern pLoserBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*败北.*");
	private static Pattern pWinner = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*【(?<cards>.+)】.*胜出 \\(((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)\\).*");
	private static Pattern pWinnerBeforeFlop = Pattern.compile("第(?<seat>\\d+)号座位：玩家(?<name>\\S+) (\\((?<role>\\S+)\\))*.*胜出 \\(((?<yi>\\d+)亿)?(?<wan>\\d+)万(?<one>\\d*)\\).*");
	public static String process(List<String> psLogs) throws Exception{
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
			
			matcher = pHandBB.matcher(psLog);
			if (matcher.matches()){
				tableNo = matcher.group("table");
				handNo = matcher.group("hand");
				sb = matcher.group("sb");
				bb = matcher.group("bb");
				ante = matcher.group("ante");
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
				String name = matcher.group("name");
				names[seatNo] = name;
				output.append("Seat ").append(seatNo).append(": " + name + " ($" + getMoneyString(matcher)
				+ " in chips)\r\n"); 
				continue;
			}
			
			matcher = pSmallBlind.matcher(psLog);
			if (matcher.matches()){
				output.append(matcher.group("name")).append(": posts small blind $")
				.append(matcher.group("sb")).append("\r\n"); 
				continue;
			}

			matcher = pBigBlind.matcher(psLog);
			if (matcher.matches()){
				bet = matcher.group("bb");
				output.append(matcher.group("name") + ": posts big blind $" + bet + "\r\n");
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
				output.append("Dealt to " + matcher.group("name") + " [" + holeCards + "]\r\n"); 
				continue;
			}
			
			matcher = pRaise.matcher(psLog);
			if (matcher.matches()){
				String name = matcher.group("name");
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
				String name = matcher.group("name");
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
				String name = matcher.group("name");
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
				output.append(matcher.group("name") + ": folds\r\n");
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
				String name = matcher.group("name");
				output.append("Uncalled bet ($" + money + ") returned to " + name + "\r\n");
				continue;
			}
			
			matcher = pWinFromPot.matcher(psLog);
			if (matcher.matches()){
				String name = matcher.group("name");
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
				String name = matcher.group("name");
				String role = Chn2Eng(matcher.group("role"));
				String cards = matcher.group("cards").replace("10", "T");
				output.append("Seat " + seatNo + ": " + name + role
						+ " showed [" + cards + "] and lost\r\n");
				continue;
			} else {
				matcher = pLoserBeforeFlop.matcher(psLog);
				if (matcher.matches()){
					String seatNo = matcher.group("seat");
					String name = matcher.group("name");
					String role = Chn2Eng(matcher.group("role"));
					output.append("Seat " + seatNo + ": " + name + role
							+ " folded before Flop\r\n");
					continue;
				}
			}

			matcher = pWinner.matcher(psLog);
			if (matcher.matches()){
				String seatNo = matcher.group("seat");
				String name = matcher.group("name");
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
					String name = matcher.group("name");
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
			
			output.append("------Unhandled------>>" + psLog + "\r\n");
		}
		return output.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File inputFile;
		List<String> psLogs = new ArrayList<String>();
		
		if (!(args == null || args.length != 1)) {
			try {
				inputFile = new File(args[0]);
				try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
				    for(String line; (line = br.readLine()) != null; ) {
				    	psLogs.add(line);
				    }
				}
				
				String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
				String inputFileStr = inputFile.getAbsolutePath();
				String outputFileStr = String.format("%s_%s.txt",
						inputFileStr.substring(0, inputFileStr.lastIndexOf(".")), time);
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileStr), "utf-8"))) {
					writer.write(ChineseToPsLog.process(psLogs));
				}

			} catch (Exception e) {
				logError(e);
				return;
			}
		}

	}

	private static void logError(Exception e) {
		System.out.println("*****************************************");
		System.out.println("Usage : ChineseToPsLog InputFile");
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
}
