// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.ozsoft.texasholdem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

import org.ozsoft.texasholdem.actions.Action;
import org.ozsoft.texasholdem.actions.BetAction;
import org.ozsoft.texasholdem.actions.RaiseAction;
import org.ozsoft.texasholdem.util.InputOutputMgr;

/**
 * Limit Texas Hold'em poker table. <br />
 * <br />
 * 
 * This class forms the heart of the poker engine. It controls the game flow for a single poker table.
 * 
 * @author Oscar Stigter
 */
public class Table {
    
    /** In fixed-limit games, the maximum number of raises per betting round. */
    private static final int MAX_RAISES = 3;
    
    /** Whether players will always call the showdown, or fold when no chance. */
    private static final boolean ALWAYS_CALL_SHOWDOWN = false;
    
    /** Table type (poker variant). */
    private final TableType tableType;
    
    /** The size of the big blind. */
    //private final int bigBlind;
    private int bigBlind;
    
    /** The players at the table. */
    private final List<Player> players;
    
    /** The active players in the current hand. */
    private final List<Player> activePlayers;
    
    /** The deck of cards. */
    private Deck deck;
    
    /** The community cards on the board. */
    private final List<Card> board;
    
    /** The current dealer position. */
    private int dealerPosition;
    
    private int ante;

    /** The current dealer. */
    private Player dealer;

    /** The position of the acting player. */
    private int actorPosition;
    
    /** The acting player. */
    private Player actor;

    /** The minimum bet in the current hand. */
    private int minBet;
    
    /** The current bet in the current hand. */
    private int bet;
    
    /** All pots in the current hand (main pot and any side pots). */
    private final List<Pot> pots;
    
    /** The player who bet or raised last (aggressor). */
    private Player lastBettor;
    
    /** Number of raises in the current betting round. */
    private int raises;
    
    private StringBuilder commandBuilder = new StringBuilder();
    
    private StringBuilder commandBuilder2 = new StringBuilder();
    
    private StringBuilder outputBuilder = new StringBuilder();
    
    private long handNumber = 0;

    private boolean allin = false;

	private int anteInTimesOfBB;
    
    /**
     * Constructor.
     * 
     * @param bigBlind
     *            The size of the big blind.
     */
    public Table(TableType type, int bigBlind) {
        this.tableType = type;
        this.bigBlind = bigBlind;
        players = new ArrayList<Player>();
        activePlayers = new ArrayList<Player>();
        deck = new Deck();
        board = new ArrayList<Card>();
        pots = new ArrayList<Pot>();
    }
    
    /**
     * Adds a player.
     * 
     * @param player
     *            The player.
     */
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    /**
     * Main game loop.
     */
    public void run() {
        for (Player player : players) {
            player.getClient().joinedTable(tableType, bigBlind, players);
        }
        dealerPosition = -1;
        actorPosition = -1;
        while (true) {
        	allin = false;
        	//If input from file
        	if (InputOutputMgr.INSTANCE.getInputType() == InputOutputMgr.InputType.File){
        		if (!InputOutputMgr.INSTANCE.next())
        			break;
        		
        		dealerPosition = InputOutputMgr.INSTANCE.getDealerPosition();
        		bigBlind = InputOutputMgr.INSTANCE.getBigBlind();
        		ante = InputOutputMgr.INSTANCE.getAnte();
        		
        		players.get(dealerPosition).setCash(InputOutputMgr.INSTANCE.getMoney1());
        		players.get((dealerPosition+1)%players.size()).setCash(InputOutputMgr.INSTANCE.getMoney2());
        	}else{
        		bigBlind = (int) (20 + (handNumber/3)*10);
        		ante = bigBlind * anteInTimesOfBB;
        	}
        		
            int noOfActivePlayers = 0;
            for (Player player : players) {
                if (player.getCash() >= bigBlind) {
                    noOfActivePlayers++;
                }
            }
            if (noOfActivePlayers > 1) {
                playHand();
                //Log result
            	if (InputOutputMgr.INSTANCE.getInputType() == InputOutputMgr.InputType.File){
            		InputOutputMgr.INSTANCE.logResult(outputBuilder.toString());
            	}          	
            	//Show result in command line.
            	System.out.print(outputBuilder.toString());
            } else {
            	if (InputOutputMgr.INSTANCE.getInputType() != InputOutputMgr.InputType.File){
	            	//reset player.
	            	for (Player player : players) {
	                    player.setCash(500);
	                }
	            	handNumber = 0;
	                //break;
            	}
            }
        }
        
        // Game over.
	//        board.clear();
	//        pots.clear();
	//        bet = 0;
	//        notifyBoardUpdated();
	//        for (Player player : players) {
	//            player.resetHand();
	//        }
	//        notifyPlayersUpdated(false);
	//        notifyMessage("Game over.");
    }
    
