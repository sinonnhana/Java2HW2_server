package org.example.demo;
import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;
    private GameState gameState;
    private boolean isPlayer1Turn = true;

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {
        try  {
            System.out.println("Server is running...");
            ServerSocket serverSocket = new ServerSocket(PORT);

            try {
                Socket player1Socket = serverSocket.accept();
                Socket player2Socket = serverSocket.accept();
                ObjectOutputStream out1 = new ObjectOutputStream(player1Socket.getOutputStream());
                ObjectInputStream in1 = new ObjectInputStream(player1Socket.getInputStream());
                ObjectOutputStream out2 = new ObjectOutputStream(player2Socket.getOutputStream());
                ObjectInputStream in2 = new ObjectInputStream(player2Socket.getInputStream());
                System.out.println(">Both players connected.");

                out1.writeObject("You are player 1. please choose the size of game (example: 3 * 4):");
                out1.flush();
                out2.writeObject("You are player 2");
                out2.flush();

                System.out.println(">Waiting for player 1 to send size...");
                String[] size = ((String) in1.readObject()).split(" ");

                int rows = Integer.parseInt(size[0]);
                int cols = Integer.parseInt(size[2]);
                System.out.println(">The size is " + rows + " x " + cols + ", and the gamestate showed below:");

                // 初始化棋盘
                int[][] board = Game.SetupBoard(rows, cols); // 棋盘
                gameState = new GameState(board, 0, 0);

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        System.out.print(board[i][j] + " ");
                    }
                    System.out.println();
                }

                System.out.println(">send initial state to players");
                // 将初始状态发送给两个客户端
                out1.writeObject(gameState);
                out1.flush();
                out2.writeObject(gameState);
                out2.flush();
                System.out.println(">initial successdully");


                System.out.println(">game start--------------game start");
                System.out.println();
                while (true) {
                    int[] coordinates = new int[4];

                    try {
                        if (isPlayer1Turn) {
                            System.out.println(">waiting for player 1's match");
                            for (int i = 0; i < 4; i++) {
                                coordinates[i] = in1.readInt(); // 可能抛出EOFException
                            }
                            System.out.println(">player 1's match: (" + coordinates[0]+","+coordinates[1]+"),("+coordinates[2]+","+coordinates[3] + ")");
                        } else {
                            System.out.println(">waiting for player 2's match");
                            for (int i = 0; i < 4; i++) {
                                coordinates[i] = in2.readInt(); // 可能抛出EOFException
                                System.out.println(">player 2's match: (" + coordinates[0]+","+coordinates[1]+"),("+coordinates[2]+","+coordinates[3] + ")");
                            }
                        }
                    } catch (EOFException e) {
                        System.out.println("One player disconnected unexpectedly.");
                        e.printStackTrace();
                        break; // 退出循环，结束服务器进程或清理资源
                    } catch (IOException e) {
                        System.out.println("An I/O error occurred.");
                        e.printStackTrace();
                    }


                    // 更新游戏状态
                    System.out.println(">update the gamestage...");
                    if (coordinates[0] != -1) {
                        updateGameState(coordinates);
                        if (isPlayer1Turn) {
                            gameState.setPlayer1Score(gameState.getPlayer1Score() + 1);
                        } else {
                            gameState.setPlayer2Score(gameState.getPlayer2Score() + 1);
                        }
                    }
                    System.out.println("updated the gamestage ok, show the current state:");
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            System.out.print(gameState.getBoard()[i][j] + " ");
                        }
                        System.out.println();
                    }


                    System.out.println("tell the player");
                    if (isPlayer1Turn){
                        out2.writeObject(coordinates);
                        out2.flush();
                    }else {
                        out1.writeObject(coordinates);
                        out1.flush();
                    }
                    System.out.println("tell ok, another player's turn");

                    System.out.println();
                    // 切换到另一个玩家
                    isPlayer1Turn = !isPlayer1Turn;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateGameState(int[] coordinates) {
        int x1 = coordinates[0], y1 = coordinates[1];
        int x2 = coordinates[2], y2 = coordinates[3];
        gameState.getBoard()[x1][y1] = 0;
        gameState.getBoard()[x2][y2] = 0;
    }
}
