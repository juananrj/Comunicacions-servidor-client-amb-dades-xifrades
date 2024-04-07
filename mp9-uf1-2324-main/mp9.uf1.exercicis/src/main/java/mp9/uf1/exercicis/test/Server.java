package mp9.uf1.exercicis.test;
import mp9.uf1.cryptoutils.MyCryptoUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Server {
    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Servidor esperando conexiones...");
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado");

            // Generar par de claves del server
            KeyPair serverKeyPair = MyCryptoUtils.randomGenerate(2048);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Enviar y recibir claves del cliente
            out.writeObject(serverKeyPair.getPublic());
            PublicKey clientPublicKey = (PublicKey) in.readObject();

            // Recibir mensajes
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] encryptedData = (byte[]) in.readObject();
                        byte[] decryptedData = MyCryptoUtils.decryptData(encryptedData, serverKeyPair.getPrivate());
                        System.out.println("Cliente: " + new String(decryptedData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Enviar mensajes
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Servidor: ");
                String msg = reader.readLine();
                byte[] encryptedData = MyCryptoUtils.encryptData(msg.getBytes(), clientPublicKey);
                out.writeObject(encryptedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