    /**
     * Plays a single hand.
     */
    private void playHand() {
    	if (InputOutputMgr.INSTANCE.getInputType() == InputOutputMgr.InputType.File){
    		handNumber = System.currentTimeMillis();//Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
    	} else {
    		handNumber++;
    	}
        
    	resetHand();
        //<--
        commandBuilder = new StringBuilder();
        commandBuilder.append(handNumber).append(" ").append(bigBlind);
        commandBuilder.append(" ").append(ante);
        commandBuilder.append(" ").append(players.get(1).getCash());
        commandBuilder.append(" ").append(players.get(0).getCash());
        
        commandBuilder2 = new StringBuilder();
        commandBuilder2.append(handNumber).append(" ").append(bigBlind);
        commandBuilder2.append(" ").append(ante);
        commandBuilder2.append(" ").append(players.get(0).getCash());
        commandBuilder2.append(" ").append(players.get(1).getCash());
        
        
        if (dealerPosition == 1){
        	commandBuilder.append(" ").append("S");
        	commandBuilder2.append(" ").append("B");
        	
		}else{
			commandBuilder.append(" ").append("B");
			commandBuilder2.append(" ").append("S");
		}
        //-->
        
        //output
        outputBuilder = new StringBuilder();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String inputFileTimeStamp = InputOutputMgr.INSTANCE.getInputFileTimeStamp();
        outputBuilder.append("PokerStars Hand #").append(handNumber).append(": ")
        	.append("Tournament #").append(inputFileTimeStamp).append(", $1 USD Hold'em No Limit - Match Round I, Level I (")
        	.append(bigBlind/2).append("/").append(bigBlind).append(") - ")
        	.append(df.format(new Date())).append("\r\n");
        outputBuilder.append("Table '").append(inputFileTimeStamp).append("' 2-max Seat #").append(dealerPosition+1).append(" is the button\r\n");
        outputBuilder.append("Seat 1: ").append(activePlayers.get(0).getName()).append(" (").append(activePlayers.get(0).getCash()).append(" in chips)\r\n");
        outputBuilder.append("Seat 2: ").append(activePlayers.get(1).getName()).append(" (").append(activePlayers.get(1).getCash()).append(" in chips)\r\n");  
        // Small blind.
        if (activePlayers.size() > 2) {
            rotateActor();
        }
        postAnte();
        rotateActor();
        postAnte();
        rotateActor();
        postSmallBlind();
        
        // Big blind.
        rotateActor();
        postBigBlind();
        
        // Pre-Flop.
        dealHoleCards();
        
        //<--
        commandBuilder.append(" ").append(players.get(1).getCards()[0]).append(players.get(1).getCards()[1]);
        commandBuilder2.append(" ").append(players.get(0).getCards()[0]).append(players.get(0).getCards()[1]);
        
        //-->
        
        doBettingRound(true);
        
        // Flop.
        if (activePlayers.size() > 1) {
            bet = 0;
            dealCommunityCards("Flop", 3);
            commandBuilder.append(" ").append("F.").append(board.get(0)).append(board.get(1)).append(board.get(2));
            commandBuilder2.append(" ").append("F.").append(board.get(0)).append(board.get(1)).append(board.get(2));
            
            doBettingRound(false);

            // Turn.
            if (activePlayers.size() > 1) {
                bet = 0;
                dealCommunityCards("Turn", 1);
                commandBuilder.append(" ").append("T.").append(board.get(3));
                commandBuilder2.append(" ").append("T.").append(board.get(3));
                //minBet = 2 * bigBlind;
                doBettingRound(false);

                // River.
                if (activePlayers.size() > 1) {
                    bet = 0;
                    dealCommunityCards("River", 1);
                    commandBuilder.append(" ").append("R.").append(board.get(4));
                    commandBuilder2.append(" ").append("R.").append(board.get(4));
                    doBettingRound(false);

                    // Showdown.
                    if (activePlayers.size() > 1) {
                        bet = 0;
                        doShowdown();
                    }
                }
            }
        }
    }
    


