package mp9.uf1.exercicis.test;
import mp9.uf1.cryptoutils.MyCryptoUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {
    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            Socket socket = new Socket("localhost", 12345);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Generar par de claves para el cliente
            KeyPair clientKeyPair = MyCryptoUtils.randomGenerate(2048);
            PublicKey clientPublicKey = clientKeyPair.getPublic();
            PrivateKey clientPrivateKey = clientKeyPair.getPrivate();

            //Enviar y recibir claves del cliente
            out.writeObject(clientPublicKey);
            PublicKey serverPublicKey = (PublicKey) in.readObject();

            //recibir mensajes del servidor
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] encryptedData = (byte[]) in.readObject();
                        byte[] decryptedData = MyCryptoUtils.decryptData(encryptedData, clientPrivateKey);
                        System.out.println("Servidor: " + new String(decryptedData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // enviar mensajes al servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Cliente: ");
                String msg = reader.readLine();
                byte[] encryptedData = MyCryptoUtils.encryptData(msg.getBytes(), serverPublicKey);
                out.writeObject(encryptedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
