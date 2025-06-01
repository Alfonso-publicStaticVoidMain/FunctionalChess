package controller.online;

import configparams.ConfigParameters;
import controller.ChessController;
import controller.IndexController;
import functional_chess_model.Position;
import graphic_resources.EmergentPanels;
import view.ChessGUI;
import view.online.ConnectionLogger;

import javax.swing.SwingUtilities;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class NetworkController implements MoveListener, WindowListener, ActionListener {
    private final ChessController chessController;
    private final ChessGUI chessView;
    private final ConnectionLogger logger;
    private ServerSocket serverSocket;
    private Connection connection;

    public NetworkController(ChessController controller, ConnectionLogger logger) {
        this.chessController = controller;
        this.chessController.addMoveListener(this);
        this.chessView = controller.getView();
        this.chessView.getBackButton().addActionListener(this);
        this.logger = logger;
        this.logger.addWindowListener(this);
    }

    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(ConfigParameters.SERVER_PORT);
                String password = UUID.randomUUID().toString().substring(0, 6);
                String hostAddress = InetAddress.getLocalHost().getHostAddress();

                logger.log("Hosting game. Share password: " + password);
                logger.log("IP address: " + hostAddress);
                logger.log("Waiting for client to connect...");

                Socket clientSocket = serverSocket.accept();
                Connection tempConnection = new Connection(clientSocket);

                String clientVariant = tempConnection.getIn().readLine();

                if (clientVariant.equals(chessController.getGame().variant().toString())) {
                    logger.log("Game types coincide. Proceeding");
                    tempConnection.getOut().println(ConfigParameters.NETWORK_ACCEPTED);
                } else {
                    logger.log("Different selected games. Client chose "+ clientVariant + ". Connection rejected.");
                    tempConnection.getOut().println(ConfigParameters.NETWORK_REJECTED);
                    tempConnection.close();
                    return;
                }

                logger.log("Client attempting to connect from " + clientSocket.getInetAddress());
                String clientPassword = tempConnection.getIn().readLine();

                if (password.equals(clientPassword)) {
                    logger.log("Password accepted. Starting game.");
                    tempConnection.getOut().println(ConfigParameters.NETWORK_ACCEPTED);
                    this.connection = tempConnection;
                    listenForMoves();
                } else {
                    logger.log("Incorrect password. Connection rejected.");
                    tempConnection.getOut().println(ConfigParameters.NETWORK_REJECTED);
                    tempConnection.close();
                }

            } catch (IOException e) {
                logger.log("Error starting server: " + e.getMessage());
            } finally {
                logger.waitAndClose();
            }
        }).start();
    }

    public void startClient() {
        new Thread(() -> {
            try {
                String hostAddress = EmergentPanels.userTextInputMessage(logger, "Introduce the IP of the host");
                logger.log("Connecting to server at " + hostAddress + ":" + ConfigParameters.SERVER_PORT + "...");

                Socket clientSocket = new Socket(hostAddress, ConfigParameters.SERVER_PORT);
                Connection tempConnection = new Connection(clientSocket);

                tempConnection.getOut().println(chessController.getGame().variant().toString());

                String response = tempConnection.getIn().readLine();

                if (response.equals(ConfigParameters.NETWORK_REJECTED)) {
                    logger.log("Different selected games. Server rejected connection.");
                    tempConnection.close();
                    return;
                }

                String userPassword = EmergentPanels.userTextInputMessage(logger, "Introduce the password");
                tempConnection.getOut().println(userPassword);

                response = tempConnection.getIn().readLine();

                if (response.equals(ConfigParameters.NETWORK_REJECTED)) {
                    logger.log("Wrong password. Server rejected connection.");
                    tempConnection.close();
                } else if (response.equals(ConfigParameters.NETWORK_ACCEPTED)) {
                    logger.log("Connection accepted. Game is starting...");
                    this.connection = tempConnection;
                    listenForMoves();
                } else {
                    logger.log("Unexpected server response.");
                    tempConnection.close();
                }

            } catch (IOException e) {
                logger.log("Failed to connect: " + e.getMessage());
            } finally {
                logger.waitAndClose(); // still closes the log window after delay
            }
        }).start();
    }

    protected void listenForMoves() {
        Thread listener = new Thread(() -> {
            String line;
            try {
                while ((line = connection.getIn().readLine()) != null) {
                    String[] parts = line.split(":");
                    int x1 = Integer.parseInt(parts[0].trim());
                    int y1 = Integer.parseInt(parts[1].trim());
                    int x2 = Integer.parseInt(parts[2].trim());
                    int y2 = Integer.parseInt(parts[3].trim());
                    String crowningType = parts[4].equals("null") ? null : parts[4].trim();

                    Position initPos = Position.of(x1, y1);
                    Position finPos = Position.of(x2, y2);

                    if (crowningType == null) System.out.println("[NETWORK DEBUG] Move received: "+ initPos +" ("+x1+", "+y1+") to "+ finPos +" ("+x2+", "+y2+")");
                    else System.out.println("[NETWORK DEBUG] Move with crowning received: "+ initPos +" ("+x1+", "+y1+") to "+ finPos +" ("+x2+", "+y2+") into"+crowningType);

                    SwingUtilities.invokeLater(() -> {
                        chessController.handleClick(x1, y1, false);
                        chessController.handleClick(x2, y2, false, crowningType);
                    });
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void disconnect() {
        if (connection != null) {
            connection.close();
            logger.log("Connection closed.");
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
                logger.log("Server socket closed.");
            } catch (IOException e) {
                logger.log("Error closing server socket: " + e.getMessage());
            }
        }
    }


    @Override
    public void onMovePerformed(Position initPos, Position finPos, String crowningType) {
        if (crowningType == null) System.out.println("[NETWORK DEBUG] Move sent: ("+initPos.x()+", "+initPos.y()+") to ("+finPos.x()+", "+finPos.y()+")");
        else System.out.println("[NETWORK DEBUG] Crowning sent: ("+initPos.x()+", "+initPos.y()+") to ("+finPos.x()+", "+finPos.y()+")"+ " into "+crowningType);
        connection.getOut().println(initPos.x() + ":" + initPos.y() + ":" + finPos.x() + ":" + finPos.y() + ":" + (crowningType == null ? "null" : crowningType));
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        logger.waitAndClose();
        chessView.dispose();
        disconnect();
        new IndexController();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[NETWORK DEBUG] NetworkController action received: "+command);
        if (command.equals(ConfigParameters.BACK_BUTTON)) {
            logger.log("Disconnecting...");
            logger.waitAndClose(1000);
            disconnect();
        }
    }
}
