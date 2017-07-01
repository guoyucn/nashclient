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

/**
 * @author sheng
 *
 */
public class PsToShortLog {
	private static Pattern pHandBB = Pattern.compile("PokerStars Hand #(\\d+):.*Level.*\\(\\d+/(\\d+)\\).*");
	private static Pattern pBtnSeat = Pattern.compile(".*Seat #(\\d+) is the button");
	private static Pattern pSeatlayer = Pattern.compile("Seat (\\d+): (\\S+) \\((\\d+) in chips\\)");
	private static Pattern pHoleCards = Pattern.compile("Dealt to \\S+ \\[(.+)\\]");
	private static Pattern pRaise = Pattern.compile("(\\S+): raises (\\d+) to (\\d+)");
	private static Pattern pCall = Pattern.compile("(\\S+): calls (\\d+).*");
	private static Pattern pFlop = Pattern.compile("\\*\\*\\* FLOP \\*\\*\\* \\[(.+)\\]");
	private static Pattern pCheck = Pattern.compile("(\\S+): checks");
	private static Pattern pBet = Pattern.compile("(\\S+): bets (\\d+)");
	private static Pattern pTurn = Pattern.compile("\\*\\*\\* TURN \\*\\*\\*.+\\[(\\S{2})\\]");
	private static Pattern pRiver = Pattern.compile("\\*\\*\\* RIVER \\*\\*\\*.+\\[(\\S{2})\\]");
	private static Pattern pFold = Pattern.compile("(\\S+): folds");
	private static Pattern pAllIn = Pattern.compile("(\\S+): (bets|raises).+and is all-in");
	
	public static String process(String heroName, List<String> psLogs) throws Exception{
		StringBuilder output = new StringBuilder();
		Matcher matcher;
		String handNo = "";
		String btnSeat = "";
		String heroSeat = "";
		String player_1 = "";
		String player_2 = "";
		
		for(String psLog : psLogs){	
			if (psLog == null)
				continue;
			psLog = psLog.trim();
			if(psLog.isEmpty())
				continue;
			
			matcher = pHandBB.matcher(psLog);
			if (matcher.matches()){
				handNo = matcher.group(1);
				output.append("\r\n").append(handNo).append(" ").append(matcher.group(2));
				
				//This is a new start, reset variables
				btnSeat = "";
				heroSeat = "";
				player_1 = "";
				player_2 = "";
				
				continue;
			}

			matcher = pBtnSeat.matcher(psLog);
			if (matcher.matches()){
				btnSeat = matcher.group(1);
				continue;
			}
			
			matcher = pSeatlayer.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(matcher.group(3));
				if (matcher.group(1).equals("1")){
					player_1 = matcher.group(2);
				} else {
					player_2 = matcher.group(2);
					
					if (player_1.equals(heroName)){
						heroSeat = "1";
					} else if (player_2.equals(heroName)){
						heroSeat = "2";
					} else {
						throw new Exception(String.format("No player name is hero name '%s' in hand# %s", heroName, handNo));
					}
					
					if(heroSeat.equals(btnSeat)){
						output.append(" S");
					} else {
						output.append(" B");
					}
				}
				continue;
			}
			
			matcher = pHoleCards.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(matcher.group(1).replace(" ", ""));
				continue;
			}
			
			matcher = pRaise.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.r" : "V.r")
					.append(matcher.group(2));
				continue;
			}
			
			matcher = pCall.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.c" : "V.c");
				continue;
			}
			
			matcher = pFlop.matcher(psLog);
			if (matcher.matches()){
				output.append(" F.").append(matcher.group(1).replace(" ", ""));
				continue;
			}
			
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
			
			matcher = pTurn.matcher(psLog);
			if (matcher.matches()){
				output.append(" T.").append(matcher.group(1));
				continue;
			}
			
			matcher = pRiver.matcher(psLog);
			if (matcher.matches()){
				output.append(" R.").append(matcher.group(1));
				continue;
			}
			
			matcher = pFold.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.f" : "V.f");
				continue;
			}
			
			matcher = pAllIn.matcher(psLog);
			if (matcher.matches()){
				output.append(" ").append(heroName.equals(matcher.group(1)) ? "H.s" : "V.s");
				continue;
			}
			
		}
		return output.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String heroName;
		File inputFile;
		List<String> psLogs = new ArrayList<String>();
		
		if (!(args == null || args.length != 2)) {
			try {
				if(args[0].startsWith("H=")){
					heroName = args[0].substring(2);
				}
				else {
					throw new Exception("Hero name is not specified.");
				}
				
				inputFile = new File(args[1]);
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
					writer.write(PsToShortLog.process(heroName, psLogs));
				}

			} catch (Exception e) {
				logError(e);
				return;
			}
		}

	}

	private static void logError(Exception e) {
		System.out.println("*****************************************");
		System.out.println("Usage : PsToShortLog H=HeroName InputFile");
		System.out.println("*****************************************");
		e.printStackTrace();
	}
}