	/**
     * Resets the game for a new hand.
     */
    private void resetHand() {
        // Clear the board.
        board.clear();
        pots.clear();
        notifyBoardUpdated();
        
        // Determine the active players.
        activePlayers.clear();
        for (Player player : players) {
            player.resetHand();
            // Player must be able to afford at least the big blind.
            if (player.getCash() >= bigBlind) {
                activePlayers.add(player);
            }
        }
        
        if (InputOutputMgr.INSTANCE.getInputType() == InputOutputMgr.InputType.File){
        	dealerPosition = InputOutputMgr.INSTANCE.getDealerPosition();
        	deck = InputOutputMgr.INSTANCE.getDeck();
        } else {
            // Rotate the dealer button.
        	dealerPosition = (dealerPosition + 1) % activePlayers.size();
            // Shuffle the deck.
            deck.shuffle();
        }
        
        dealer = activePlayers.get(dealerPosition);


        // Determine the first player to act.
        actorPosition = dealerPosition;
        actor = activePlayers.get(actorPosition);
        
        // Set the initial bet to the big blind.
        minBet = bigBlind;
        bet = minBet;
        
        // Notify all clients a new hand has started.
        for (Player player : players) {
            player.getClient().handStarted(dealer);
        }
        notifyPlayersUpdated(false);
        notifyMessage("New hand, %s is the dealer.", dealer);
    }

    /**
     * Rotates the position of the player in turn (the actor).
     */
    private void rotateActor() {
        actorPosition = (actorPosition + 1) % activePlayers.size();
        actor = activePlayers.get(actorPosition);
        for (Player player : players) {
            player.getClient().actorRotated(actor);
        }
    }
    
    /**
     * Posts the small blind.
     */
    private void postSmallBlind() {
        final int smallBlind = bigBlind / 2;
        actor.postSmallBlind(smallBlind);
        contributePot(smallBlind);
        notifyBoardUpdated();
        notifyPlayerActed();
        
        //output
        outputBuilder.append(actor.getName()).append(": posts small blind ")
        	.append(smallBlind).append("\r\n");
    }
    
    /**
     * Posts the big blind.
     */
    private void postBigBlind() {
        actor.postBigBlind(bigBlind);
        contributePot(bigBlind);
        notifyBoardUpdated();
        notifyPlayerActed();

        //output
        outputBuilder.append(actor.getName()).append(": posts big blind ")
        	.append(bigBlind).append("\r\n");
    }
    
    private void postAnte() {
    	actor.postAnte(ante);
        contributePot(ante);
        notifyBoardUpdated();
        notifyPlayerActed();
        outputBuilder.append(actor.getName()).append(": posts the ante ").append(ante).append("\r\n");

	}
    /**
     * Deals the Hole Cards.
     */
    private void dealHoleCards() {
    	//output
    	outputBuilder.append("*** HOLE CARDS ***\r\n");
    	    	
    	/*
    	for (Player player : activePlayers) {
            player.setCards(deck.deal(2));
        }
		*/
    	//Deal hole cards from dealer
    	for(int i = dealerPosition; i < activePlayers.size() + dealerPosition; i++) {
    		activePlayers.get(i%activePlayers.size()).setCards(deck.deal(2));
    		
    		//output
    		outputBuilder.append("Dealt to ")
    			.append(activePlayers.get(i%activePlayers.size()).getName()).append(" [")
    			.append(Card.cardstoString(activePlayers.get(i%activePlayers.size()).getCards(), " "))
    			.append("]\r\n");
    	}
    	
        System.out.println();
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the hole cards.", dealer);
    }
    
    /**
     * Deals a number of community cards.
     * 
     * @param phaseName
     *            The name of the phase.
     * @param noOfCards
     *            The number of cards to deal.
     */
    private void dealCommunityCards(String phaseName, int noOfCards) {
        for (int i = 0; i < noOfCards; i++) {
            board.add(deck.deal());
        }
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the %s.", dealer, phaseName);
        
        //output
        if (board.size() == 3){
	        outputBuilder.append("*** ").append(phaseName.toUpperCase())
	        	.append(" *** [").append(Card.cardstoString(board, " "))
	        	.append("]\r\n");
        } else {
        	List<Card> cards = board.subList(0, board.size()-1);
        	outputBuilder.append("*** ").append(phaseName.toUpperCase())
        		.append(" *** [").append(Card.cardstoString(cards, " "))
        		.append("] [").append(board.get(board.size()-1)).append("]\r\n");
        }
    }
    
