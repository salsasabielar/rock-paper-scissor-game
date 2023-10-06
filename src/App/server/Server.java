
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package App.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static final String START_STATE = "start-game";
    public static final String RESULT_STATE = "result-game";
    public static final String SEND_RESULT_STATE = "final-result-game";
    public static final String READY_STATE = "ready-game";
    public static final String NOT_READY_STATE = "not-ready-game";
    public static final String JOIN_STATE = "join-game";
    public static final String EXIT_STATE = "exit-game";
    public static final String COUNTDOWN_GAME_STATE = "countdown-start-game";
    private List<_Thread> _tClient;
    private boolean run;
    private Config config = new Config();
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public Server() {
        _tClient = new ArrayList<>();
    }
    
    public void start(){
        run = true;
        try {
            ServerSocket server = new ServerSocket(config.getPort());
            System.out.println("Server sudah berjalan pada "+config.getIp()+":"+config.getPort());
            while(run){
                Socket socket = server.accept();
                if (!run) {
                    System.out.println("Server terhenti");
                    break;
                }
                _Thread t = new _Thread(socket);
                _tClient.add(t);
                t.start();
                System.out.println("Socket thread berhasi berjalan");
            }
           server.close();
            for (_Thread clientT : _tClient) {
                clientT.socket.close();
                clientT.input.close();
                clientT.output.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public synchronized void sendRequest(String message) {
        for (int i = 0; i < _tClient.size(); i++) {
            _Thread _tr = _tClient.get(i);
            if (!_tr.writeRequest(message)) {
                _tClient.remove(i);
            }
        }
    }
    public synchronized void disconnect(int id){
        for (int i = 0; i < _tClient.size(); i++) {
            _Thread _tr = _tClient.get(i);
            if (_tr.id == id) {
                _tClient.remove(i);
            }
        }
    }
    public List<_Thread> getClient() {
        return _tClient;
    }
    
    public class _Thread extends Thread{
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        public int id;

        public _Thread(Socket socket) {
            try {
                id = (int) (Math.random() * (100 - 900 + 1) + 100);
                this.socket = socket;
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public boolean writeRequest(String msg){
            if (!socket.isConnected()) {
                closeConnection();
                return false;
            }
            try {
                output.writeObject(msg);
            } catch (IOException ex) {
                Logger.getLogger(_Thread.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        public void closeConnection(){
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(_Thread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            while (true) {                
                try {
                    String res;
                    String req = input.readObject().toString();
                    System.out.println(req);
                    String username = req.split("~")[0];
                    String state = req.split("~")[1];
                    if (state.equals(JOIN_STATE) || state.equals(EXIT_STATE) || state.equals(READY_STATE) || state.equals(NOT_READY_STATE)) {
                        res = username + "~" + state + "~\n";
                        System.out.println(username+ " telah " + state);
                        sendRequest(res);
                    }else if(state.equals(RESULT_STATE)){
                        String result = req.split("~")[2];
                        res = username + "~" + state + "~" + result + "~\n";
                        System.out.println(username+ " telah " + state);
                        sendRequest(res);
                    }else if(state.equals(SEND_RESULT_STATE)){
                        String result = req.split("~")[2];
                        res = username + "~" + state + "~" + result + "~\n";
                        System.out.println(username+ " telah " + state);
                        sendRequest(res);
                    }else if(state.equals(COUNTDOWN_GAME_STATE)){
                        res = username + "~" + state + "~\n";
                        sendRequest(res);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
        }
    }
}
