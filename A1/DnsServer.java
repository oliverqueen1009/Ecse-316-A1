import java.io.*;
import java.net.*;

class DnsServer {
    public static void main(String args[]) throws Exception {

    DatagramSocket serverSocket = new DatagramSocket(53);

    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];

    while (true) {
        System.out.println("Server started");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String sentence = new String(receivePacket.getData());

        InetAddress IPAddress = receivePacket.getAddress();

        int port = receivePacket.getPort();
        System.out.println("IP, Port: " + IPAddress + ", " + port);
        String capitalizedSentence = sentence.toUpperCase();
        sendData = capitalizedSentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        
        serverSocket.send(sendPacket);
        }
    }
}