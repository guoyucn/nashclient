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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.ozsoft.texasholdem.Table;
import org.ozsoft.texasholdem.TableType;
import org.ozsoft.texasholdem.actions.Action;
import org.ozsoft.texasholdem.actions.BetAction;
import org.ozsoft.texasholdem.actions.RaiseAction;

/**
 * Panel with buttons to let a human player select an action.
 * 
 * @author Oscar Stigter
 */
public class ControlPanel extends JPanel implements ActionListener {
    
    /** Serial version UID. */
    private static final long serialVersionUID = 4059653681621749416L;
    
    /** The table type (betting structure). */
    private final TableType tableType;

    /** The Check button. */
    private final JButton checkButton;
    
    /** The Call button. */
    private final JButton callButton;
    
    /** The Bet button. */
    private final JButton betButton;
    
    /** The Raise button. */
    private final JButton raiseButton;
    
    /** The Allin button. */
    private final JButton allinButton;

    /** The Fold button. */
    private final JButton foldButton;
    
    /** The Continue button. */
    private final JButton continueButton;
    
    /** The betting panel. */
    private final AmountPanel amountPanel;

    /** Monitor while waiting for user input. */
    private final Object monitor = new Object();
    
    /** The selected action. */
    private Action selectedAction;
    
    private AbstractAction keyAction;
    private Set<Action> allowedActions = null;
    
    private Table table;
    /**
     * Constructor.
     */
    public ControlPanel(TableType tableType, Table table) {
    	this.table = table;
        this.tableType = tableType;
        setBackground(UIConstants.TABLE_COLOR);
        keyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	if (ControlPanel.this.allowedActions == null)
            		return;
                String cmd = e.getActionCommand();
                if ("n".equals(cmd)) {
                    selectedAction = Action.CONTINUE;
                } else if ("k".equals(cmd)) {
                    selectedAction = Action.CHECK;
                } else if ("c".equals(cmd)) {
                    selectedAction = Action.CALL;
                } else if ("b".equals(cmd)) {
                    selectedAction = Action.BET;
                } else if ("r".equals(cmd)) {
                    selectedAction = Action.RAISE;
                } else if ("a".equals(cmd)) {
                    selectedAction = Action.ALL_IN;
                } else if ("f".equals(cmd)){
                    selectedAction = Action.FOLD;
                }else{
                	return;
                }
                if (!ControlPanel.this.allowedActions.contains(selectedAction)){
                	return;
                }
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        };
        continueButton = createActionButton(Action.CONTINUE, "N");
        checkButton = createActionButton(Action.CHECK, "K");
        callButton = createActionButton(Action.CALL, "C");
        betButton = createActionButton(Action.BET, "B");
        raiseButton = createActionButton(Action.RAISE, "R");
        allinButton = createActionButton(Action.ALL_IN, "A");
        foldButton = createActionButton(Action.FOLD, "F");
        amountPanel = new AmountPanel(table);
    }
    

	/**
     * Waits for the user to click the Continue button.
     */
    public void waitForUserInput() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                add(continueButton);
                validate();
                repaint();
            }
        });
        Set<Action> allowedActions = new HashSet<Action>();
        allowedActions.add(Action.CONTINUE);
        getUserInput(0, 0, allowedActions);
    }
    
    /**
     * Waits for the user to click an action button and returns the selected
     * action.
     * 
     * @param minBet
     *            The minimum bet.
     * @param cash
     *            The player's remaining cash.
     * @param allowedActions
     *            The allowed actions.
     * 
     * @return The selected action.
     */
    public Action getUserInput(int minBet, int cash, final Set<Action> allowedActions) {
    	this.allowedActions = allowedActions;
        selectedAction = null;
        while (selectedAction == null) {
            // Show the buttons for the allowed actions.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeAll();
                    if (allowedActions.contains(Action.CONTINUE)) {
                        add(continueButton);
                    } else {
                        if (allowedActions.contains(Action.CHECK)) {
                            add(checkButton);
                        }
                        if (allowedActions.contains(Action.CALL)) {
                            add(callButton);
                        }
                        if (allowedActions.contains(Action.BET)) {
                            add(betButton);
                        }
                        if (allowedActions.contains(Action.RAISE)) {
                            add(raiseButton);
                        }
                        if (allowedActions.contains(Action.ALL_IN)) {
                            add(allinButton);
                        }
                        if (allowedActions.contains(Action.FOLD)) {
                            add(foldButton);
                        }
                    }
                    validate();
                    repaint();
                }
            });
            
            // Wait for the user to select an action.
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
            ControlPanel.this.allowedActions = null;
            // In case of a bet or raise, show panel to select amount.
            if (tableType == TableType.NO_LIMIT && (selectedAction == Action.BET || selectedAction == Action.RAISE)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        removeAll();
                        add(amountPanel);
                        validate();
                        repaint();
                    }
                });

                if (selectedAction == Action.BET) {
                	selectedAction = amountPanel.show(selectedAction, minBet, cash, table.getBigBlind()/2);
                } else if (selectedAction == Action.RAISE) {
                	selectedAction = amountPanel.show(selectedAction, table.getBet() + minBet, table.getActor().getBet() + cash, table.getBigBlind()/2);
                }
                
                if (selectedAction == Action.BET) {
                    selectedAction = new BetAction(amountPanel.getAmount());
                } else if (selectedAction == Action.RAISE) {
                    selectedAction = new RaiseAction(amountPanel.getAmount() - table.getBet());
                } else {
                    // User cancelled.
                    selectedAction = null;
                }
            }
        }
        
        return selectedAction;
    }
    
    /*
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == continueButton) {
            selectedAction = Action.CONTINUE;
        } else if (source == checkButton) {
            selectedAction = Action.CHECK;
        } else if (source == callButton) {
            selectedAction = Action.CALL;
        } else if (source == betButton) {
            selectedAction = Action.BET;
        } else if (source == raiseButton) {
            selectedAction = Action.RAISE;
        } else if (source == allinButton) {
            selectedAction = Action.ALL_IN;
        } else {
            selectedAction = Action.FOLD;
        }
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }
    
    /**
     * Creates an action button.
     * 
     * @param action
     *            The action.
     * 
     * @return The button.
     */
    private JButton createActionButton(Action action, String stokeKey) {
        String label = action.getName();
        JButton button = new JButton(label);
        button.setMnemonic(label.charAt(0));
        button.setSize(100, 30);
        button.addActionListener(this);
        
        this.getInputMap().put(KeyStroke.getKeyStroke(stokeKey), "keyAction");
        this.getActionMap().put("keyAction", keyAction);
        return button;
    }
    

}
