/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package App;

import App.server.Config;
import static App.server.Server.COUNTDOWN_GAME_STATE;
import static App.server.Server.EXIT_STATE;
import static App.server.Server.JOIN_STATE;
import static App.server.Server.READY_STATE;
import static App.server.Server.RESULT_STATE;
import static App.server.Server.SEND_RESULT_STATE;
import static App.server.Server.NOT_READY_STATE;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;

public class Player1 extends javax.swing.JFrame {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Config config = new Config();

    /**
     * Creates new form Player1
     */
    public Player1() {
        initComponents();
        start();
    }

    private boolean start() {
        try {
            socket = new Socket("localhost", config.getPort());
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
        new Player1.ClientListenner().start();
        return true;
    }

    private void stopClient() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void catchOpponent(String username, String state) {
        if (!username.equals(myUsername.getText())) {
            if (state.equalsIgnoreCase(JOIN_STATE)) {
                oponentName.setText(username);
            } else if (state.equalsIgnoreCase(EXIT_STATE)) {
                oponentName.setText("");
            } else if (state.equalsIgnoreCase(READY_STATE)) {
                readyOpponent.setText("Ready");
            } else if (state.equalsIgnoreCase(NOT_READY_STATE)) {
                readyOpponent.setText("belum");
            }
        }

    }

    public class ClientListenner extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    String res;
                    String req = (String) input.readObject();
                    System.out.println(req);
                    String username = req.split("~")[0];
                    String state = req.split("~")[1];
                    if (state.equals(JOIN_STATE) || state.equals(EXIT_STATE) || state.equals(READY_STATE) || state.equals(NOT_READY_STATE)) {
                        catchOpponent(username, state);
                    } else if (state.equals(RESULT_STATE)) {
                        if (!username.equals(myUsername.getText())) {
                            String result = req.split("~")[2];
                            oponentChoice.setText(result);
                        }
                    } else if (state.equals(SEND_RESULT_STATE)) {
                        if (!username.equals(myUsername.getText())) {
                            String result = req.split("~")[2];
                            myResult.setText(result);
                        }
                    } else if (state.equals(COUNTDOWN_GAME_STATE)) {
                        if (!username.equals(myUsername.getText())) {
                            countdownAction();
                        }

                    }

                } catch (IOException ex) {
                    Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
        }
    }

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

    int countdown = 10;

    private void countdownAction() {
        countdown = 10;
        myResult.setText("-");
        String myReadys = this.myReady.getText();
        String opponentReady = readyOpponent.getText();
        if (myReadys.equalsIgnoreCase("ready") && opponentReady.equalsIgnoreCase("ready")) {
            Timer t = new Timer();
            TimerTask tk = new TimerTask() {
                @Override
                public void run() {
                    countdown = countdown - 1;
                    countdownLabel.setText(String.valueOf(countdown));
                    if (countdown == 1) {
                        cancel();
                        t.cancel();
                        t.purge();
                        setWinner();
                    }
                    if (countdown <= 2) {
                        try {
                            String choice = getSelectedButtonText(choiceGroup);
                            String username = myUsername.getText();
                            String req = username + "~" + RESULT_STATE + "~" + choice;
                            output.writeObject(req);
                            myChoice.setText(choice);
                        } catch (IOException ex) {
                            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
            t.schedule(tk, 0, 1000);
        } else {
            JOptionPane.showMessageDialog(this, "Anda dan musuh belum siap untuk bermain");
        }
    }

    private void setWinner() {

        try {
            String me = myChoice.getText();
            String opponent = oponentChoice.getText();
            String results = "";
            String Myresults = "";
            if (me.equalsIgnoreCase("batu")) {
                if (opponent.equalsIgnoreCase("batu")) {
                    results = "Imbang";
                    Myresults = "Imbang";
                } else if (opponent.equalsIgnoreCase("gunting")) {
                    results = "Menang";
                    Myresults = "Kalah";
                } else if (opponent.equalsIgnoreCase("kertas")) {
                    results = "Kalah";
                    Myresults = "Menang";
                }
            } else if (me.equalsIgnoreCase("gunting")) {
                if (opponent.equalsIgnoreCase("batu")) {
                    results = "Kalah";
                    Myresults = "Menang";
                } else if (opponent.equalsIgnoreCase("gunting")) {
                    results = "Imbang";
                    Myresults = "Imbang";
                } else if (opponent.equalsIgnoreCase("kertas")) {
                    results = "Menang";
                    Myresults = "Kalah";
                }
            } else if (me.equalsIgnoreCase("kertas")) {
                if (opponent.equalsIgnoreCase("batu")) {
                    results = "Menang";
                    Myresults = "Kalah";
                } else if (opponent.equalsIgnoreCase("gunting")) {
                    results = "Kalah";
                    Myresults = "Menang";
                } else if (opponent.equalsIgnoreCase("kertas")) {
                    results = "Imbang";
                    Myresults = "Imbang";
                }
            }

            String username = myUsername.getText();
            String req = username + "~" + SEND_RESULT_STATE + "~" + Myresults;
            output.writeObject(req);
            myResult.setText(results);
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        choiceGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        joinBtn = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        countDownBtn = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        countdownLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        batuAnswer = new javax.swing.JRadioButton();
        kertasAnswer = new javax.swing.JRadioButton();
        guntingAnswer = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        myResult = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        oponentName = new javax.swing.JLabel();
        myUsername = new javax.swing.JLabel();
        myChoice = new javax.swing.JLabel();
        oponentChoice = new javax.swing.JLabel();
        exitBtn = new javax.swing.JButton();
        readyBtn = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        myReady = new javax.swing.JLabel();
        readyOpponent = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Game Batu Kertas Gunting");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Username");

        joinBtn.setText("Join");
        joinBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinBtnActionPerformed(evt);
            }
        });

        countDownBtn.setText("Start");
        countDownBtn.setEnabled(false);
        countDownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countDownBtnActionPerformed(evt);
            }
        });

        jLabel4.setText("Countdown : ");

        countdownLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        countdownLabel.setText("0");

        jLabel3.setText("Pilihan Anda:");

        choiceGroup.add(batuAnswer);
        batuAnswer.setText("Batu");

        choiceGroup.add(kertasAnswer);
        kertasAnswer.setText("Kertas");

        choiceGroup.add(guntingAnswer);
        guntingAnswer.setText("Gunting");

        jLabel5.setText("Nama Musuh :");

        jLabel6.setText("Pilihan Musuh :");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        myResult.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        myResult.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        myResult.setText("-");

        jLabel8.setText("hasil");

        jLabel9.setText("Nama Anda :");

        jLabel10.setText("Pilihan Anda :");

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        myChoice.setText("null");

        oponentChoice.setText("null");

        exitBtn.setText("Keluar");
        exitBtn.setEnabled(false);
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });

        readyBtn.setText("Ready");
        readyBtn.setEnabled(false);
        readyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyBtnActionPerformed(evt);
            }
        });

        jLabel7.setText("Ready: ");

        myReady.setText("Belum");

        readyOpponent.setText("Belum");

        jLabel13.setText("Ready: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(172, 172, 172)
                                .addComponent(jLabel1)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(joinBtn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(exitBtn))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(readyBtn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(countDownBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(countdownLabel)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel13)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(readyOpponent))
                                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(oponentChoice)
                                    .addComponent(oponentName))
                                .addGap(65, 65, 65))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(kertasAnswer)
                                            .addComponent(guntingAnswer)
                                            .addComponent(batuAnswer))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel3)
                                        .addGap(48, 48, 48)))
                                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(153, 153, 153)
                                        .addComponent(myResult, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(66, 66, 66)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel8)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel9)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(myUsername))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel10)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(myChoice))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel7)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(myReady)))
                                                .addGap(39, 39, 39)
                                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(163, 163, 163)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(joinBtn)
                    .addComponent(exitBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countDownBtn)
                    .addComponent(jLabel4)
                    .addComponent(countdownLabel)
                    .addComponent(readyBtn))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(oponentName)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(readyOpponent))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(oponentChoice)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(batuAnswer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(kertasAnswer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(guntingAnswer))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(myUsername))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(myReady))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(myChoice)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(myResult)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void joinBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinBtnActionPerformed
        try {
            // TODO add your handling code here:
            String username = usernameField.getText();
            String req = username + "~" + JOIN_STATE;
            output.writeObject(req);
            myUsername.setText(username);
            usernameField.setEditable(false);
            readyBtn.setEnabled(true);
            countDownBtn.setEnabled(true);
            exitBtn.setEnabled(true);
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_joinBtnActionPerformed

    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
        try {
            // TODO add your handling code here:
            String username = usernameField.getText();
            String req = username + "~" + EXIT_STATE;
            output.writeObject(req);
            myUsername.setText("");
            usernameField.setEditable(true);
            readyBtn.setEnabled(false);
            countDownBtn.setEnabled(false);
            exitBtn.setEnabled(!true);
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_exitBtnActionPerformed

    private void readyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyBtnActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            if (!myUsername.getText().equalsIgnoreCase("")) {
                String username = usernameField.getText();
                if (readyBtn.getText().equalsIgnoreCase("Ready")) {
                    String req = username + "~" + READY_STATE;
                    output.writeObject(req);
                    myReady.setText("Ready");
                    readyBtn.setText("Cancel");
                } else if (readyBtn.getText().equalsIgnoreCase("cancel")) {
                    String req = username + "~" + NOT_READY_STATE;
                    output.writeObject(req);
                    System.out.println(req);
                    myReady.setText("belum");
                    readyBtn.setText("Ready");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Anda belum join ke server");
            }
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_readyBtnActionPerformed

    private void countDownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countDownBtnActionPerformed
        // TODO add your handling code here:
        try {
            String username = myUsername.getText();
            String req = username + "~" + COUNTDOWN_GAME_STATE;
            output.writeObject(req);
            countdownAction();
        } catch (IOException ex) {
            Logger.getLogger(Player1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_countDownBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Player1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Player1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Player1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Player1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Player1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton batuAnswer;
    private javax.swing.ButtonGroup choiceGroup;
    private javax.swing.JButton countDownBtn;
    private javax.swing.JLabel countdownLabel;
    private javax.swing.JButton exitBtn;
    private javax.swing.JRadioButton guntingAnswer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JButton joinBtn;
    private javax.swing.JRadioButton kertasAnswer;
    private javax.swing.JLabel myChoice;
    private javax.swing.JLabel myReady;
    private javax.swing.JLabel myResult;
    private javax.swing.JLabel myUsername;
    private javax.swing.JLabel oponentChoice;
    private javax.swing.JLabel oponentName;
    private javax.swing.JButton readyBtn;
    private javax.swing.JLabel readyOpponent;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
