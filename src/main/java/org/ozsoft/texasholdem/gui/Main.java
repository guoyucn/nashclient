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

package org.ozsoft.texasholdem.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;

import org.ozsoft.texasholdem.Card;
import org.ozsoft.texasholdem.Client;
import org.ozsoft.texasholdem.Player;
import org.ozsoft.texasholdem.Table;
import org.ozsoft.texasholdem.TableType;
import org.ozsoft.texasholdem.actions.Action;
import org.ozsoft.texasholdem.bots.BasicBot;
import org.ozsoft.texasholdem.util.InputOutputMgr;

/**
 * The game's main frame.
 * 
 * This is the core class of the Swing UI client application.
 * 
 * @author Oscar Stigter
 */
public class Main extends JFrame implements Client {
    
    /** Serial version UID. */
    private static final long serialVersionUID = -5414633931666096443L;
    
    /** Table type (betting structure). */
    private static final TableType TABLE_TYPE = TableType.NO_LIMIT;

    /** The size of the big blind. */
    private static final int BIG_BLIND = 20;
    
    /** The starting cash per player. */
    private static final int STARTING_CASH = 500;
    
    /** The table. */
    private final Table table;
    
    /** The players at the table. */
    private final Map<String, Player> players;
    
    /** The GridBagConstraints. */
    private final GridBagConstraints gc;
    
    /** The board panel. */
    private final BoardPanel boardPanel;
    
    /** The control panel. */
    private final ControlPanel controlPanel;
    
    /** The player panels. */
    private final Map<String, PlayerPanel> playerPanels;
    
    /** The human player. */
    private final Player humanPlayer;
    
    /** The current dealer's name. */
    private String dealerName; 

    /** The current actor's name. */
    private String actorName; 

    
    private Properties prop;
    /**
     * Constructor.
     * @throws IOException 
     */
    public Main() throws IOException {
        super("Texas Hold'em poker");
        File config = new File ("config/application.properties");
        InputStream stream = null;
        if (config.exists()){
        	stream = new FileInputStream(config);
        }else{
        	stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
        }
        //InputStream stream = Main.class.getResourceAsStream("application.properties");
    	prop = new Properties();
    	prop.load(stream);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.TABLE_COLOR);
        setLayout(new GridBagLayout());

        gc = new GridBagConstraints();

        table = new Table(TABLE_TYPE, BIG_BLIND);
        String anteInTimesOfBB = prop.getProperty("ante.in.times.of.bb", "10");
        table.setAnteInTimesOfBB(Integer.parseInt(anteInTimesOfBB));
        controlPanel = new ControlPanel(TABLE_TYPE, table);
        
        boardPanel = new BoardPanel(controlPanel);        
        addComponent(boardPanel, 1, 1, 1, 1);
        
        players = new LinkedHashMap<String, Player>();
        humanPlayer = new Player("Human", STARTING_CASH, this);
        players.put("Human", humanPlayer);
//        players.put(prop.getProperty("hero.name", "Hero"),    new Player(prop.getProperty("hero.name", "Hero"),   STARTING_CASH, new BasicBot(prop.getProperty("ws.url.hero"))));        
        players.put(prop.getProperty("villain.name", "Villain"),    new Player(prop.getProperty("villain.name", "Villain"),   STARTING_CASH, new BasicBot(prop.getProperty("ws.url.villain"))));
//        players.put("Mike",   new Player("Mike",  0, new BasicBot(25, 50)));
//        players.put("Eddie",  new Player("Eddie", 0, new BasicBot(50, 25)));

        for (Player player : players.values()) {
            table.addPlayer(player);
        }
        
        playerPanels = new HashMap<String, PlayerPanel>();
        int i = 0;
        for (Player player : players.values()) {
            PlayerPanel panel = new PlayerPanel();
            playerPanels.put(player.getName(), panel);
            switch (i++) {
                case 1:
                    // North position.
                    addComponent(panel, 1, 0, 1, 1);
                    break;
                case 2:
                    // East position.
                    addComponent(panel, 2, 1, 1, 1);
                    break;
                case 0:
                    // South position.
                    addComponent(panel, 1, 2, 1, 1);
                    break;
                case 3:
                    // West position.
                    addComponent(panel, 0, 1, 1, 1);
                    break;
                default:
                    // Do nothing.
            }
        }
        
