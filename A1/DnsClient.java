import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;


class DnsClient {
    private static int timeout = 5;
    private static int max_retries = 3;
    private static int port = 53;
    private static String reqType = "A";
    private static String domainName;
    private static String server;
    

    public static void main(String args[]) throws Exception {

        //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        DatagramSocket clientSocket = new DatagramSocket();

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        try {
            //String[] teststr = {"–t", "10", "–r", "2", "–mx", "@8.8.8.8", "mcgill.ca"};
            parseCommandArgs(args);

            System.out.println("DnsClient sending request for " + domainName);
            System.out.println("Server: " + server);
            System.out.println("Request type: " + reqType);

            InetAddress IPAddress = InetAddress.getByName(server);

            //String sentence = inFromUser.readLine();
            //sendData = sentence.getBytes();

            // Prepare Question/Request packet to send
            byte[] qheader = constructQHeader();
            byte[] question = constructQuestion();
            ByteBuffer sendDataBuffer = ByteBuffer.allocate(qheader.length + question.length);
            sendDataBuffer.put(qheader);
            sendDataBuffer.put(question);
            sendData = sendDataBuffer.array();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);

            String modifiedSentence = new String(receivePacket.getData());

            System.out.println("FROM SERVER:" + modifiedSentence);
            clientSocket.close();
        } catch (Exception e) {
            System.err.println("ERROR\t" + e.getMessage());
        }
        
    }


    /**
     * Parse arguments for (optional) timeout, mad-retries, port, query type
     * and                 (required) server IP, domain name
     * @param args command-line arguments
     */
    private static void parseCommandArgs(String[] args) {
        System.out.println(Arrays.toString(args)); //debug

        if (args.length < 2 || args.length > 9) {
            throw new IllegalArgumentException("Incorrect input syntax. \nUsage: java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name");
        }

        for (int i=0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-t")) {
                timeout = Integer.parseInt(args[++i]);
            }
            else if (arg.equals("-r")) { 
                max_retries = Integer.parseInt(args[++i]);
            }
            else if (arg.equals("-p")) { 
                port = Integer.parseInt(args[++i]);
            }
            else if (arg.equals("-mx")) {
                reqType = "MX";
            }
            else if (arg.equals("-ns")) {
                reqType = "NS";
            }
            else if (arg.contains("@")) {
                server = arg.replaceFirst("@", "");
            }
            else {
                domainName = arg;
            }
        }

        if (domainName == null || server == null) {
            throw new IllegalArgumentException("Server IP address & domain name are required. \nUsage:java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name");
        }
    }


    /**
     * Construct Question (request) packet header
     * @return header as byte array
     */
    private static byte[] constructQHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(12); // 6 rows of 16 bits = 12 bytes
        
        Random random = new Random();
        short ID = (short)random.nextInt();

        String fields = "00000101"; // QR:0 query | OPCODE:0000 standard | AA:1 authoritative | TC:0 not truncated | RD:1 recursion
        fields += "00000000";       // RA:0 recursion | Z:000 reserved | RCODE: 0000 error code
        short fieldsShort = Short.parseShort(fields, 2);

        short QDCOUNT = 1; // # of entries in question
        short ANCOUNT = 0; // # of records in answer
        short NSCOUNT = 0; // # of records in autority
        short ARCOUNT = 0; // # of records in additional records

        // write data into ByteBuffer
        buffer.putShort(ID);
        buffer.putShort(fieldsShort);
        buffer.putShort(QDCOUNT);
        buffer.putShort(ANCOUNT);
        buffer.putShort(NSCOUNT);
        buffer.putShort(ARCOUNT);

        return buffer.array();
    }

    /** 
     * Construct Question
     * @return question as byte array
     */
    private static byte[] constructQuestion() {
        // QNAME
        String[] labels = domainName.split("\\."); // . is a special char

        int qname_size = domainName.length() + 2; // each element has size of 1 byte
        ByteBuffer buffer = ByteBuffer.allocate(qname_size + 4); // QNAME size + 2 rows of 16 bits (4 bytes)

        for (String label : labels) {
            System.out.println(label);  //debug

            int strlen = label.length();
            buffer.put((byte) strlen);  // label length

            byte[] bChars = label.getBytes();
            buffer.put(bChars); // char array in ascii representation

            System.out.println("bChars:" + Arrays.toString(bChars)); //debug
            System.out.println(" ");
        }
        buffer.put((byte) 0);   // final label with length 0

        short QTYPE = 1; //0x0001 (host address)
        switch (reqType) {
            case "MX":
                QTYPE = 2; //0x0002 (name server)
                break;
            case "NS":
                QTYPE = 15; //0x000f (mail server)
                break;
        }
        
        short QCLASS = 1; // Internet address

        buffer.putShort(QTYPE);
        buffer.putShort(QCLASS);

        return buffer.array();
    }
} 