import java.io.*;
import java.net.*;

public class PINGClient
{
    final static int MAX_SIZE = 300; //constant max value for payload size

    public static String getHeader(String pingPacket) 
    {
        int index = pingPacket.indexOf("HOST:"); //index at 'HOST'
        return pingPacket.substring(0, index); //return everything before our index 
    }

    public static String getPayload(String pingPacket) 
    {
        int index = pingPacket.indexOf("HOST:"); //index at 'HOST'
        return pingPacket.substring(index); // return everything after our index
    }

    static String getRestString(int size)
    {
        //choose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
                                    "0123456789" + 
                                    "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(size); //create StringBuffer size of string
        for (int i = 0; i < size; i++) 
        {
            // generate a random number between 0 to string length to select character at random
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index)); // add Character one by one
        }
        return sb.toString();
    }

    public static void main(String args[]) throws IOException 
    {
        //initializing variables
        int numResponsePack = 1;
        long maxTime = 0;
        long minTime = 0;
        long sumTime = 0;
        int sumPayload = 0;
        
        // Receiving arguments from command and converting them from string to integers
        String hostName = args[0];
        String portS = (args[1]);
        int port = Integer.parseInt(portS);
        String clientS = (args[2]);
        int clientID = Integer.parseInt(clientS);
        String numPingS = (args[3]);
        int numPingPackets = Integer.parseInt(numPingS);
        String numWaitS = (args[4]);
        int numWaitSec = Integer.parseInt(numWaitS);
        try 
        {
            System.out.println("PINGClient started with server IP: " + hostName +
                               " port: " + port +
                               " client ID: " + clientID +
                               " packets: " + numPingPackets +
                               " wait: " + numWaitSec + "\n");
            InetAddress serverAddress = InetAddress.getByName(hostName); //getting ip address
            DatagramSocket socket = new DatagramSocket();//creating socket

            for (int i = 1; i <= numPingPackets; i++) 
            {
                byte[] sendData = new byte[MAX_SIZE]; //setting byte size to max size value
                int size = (int) (Math.random() * 151) + 150; // Random size between 150 and 300 bytes
                int restNum = (MAX_SIZE - size); //rest size number

                //Creating the ping packet string with the header and payload
                String header = "Version: " + 1 + "\n" +
                        "Client ID: " + clientID + "\n" +
                        "Sequence No.: " + i + "\n" +
                        "Time: " + System.currentTimeMillis() + "\n" +
                        "Payload Size: " + size;
                String payload = "Host: " + hostName + "\n" +
                        "Class-name: VCU-CMSC440-SPRING-2023\n" +
                        "User-name: Baez Salazar, Emil\n" +
                        "Rest: " + (PINGClient.getRestString(restNum));
                String pingPacket = header + payload; //combining header and payload to one string/packet

                System.out.println("---------- Ping Request Packet Header ----------");
                System.out.println(header); //print request header
                System.out.println("--------- Ping Request Packet Payload ------------");
                System.out.println(payload); //print request payload
                System.out.println("---------------------------------------\n");

                sendData = pingPacket.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                long startTime = System.nanoTime(); // Start time right before packet is sent
                socket.send(sendPacket); // Send the ping packet

                socket.setSoTimeout(numWaitSec * 1000); // Wait for response or timeout also converted from ms to s

                //constructing receive packet
                byte[] receiveData = new byte[MAX_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try 
                {
                    socket.receive(receivePacket); // receiving packet
                    String receivedPingPacket = new String(receivePacket.getData()); //store packet in string
                    String receivedHeader = getHeader(receivedPingPacket); //get header from received packet
                    String receivedPayload = getPayload(receivedPingPacket); //get payload from received packet

                    sumPayload += size; //incrementing the size to get total sum of payload size
                    
                    System.out.println("----------- Received Ping Response Packet Header ----------");
                    System.out.println(receivedHeader); //print received header
                    System.out.println("----------- Received Ping Response Packet Payload ----------");
                    System.out.println(receivedPayload); //print received payload
                    System.out.println("---------------------------------------");

                    long endTime = System.nanoTime(); //End time after packet received
                    long totalTimeNano = endTime - startTime; //Total time in nano seconds
                    if(i == 1) //to avoid a min time of 0
                    {   
                        minTime = totalTimeNano;
                    }
                    minTime = Math.min(minTime, totalTimeNano); // Minimum time in nano seconds
                    maxTime = Math.max(maxTime, totalTimeNano); // Max time in nano seconds
                    sumTime += totalTimeNano; // Sum of all the times in nano seconds
                    double totalTimeSec = (double) totalTimeNano / 1_000_000_000; // Convert Total time from nano seconds to seconds
                    System.out.println("RTT: " + (totalTimeSec) + " seconds\n");
                    numResponsePack++; //count number of response packets
                } 
                catch (SocketTimeoutException e) 
                {
                    System.out.println("--------------- Ping Response Packet Timed-Out ---------------\n");
                }
            }
            socket.close();
            // Convert min, max, and average time from nano seconds to seconds
            double minTimeSec = (double) minTime / 1_000_000_000;
            double maxTimeSec = (double) maxTime / 1_000_000_000;
            double averageTime = (double) sumTime / 1_000_000_000 / numResponsePack;
            double lossRate = ((double)numResponsePack / numPingPackets) * 100; //find loss rate
            int averagePayload = sumPayload / numResponsePack; 
            System.out.println("Summary: " + numPingPackets + " :: " + numResponsePack + 
                               " :: " + lossRate + "% :: " + minTimeSec + " :: " + maxTimeSec + 
                               " :: " + averageTime + " :: " + averagePayload);
        }
        catch (UnknownHostException e) 
        {
            System.out.println("ERR - arg 1");
        } 
        catch (NumberFormatException e) 
        {
            System.out.println("ERR - arg 3, 4, 5");
        } 
        catch (IOException e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