    /**
     * Performs a betting round.
     */
    private void doBettingRound(boolean isPreFlop) {
        // Determine the number of active players.
        int playersToAct = activePlayers.size();
        minBet = bigBlind;
        // Determine the initial player and bet size.
        if (board.size() == 0) {
            // Pre-Flop; player left of big blind starts, bet is the big blind.
            bet = bigBlind;
        } else {
            // Otherwise, player left of dealer starts, no initial bet.
            actorPosition = dealerPosition;
            bet = 0;
        }
//        if (playersToAct == 2 && isPreFlop){
//            bet = bigBlind/2;
//        }
        if (playersToAct == 2 && !isPreFlop) {
            // Heads Up mode; player who is not the dealer starts.
            actorPosition = dealerPosition;
        }
        
        lastBettor = null;
        raises = 0;
        notifyBoardUpdated();

        //If already all in, just do nothing
    	if (allin)
    		return;

    	while (playersToAct > 0) {
            rotateActor();
            //boolean isHuman;
            Action action = null;

            if (actor.isAllIn()) {
                // Player is all-in, so must check.
                //action = Action.CHECK;
                playersToAct--;
            } else {
                // Otherwise allow client to act.
                Set<Action> allowedActions = getAllowedActions(actor);
                String command = null;
                if (actor == players.get(0)){
                	command = commandBuilder2.toString();
                }else{
                	command = commandBuilder.toString();
                }
                action = actor.getClient().act(minBet, bet, allowedActions, command);
                // Verify chosen action to guard against broken clients (accidental or on purpose).
                if (!allowedActions.contains(action)) {
                    if (action instanceof BetAction && !allowedActions.contains(Action.BET)) {
                        //throw new IllegalStateException(String.format("Player '%s' acted with illegal Bet action", actor));
                    } else if (action instanceof RaiseAction && !allowedActions.contains(Action.RAISE)) {
                        //throw new IllegalStateException(String.format("Player '%s' acted with illegal Raise action", actor));
                    }
                }
                playersToAct--;
                
                if (actor == players.get(0)){
                	//isHuman = true;
                	commandBuilder.append(" ").append("V.");
                	commandBuilder2.append(" ").append("H.");
                }else{
                	//isHuman = false;
                	commandBuilder.append(" ").append("H.");
                	commandBuilder2.append(" ").append("V.");
                }
                	
            	//output
            	outputBuilder.append(actor.getName()).append(": ");
                
                if (action == Action.CHECK) {
                	commandBuilder.append("k");
                	commandBuilder2.append("k");
                    // Do nothing.
                	//output
                	outputBuilder.append("checks\r\n");
                } else if (action == Action.CALL) {
                	commandBuilder.append("c");
                	commandBuilder2.append("c");
                	boolean partialCall = false;
                    int betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                        partialCall = true;
                    }
                    actor.payCash(betIncrement);
                    actor.setBet(actor.getBet() + betIncrement);
                    contributePot(betIncrement);

                	//output
                	outputBuilder.append("calls ").append(betIncrement)
                	.append(partialCall ? " and is all-in" : "").append("\r\n");
                	
                	if (partialCall){
                		uncallBet();
                	}
                		
                } else if (action instanceof BetAction) {
                    int amount = action.getAmount();
                	outputBuilder.append("bets ").append(amount).append("\r\n");
                    //if (isHuman){
                    //	amount = amount - bet;
                    //}
                    if (amount < minBet && amount < actor.getCash()) {
                        //throw new IllegalStateException("Illegal client action: bet less than minimum bet!");
                    }
                    
                    actor.setBet(amount);
                    actor.payCash(amount);
                    contributePot(amount);
                    bet = amount;
                    minBet = amount;
                    lastBettor = actor;
                    playersToAct = activePlayers.size() - 1;
                    
                    commandBuilder.append("r").append(amount);
                    commandBuilder2.append("r").append(amount);
                	//output
                } else if (action instanceof RaiseAction) {
                    int amount =  action.getAmount();
                    /*
                    if (isHuman){
                    	//outputBuilder.append("raises ").append(amount).append("\r\n");
                    	amount = amount - bet;
                    }else{
                    	//outputBuilder.append("raises ").append(amount + bet).append("\r\n");
                    }
                    */
                    if (amount < minBet && amount < actor.getCash()) {
                        //throw new IllegalStateException("Illegal client action: raise less than minimum bet!");
                    }
                    minBet = amount;
                    int betIncrement = bet - actor.getBet() + amount;
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    bet = bet + amount;
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributePot(betIncrement);
                    lastBettor = actor;
                    raises++;
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) { 
                        // All players get another turn.
                        playersToAct = activePlayers.size() - 1;
                    } else {
                        // Max. number of raises reached; other players get one more turn.
                        playersToAct = activePlayers.size() - 1;
                    }
                    commandBuilder.append("r").append(amount);
                    commandBuilder2.append("r").append(amount);
                	//output
                    outputBuilder.append("raises ").append(amount).append(" to ").append(actor.getBet()).append("\r\n");
                    
                    //If another player already allin
                    if (allin){
                    	uncallBet();
                    }
                } else if (action == Action.FOLD) {
                	//output
                	outputBuilder.append("folds\r\n");

                	actor.setCards(null);
                    activePlayers.remove(actor);
                    actorPosition--;
                    if (activePlayers.size() == 1) {
                        // Only one player left, so he wins the entire pot.
                        notifyBoardUpdated();
                        notifyPlayerActed();
                        Player winner = activePlayers.get(0);
                        int amount = getTotalPot();
                        winner.win(amount);
                        notifyBoardUpdated();
                        notifyMessage("FOLD, %s wins $ %d [show].", winner, amount);
                        playersToAct = 0;
                    	//output
                        outputBuilder.append(winner.getName()).append(" collected ")
                        	.append(amount).append(" from pot\r\n")
                        	.append("*** SUMMARY ***\r\n")
                        	.append("Total pot ").append(amount).append(" | Rake 0\r\n")
                        	.append("Board [").append(Card.cardstoString(board, " ")).append("]\r\n");
                        
                        for(int i = 0; i < players.size(); i++){
                        	outputBuilder.append("Seat ").append(i+1).append(": ").append(players.get(i).getName())
                        		.append(dealerPosition == i ? " (button) (small blind) " : " (big blind) ")
                        		.append(players.get(i) == winner ? "won (" + amount + ")\r\n" : " mucked\r\n");
                        }
                        outputBuilder.append("\r\n\r\n\r\n");
                    }

                    commandBuilder.append("f");
                    commandBuilder2.append("f");
                } else if (action == Action.ALL_IN){
                	int amount = actor.getCash();
                	int raise = actor.getBet() + amount - bet;
                	
                	
                	bet = actor.getBet() + amount;
                    minBet = amount;
                    int betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributePot(betIncrement);
                    lastBettor = actor;
                    raises++;
                    
                    playersToAct = activePlayers.size() - 1;
                    commandBuilder.append("r").append(raise);
                    commandBuilder2.append("r").append(raise);
                	//outputBuilder.append("raises ").append(amount).append("\r\n");
                	outputBuilder.append("raises ").append(raise).append(" to ").append(actor.getBet()).append("\r\n");
                	
                	//If another player already allin
                    if (allin){
                    	uncallBet();
                    }
                }
                else {
                    // Programming error, should never happen.
                    throw new IllegalStateException("Invalid action: " + action);
                }
            }
            if(actor.isAllIn()){
            	allin = true;
            }
            actor.setAction(action);
            if (playersToAct > 0) {
                notifyBoardUpdated();
                notifyPlayerActed();
            }
        }
        
        // Reset player's bets.
        for (Player player : activePlayers) {
            player.resetBet();
        }
        notifyBoardUpdated();
        notifyPlayersUpdated(false);
    }
    
    /**
     * Returns the allowed actions of a specific player.
     * 
     * @param player
     *            The player.
     * 
     * @return The allowed actions.
     */
    private Set<Action> getAllowedActions(Player player) {
        Set<Action> actions = new HashSet<Action>();
        if (player.isAllIn()) {
            //actions.add(Action.CHECK);
        } else {
            int actorBet = actor.getBet();
            if (bet == 0) {
                actions.add(Action.CHECK);
                if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                    actions.add(Action.BET);
                    actions.add(Action.ALL_IN);
                }
            } else {
                if (actorBet < bet) {
                    actions.add(Action.CALL);
                    if ((tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2)
                    	&& actorBet + actor.getCash() > bet) {
                        actions.add(Action.RAISE);
                        actions.add(Action.ALL_IN);
                    }
                } else {
                    actions.add(Action.CHECK);
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        actions.add(Action.RAISE);
                        actions.add(Action.ALL_IN);
                    }
                }
            }
            actions.add(Action.FOLD);
        }
        return actions;
    }
    
    /**
     * Contributes to the pot.
     * 
     * @param amount
     *            The amount to contribute.
     */
    private void contributePot(int amount) {
        for (Pot pot : pots) {
            if (!pot.hasContributer(actor)) {
                int potBet = pot.getBet();
                if (amount >= potBet) {
                    // Regular call, bet or raise.
                    pot.addContributer(actor);
                    amount -= pot.getBet();
                } else {
                    // Partial call (all-in); redistribute pots.
                    pots.add(pot.split(actor, amount));
                    amount = 0;
                }
            }
            if (amount <= 0) {
                break;
            }
        }
        if (amount > 0) {
            Pot pot = new Pot(amount);
            pot.addContributer(actor);
            pots.add(pot);
        }
    }
    
    /**
     * Performs the showdown.
     */
    private void doShowdown() {
//        System.out.println("\n[DEBUG] Pots:");
//        for (Pot pot : pots) {
//            System.out.format("  %s\n", pot);
//        }
//        System.out.format("[DEBUG]  Total: %d\n", getTotalPot());
        
        // Determine show order; start with all-in players...
        List<Player> showingPlayers = new ArrayList<Player>();
        for (Pot pot : pots) {
            for (Player contributor : pot.getContributors()) {
                if (!showingPlayers.contains(contributor) && contributor.isAllIn()) {
                    showingPlayers.add(contributor);
                }
            }
        }
        // ...then last player to bet or raise (aggressor)...
        if (lastBettor != null) {
            if (!showingPlayers.contains(lastBettor)) {
                showingPlayers.add(lastBettor);
            }
        }
        //...and finally the remaining players, starting left of the button.
        int pos = (dealerPosition + 1) % activePlayers.size();
        while (showingPlayers.size() < activePlayers.size()) {
            Player player = activePlayers.get(pos);
            if (!showingPlayers.contains(player)) {
                showingPlayers.add(player);
            }
            pos = (pos + 1) % activePlayers.size();
        }
        
        // Players automatically show or fold in order.
        boolean firstToShow = true;
        int bestHandValue = -1;
        for (Player playerToShow : showingPlayers) {
            Hand hand = new Hand(board);
            hand.addCards(playerToShow.getCards());
            HandValue handValue = new HandValue(hand);
            boolean doShow = ALWAYS_CALL_SHOWDOWN;
            if (!doShow) {
                if (playerToShow.isAllIn()) {
                    // All-in players must always show.
                    doShow = true;
                    firstToShow = false;
                } else if (firstToShow) {
                    // First player must always show.
                    doShow = true;
                    bestHandValue = handValue.getValue();
                    firstToShow = false;
                } else {
                    // Remaining players only show when having a chance to win.
                    if (handValue.getValue() >= bestHandValue) {
                        doShow = true;
                        bestHandValue = handValue.getValue();
                    }
                }
            }
            if (doShow) {
                // Show hand.
                for (Player player : players) {
                    player.getClient().playerUpdated(playerToShow);
                }
                notifyMessage("%s has %s. [show]", playerToShow, handValue.getDescription());
            } else {
                // Fold.
                playerToShow.setCards(null);
                activePlayers.remove(playerToShow);
                for (Player player : players) {
                    if (player.equals(playerToShow)) {
                        player.getClient().playerUpdated(playerToShow);
                    } else {
                        // Hide secret information to other players.
                        player.getClient().playerUpdated(playerToShow.publicClone());
                    }
                }
                notifyMessage("%s folds. [show]", playerToShow);
            }
        }
        
        // Sort players by hand value (highest to lowest).
        Map<HandValue, List<Player>> rankedPlayers = new TreeMap<HandValue, List<Player>>();
        for (Player player : activePlayers) {
            // Create a hand with the community cards and the player's hole cards.
            Hand hand = new Hand(board);
            hand.addCards(player.getCards());
            // Store the player together with other players with the same hand value.
            HandValue handValue = new HandValue(hand);
//            System.out.format("[DEBUG] %s: %s\n", player, handValue);
            List<Player> playerList = rankedPlayers.get(handValue);
            if (playerList == null) {
                playerList = new ArrayList<Player>();
            }
            playerList.add(player);
            rankedPlayers.put(handValue, playerList);
        }

        // Per rank (single or multiple winners), calculate pot distribution.
        int totalPot = getTotalPot();
        Map<Player, Integer> potDivision = new HashMap<Player, Integer>();
        for (HandValue handValue : rankedPlayers.keySet()) {
            List<Player> winners = rankedPlayers.get(handValue);
            for (Pot pot : pots) {
                // Determine how many winners share this pot.
                int noOfWinnersInPot = 0;
                for (Player winner : winners) {
                    if (pot.hasContributer(winner)) {
                        noOfWinnersInPot++;
                    }
                }
                if (noOfWinnersInPot > 0) {
                    // Divide pot over winners.
                    int potShare = pot.getValue() / noOfWinnersInPot;
                    for (Player winner : winners) {
                        if (pot.hasContributer(winner)) {
                            Integer oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + potShare);
                            } else {
                                potDivision.put(winner, potShare);
                            }
                            
                        }
                    }
                    // Determine if we have any odd chips left in the pot.
                    int oddChips = pot.getValue() % noOfWinnersInPot;
                    if (oddChips > 0) {
                        // Divide odd chips over winners, starting left of the dealer.
                        pos = dealerPosition;
                        while (oddChips > 0) {
                            pos = (pos + 1) % activePlayers.size();
                            Player winner = activePlayers.get(pos);
                            Integer oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + 1);
//                                System.out.format("[DEBUG] %s receives an odd chip from the pot.\n", winner);
                                oddChips--;
                            }
                        }
                        
                    }
                    pot.clear();
                }
            }
        }
        
        //output
        outputBuilder.append("*** SHOW DOWN ***\r\n");
        for(int i = dealerPosition; i < players.size() + dealerPosition; i++) {
    		outputBuilder.append(players.get(i%players.size()).getName()).append(": shows [")
    			.append(Card.cardstoString(players.get(i%players.size()).getCards(), " "))
    			.append("]\r\n");
    	}
        
        // Divide winnings.
        StringBuilder winnerText = new StringBuilder();
        int totalWon = 0;
        for (Player winner : potDivision.keySet()) {
            int potShare = potDivision.get(winner);
            winner.win(potShare);
            totalWon += potShare;
            if (winnerText.length() > 0) {
                winnerText.append(", ");
            }
            winnerText.append(String.format("%s wins $ %d", winner, potShare));
            notifyPlayersUpdated(true);

        	//output
            outputBuilder.append(winner.getName()).append(" collected ")
            	.append(potShare).append(" from pot\r\n");
        }
        winnerText.append('.');
        notifyMessage(winnerText.toString());
        
        // Sanity check.
        if (totalWon != totalPot) {
            throw new IllegalStateException("Incorrect pot division!");
        }
        
        //output
        outputBuilder.append("*** SUMMARY ***\r\n")
    	.append("Total pot ").append(totalWon).append(" | Rake 0\r\n")
    	.append("Board [").append(Card.cardstoString(board, " ")).append("]\r\n");
    
        if (potDivision.keySet().size() == 1){
        	Player winner = potDivision.keySet().iterator().next();
        	int potShare = potDivision.get(winner);
	        for(int i = 0; i < players.size(); i++){
	        	outputBuilder.append("Seat ").append(i+1).append(": ").append(players.get(i).getName())
	    			.append(dealerPosition == i ? " (button) (small blind) " : " (big blind) ")
	    			.append(players.get(i) == winner ? "won (" + potShare + ")\r\n" : " mucked\r\n");
	        }
        }
        else {
	        for(int i = 0; i < players.size(); i++){
	        	int potShare = potDivision.get(players.get(i));
	        	outputBuilder.append("Seat ").append(i+1).append(": ").append(players.get(i).getName())
	    			.append(dealerPosition == i ? " (button) (small blind) " : " (big blind) ")
	    			.append("won (" + potShare + ")\r\n");
	        }
        }
        	
        outputBuilder.append("\r\n\r\n\r\n");

    }
    
    //Uncall bet in case of partial call  
    private void uncallBet() {
    	int total = 0;
    	Player p = null;
    	for (Iterator<Pot> iterator = pots.iterator(); iterator.hasNext();) {
    	    Pot pot = iterator.next();
            if (pot.getContributors().size() == 1) {
            	p = pot.getContributors().iterator().next();
            	int amount = pot.getBet();
            	total += amount;
            	
            	//Player needs to get this amount back from this pot
                p.setCash(p.getCash() + amount);
                p.setBet(p.getBet() - amount);

                //Update table info
                bet -= amount;
                iterator.remove();
            }
    	}
    	
    	//Output
    	if (p != null) {
	    	outputBuilder.append("Uncalled bet (").append(total)
	    	.append(") returned to ").append(p.getName()).append("\r\n");
    	}
    }
    
    /**
     * Notifies listeners with a custom game message.
     * 
     * @param message
     *            The formatted message.
     * @param args
     *            Any arguments.
     */
    private void notifyMessage(String message, Object... args) {
        message = String.format(message, args);
        for (Player player : players) {
            player.getClient().messageReceived(message);
        }
    }
    
    /**
     * Notifies clients that the board has been updated.
     */
    private void notifyBoardUpdated() {
        int pot = getTotalPot();
        for (Player player : players) {
            player.getClient().boardUpdated(board, bet, pot, ante);
        }
    }
    
    /**
     * Returns the total pot size.
     * 
     * @return The total pot size.
     */
    private int getTotalPot() {
        int totalPot = 0;
        for (Pot pot : pots) {
            totalPot += pot.getValue();
        }
        return totalPot;
    }

    /**
     * Notifies clients that one or more players have been updated. <br />
     * <br />
     * 
     * A player's secret information is only sent its own client; other clients
     * see only a player's public information.
     * 
     * @param showdown
     *            Whether we are at the showdown phase.
     */
    private void notifyPlayersUpdated(boolean showdown) {
        for (Player playerToNotify : players) {
            for (Player player : players) {
                if (!showdown && !player.equals(playerToNotify)) {
                    // Hide secret information to other players.
                    player = player.publicClone();
                }
                playerToNotify.getClient().playerUpdated(player);
            }
        }
    }
    
    /**
     * Notifies clients that a player has acted.
     */
    private void notifyPlayerActed() {
        for (Player p : players) {
            Player playerInfo = p.equals(actor) ? actor : actor.publicClone();
            p.getClient().playerActed(playerInfo);
        }
    }

	public int getDealerPosition() {
		return dealerPosition;
	}

	public void setDealerPosition(int dealerPosition) {
		this.dealerPosition = dealerPosition;
	}

	public Player getDealer() {
		return dealer;
	}

	public void setDealer(Player dealer) {
		this.dealer = dealer;
	}

	public int getActorPosition() {
		return actorPosition;
	}

	public void setActorPosition(int actorPosition) {
		this.actorPosition = actorPosition;
	}

	public Player getActor() {
		return actor;
	}

	public void setActor(Player actor) {
		this.actor = actor;
	}

	public int getMinBet() {
		return minBet;
	}

	public void setMinBet(int minBet) {
		this.minBet = minBet;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public Player getLastBettor() {
		return lastBettor;
	}

	public void setLastBettor(Player lastBettor) {
		this.lastBettor = lastBettor;
	}

	public int getRaises() {
		return raises;
	}

	public void setRaises(int raises) {
		this.raises = raises;
	}

	public long getHandNumber() {
		return handNumber;
	}

	public void setHandNumber(long handNumber) {
		this.handNumber = handNumber;
	}

	public TableType getTableType() {
		return tableType;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public List<Player> getActivePlayers() {
		return activePlayers;
	}

	public Deck getDeck() {
		return deck;
	}

	public List<Card> getBoard() {
		return board;
	}

	public List<Pot> getPots() {
		return pots;
	}

	public void setAnteInTimesOfBB(int anteInTimesOfBB) {
		this.anteInTimesOfBB = anteInTimesOfBB;
	}
    
}