        // Show the frame.
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
//        
//        controlPanel.add(new JButton("abc"));
//        controlPanel.add(new JButton("bcd"));
//        controlPanel.add(new JButton("xxx"));
//        controlPanel.add(new JButton("www"));
        
        // Start the game.
        table.run();
    }
    
    /**
     * The application's entry point.
     * 
     * @param args
     *            The command line arguments.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	if (!(args == null || args.length < 1)) {
    		try {
    			InputOutputMgr.INSTANCE.setInputType(InputOutputMgr.InputType.valueOf(args[0]));
    		} catch (Exception e) {
    			InputOutputMgr.INSTANCE.setInputType(InputOutputMgr.InputType.Random);
    		}
    	}
    	
        new Main();
    }

    @Override
    public void joinedTable(TableType type, int bigBlind, List<Player> players) {
        for (Player player : players) {
            PlayerPanel playerPanel = playerPanels.get(player.getName());
            if (playerPanel != null) {
                playerPanel.update(player);
            }
        }
    }

    @Override
    public void messageReceived(String message) {
        boardPanel.setMessage(message);
        if (message!= null && message.contains("[show]")){
        	boardPanel.waitForUserInput();
        }
    }

    @Override
    public void handStarted(Player dealer) {
        setDealer(false);
        dealerName = dealer.getName();
        setDealer(true);
    }

    @Override
    public void actorRotated(Player actor) {
        setActorInTurn(false);
        actorName = actor.getName();
        setActorInTurn(true);
    }

    @Override
    public void boardUpdated(List<Card> cards, int bet, int pot, int ante) {
        boardPanel.update(cards, bet, pot, ante);
    }

    @Override
    public void playerUpdated(Player player) {
        PlayerPanel playerPanel = playerPanels.get(player.getName());
        if (playerPanel != null) {
            playerPanel.update(player);
        }
    }

    @Override
    public void playerActed(Player player) {
        String name = player.getName();
        PlayerPanel playerPanel = playerPanels.get(name);
        if (playerPanel != null) {
            playerPanel.update(player);
            Action action = player.getAction();
            if (action != null) {
                boardPanel.setMessage(String.format("%s %s.", name, action.getVerb()));
                if (player.getClient() != this) {
//                    boardPanel.waitForUserInput();
                }
            }
        } else {
            throw new IllegalStateException(
                    String.format("No PlayerPanel found for player '%s'", name));
        }
    }

    @Override
    public Action act(int minBet, int currentBet, Set<Action> allowedActions, String command) {
    	return act(minBet, currentBet, allowedActions);
    }

    
    @Override
    public Action act(int minBet, int currentBet, Set<Action> allowedActions) {
        boardPanel.setMessage("Please select an action:");
        return controlPanel.getUserInput(minBet, humanPlayer.getCash(), allowedActions);
    }
    /**
     * Adds an UI component.
     * 
     * @param component
     *            The component.
     * @param x
     *            The column.
     * @param y
     *            The row.
     * @param width
     *            The number of columns to span.
     * @param height
     *            The number of rows to span.
     */
    private void addComponent(Component component, int x, int y, int width, int height) {
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.gridheight = height;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        getContentPane().add(component, gc);
    }

    /**
     * Sets whether the actor  is in turn.
     * 
     * @param isInTurn
     *            Whether the actor is in turn.
     */
    private void setActorInTurn(boolean isInTurn) {
        if (actorName != null) {
            PlayerPanel playerPanel = playerPanels.get(actorName);
            if (playerPanel != null) {
                playerPanel.setInTurn(isInTurn);
            }
        }
    }

    /**
     * Sets the dealer.
     * 
     * @param isDealer
     *            Whether the player is the dealer.
     */
    private void setDealer(boolean isDealer) {
        if (dealerName != null) {
            PlayerPanel playerPanel = playerPanels.get(dealerName);
            if (playerPanel != null) {
                playerPanel.setDealer(isDealer);
            }
        }
    }

}
