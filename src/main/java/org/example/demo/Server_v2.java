package org.example.demo;
import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server_v2 {
    Map<String,String> userDatabase = new HashMap<>();
    Map<String,Boolean> userIsLogged = new HashMap<>();
    Map<String,Integer[]> userRecords = new HashMap<>();
    private static final int PORT = 12345;
    private static Queue<Player> Players = new LinkedList<>();

    private static ExecutorService clientHandlerPool = Executors.newCachedThreadPool(); // 用于处理客户端的线程池

    private static ExecutorService gameExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new Server_v2().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            userDatabase.put("1","1");
            userDatabase.put("2","2");
            System.out.println("Server is running...");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("> Client connected: " + clientSocket.getRemoteSocketAddress());
                    clientHandlerPool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            Player player = new Player(clientSocket);

            while (true){
            try {
                String request = (String) player.in.readObject();
                if ("REGISTER".equals(request)) {
                    String username = (String) player.in.readObject();
                    String password = (String) player.in.readObject();
                    if (!userDatabase.containsKey(username)) {
                        userDatabase.put(username, password);
                        player.out.writeObject("SUCCESS");
                        player.out.flush();
                    } else {
                        player.out.writeObject("FAIL");
                        player.out.flush();
                    }
                } else if ("LOGIN".equals(request)) {
                    String username = (String) player.in.readObject();
                    String password = (String) player.in.readObject();
                    if (password.equals(userDatabase.get(username))) {
                        player.out.writeObject("SUCCESS");
                        player.out.flush();
                        player.username = username;
                    } else {
                        player.out.writeObject("FAIL");
                        player.out.flush();
                    }
                    break;
                }
            } catch (SocketException e) {
                if (e.getMessage().contains("Broken pipe")) {
                    System.out.println("Detected broken pipe: ");
                }
            } catch (IOException e) {
                System.out.println("> Detected disconnected While login: " + clientSocket.getRemoteSocketAddress());
                return;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

            // 登陆成功，展示大厅页面
//            while (true){
                String require = (String) player.in.readObject();
                if (require.equals("suiji")) {
                    player.out.writeObject("SUCCESS");
                    player.out.flush();
                    suiji(player); // 调用匹配方法

//                    synchronized (player.lock) { // 确保对 lock 对象进行同步
//                        try {
//                            suiji(player); // 调用匹配方法
//                            player.lock.wait(); // 阻塞当前线程，等待唤醒
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt(); // 恢复中断标志
//                            System.err.println("Thread interrupted: " + e.getMessage());
//                        }
//                    }
                }


      //      }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkAndCleanDisconnectedSockets() {

        for (Player player : Players) {
            try {
                player.out.writeObject("ping");
                player.out.flush();
                System.out.println(666);
                String res = (String)player.in.readObject();
                System.out.println(666);
                System.out.println(player.socket.getRemoteSocketAddress()+res);
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("Error cleaning disconnected socket: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                // 发送心跳包
                player.out.writeObject("ping");
                player.out.flush();
                String res = (String)player.in.readObject();
                System.out.println(player.socket.getRemoteSocketAddress()+res);
            } catch (SocketException e) {
                if (e.getMessage().contains("Broken pipe")) {
                    System.out.println("Detected broken pipe: " + player.socket);
                }
                cleanUpDisconnectedSocket(player);
            } catch (IOException e) {
                System.out.println("Detected disconnected socket: " + player.socket);
                cleanUpDisconnectedSocket(player);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }


        }
    }

    // 清理断开连接的 socket 和相关资源
    private static void cleanUpDisconnectedSocket(Player player) {
        Players.remove(player);
        System.out.println("Removed disconnected socket and closed resources.");
    }

    private static void suiji(Player player) throws IOException {
        try {
            player.out.writeObject("waiting for another player...");

            synchronized (Players) {
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException ex) {}

                checkAndCleanDisconnectedSockets();

                Players.add(player);
                System.out.println(Players.size());
                if (Players.size() >= 2) {
                    Player player1 = Players.poll();
                    Player player2 = Players.poll();
                    gameExecutor.execute(new GameSession(player1, player2));
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void choose(){

    }
    private static void showUser(){

    }

}




class GameSession implements Runnable {
    private Player player1;
    private Player player2;
    private GameState gameState;
    private boolean isPlayer1Turn = true;
    private static final Logger logger = Logger.getLogger(GameSession.class.getName());

    public GameSession(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public void run() {
        try {
            initializeGame();
            startGameLoop();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Game session error: ", e.getMessage());
        } finally {
            closeConnections();
        }
    }


    private void initializeGame() throws IOException, ClassNotFoundException {

        System.out.println("sent initial information");

        player1.out.writeObject("You are player 1. Please choose the size of the game (example: 3 * 4):");
        player1.out.flush();
        player2.out.writeObject("You are player 2, waiting for player 1 to choose the size...");
        player2.out.flush();

        String[] size = ((String) player1.in.readObject()).split(" ");
        int rows = Integer.parseInt(size[0]);
        int cols = Integer.parseInt(size[2]);


        int[][] board = Game.SetupBoard(rows, cols);
        gameState = new GameState(board, 0, 0);
        while (!ifValid(gameState)){
            board = Game.SetupBoard(rows, cols);
            gameState = new GameState(board, 0, 0);
        }

        player1.out.writeObject(gameState);
        player1.out.flush();
        player2.out.writeObject(gameState);
        player2.out.flush();
        System.out.println("> Game initialized: " + rows + " x " + cols);
        printOutState(gameState);
    }

    private void startGameLoop() throws IOException {

        System.out.println("> Starting the game...");
        System.out.println();
        while (true) {

            int[] coordinates = new int[9];
            ObjectInputStream currentIn = isPlayer1Turn ? player1.in : player2.in;
            ObjectOutputStream currentOut = isPlayer1Turn ? player2.out : player1.out;

            try {
                System.out.println("> Waiting for " + (isPlayer1Turn ? "Player 1" : "Player 2") + "'s turn.");
                for (int i = 0; i < 9; i++) {
                    coordinates[i] = currentIn.readInt();
                }
                int flag = coordinates[0];
                System.out.println("< player's match: (" + coordinates[1]+","+coordinates[2]+"),("+coordinates[2*flag+1]+","+coordinates[2*flag+2] + ")");


                System.out.println("> update the game state...");
                if (coordinates[0] != 0) {
                    updateGameState(new int[]{coordinates[1], coordinates[2],coordinates[2*flag+1],coordinates[2*flag+2]});
                    if (isPlayer1Turn) {
                        gameState.setPlayer1Score(gameState.getPlayer1Score() + 1);
                    } else {
                        gameState.setPlayer2Score(gameState.getPlayer2Score() + 1);
                    }
                }
                System.out.println("> the new state is: ");
                printOutState(gameState);

                currentOut.writeObject(coordinates);
                currentOut.flush();
                boolean valid = ifValid(gameState);
                Thread.sleep(2000);
                if (valid) {
                    System.out.println("There are still unconnected pairs");
                    try {
                        player1.out.writeInt(0);
                        player1.out.flush();
                    } catch (EOFException e) {
                    handleDisconnection(isPlayer1Turn ? player1 : player2, "A player disconnected unexpectedly.");
                    break;
                } catch (SocketException e) {
                    logger.log(Level.SEVERE, "Socket errorss: ", e.getMessage());
                    OneplayerClosed();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "IO error: ", e.getMessage());
                }

                    try {
                        player2.out.writeInt(0);
                        player2.out.flush();
                    } catch (EOFException e) {
                        handleDisconnection(isPlayer1Turn ? player1 : player2, "A player disconnected unexpectedly.");
                        break;
                    } catch (SocketException e) {
                        logger.log(Level.SEVERE, "Socket errorss: ", e.getMessage());
                        OneplayerClosed();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "IO error: ", e.getMessage());
                    }

                }else {
                    Thread.sleep(1000);
                    System.out.println("Currently no unconnected pairing exists");
                    if (gameState.getPlayer1Score() > gameState.getPlayer2Score()) {
                        player1.out.writeInt(1);
                        player1.out.flush();
                        player2.out.writeInt(1);
                        player2.out.flush();
                    }else if (gameState.getPlayer1Score() < gameState.getPlayer2Score()){
                        player1.out.writeInt(2);
                        player1.out.flush();
                        player2.out.writeInt(2);
                        player2.out.flush();
                    }else {
                        player1.out.writeInt(3);
                        player1.out.flush();
                        player2.out.writeInt(3);
                        player2.out.flush();
                    }
//                    synchronized (player1.lock) {
//                        player1.lock.notify();
//                    }
//                    synchronized (player2.lock) {
//                        player2.lock.notify();
//                    }
                    return;
                }
                System.out.println("> Turn completed. Next player's turn.");
                System.out.println();
                isPlayer1Turn = !isPlayer1Turn;
            } catch (EOFException e) {
                handleDisconnection(isPlayer1Turn ? player1 : player2, "A player disconnected unexpectedly.");
                break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (SocketException e) {
                logger.log(Level.SEVERE, "Socket errorss: ", e.getMessage());
                OneplayerClosed();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IO error: ", e.getMessage());
            }
        }
    }

    private void updateGameState(int[] coordinates) {
        int x1 = coordinates[0], y1 = coordinates[1];
        int x2 = coordinates[2], y2 = coordinates[3];
        gameState.getBoard()[x1][y1] = 0;
        gameState.getBoard()[x2][y2] = 0;
    }

    private void handleDisconnection(Player player, String message) {
        try {
            System.out.println(message);
            ObjectOutputStream remainingPlayerOut = (player == player1) ? player2.out : player1.out;
            remainingPlayerOut.writeObject("Opponent disconnected. Ending game session.");
            remainingPlayerOut.flush();
            logger.log(Level.INFO, "Opponent disconnected. Notified the remaining player.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error notifying remaining player: ", e.getMessage());
        }
        finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (player1 != null) player1.close();
            if (player2 != null) player2.close();
            System.out.println("Connections closed for the session.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing connections: ", e);
        }
    }

    private void OneplayerClosed()  {
        try {
            player1.out.writeObject("your opponent has left the game!");
            player1.out.flush();
            player2.out.writeObject("your opponent has left the game!");
            player2.out.flush();
        }catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing connections: ", e.getMessage());
        }
    }

    private void printOutState(GameState gameState){
        int[][] board = gameState.getBoard();
        int row = board.length;
        int col = board[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(gameState.getBoard()[i][j] + " ");
            }
            System.out.println();
        }
    }

    //  判断是否还有可行配对
    private boolean ifValid(GameState gameState){
        int[][] board = gameState.getBoard();
        Game game = new Game(board);
        int row = board.length;
        int col = board[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < row; k++) {
                    for (int l = 0; l < row; l++) {
                        if (game.judge(i, j, k, l) && board[i][j] != 0 && board[k][l] != 0){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
