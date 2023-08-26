import java.io.*;
import java.net.*;

public class PINGServer 
{
    final static int MAX_SIZE = 300; //constant max value for payload size
    
    public static String[] getVariables(String packet) //Function to get the variables in our packet
    {
        String[] lines = packet.split("\n"); // Split the packet by lines
        String[] values = new String[lines.length]; // Create an array to store the values
        for (int i = 0; i < lines.length; i++) 
        {
            String line = lines[i].trim(); // Trim spaces
            if (line.contains(":")) // Check if the line contains a colon
            { 
                String[] variable = line.split(":"); // Split by the colon to get variable
                if (variable.length > 1) // make sure there's a variable there
                { 
                    values[i] = variable[1].trim(); // Get the variable after the colon and store in the array
                }
            }
        }
        return values;
    }
    
    public static String getHeader(String pingPacket) 
    {
        int index = pingPacket.indexOf("Host:"); //index at 'Host'
        return pingPacket.substring(0, index); //return everything before our index 
    }

    public static String getPayload(String pingPacket) 
    {
        int index = pingPacket.indexOf("Host:"); //index at 'Host'
        return pingPacket.substring(index); // return everything after our index
    }

    public static void main(String[] args) 
    {
        //convert arguments from string to integers
        String portString = (args[0]);
        int port = Integer.parseInt(portString);
        String lossString = (args[1]);
        int loss = Integer.parseInt(lossString);

        if (port < 1 || port > 65535) //make sure port is between 1 and 65535 inclusive
        {
            System.err.println("ERR - arg 1");
            System.exit(1);
        }
        else if(loss < 0 || loss > 100) //make sure loss is between 0 and 100 inclusive
        {
            System.err.println("ERR - arg 2");
            System.exit(1);
        }

        try(DatagramSocket socket = new DatagramSocket(port))
        { 
            InetAddress ipAddress = InetAddress.getLocalHost(); //Get our ip address
            System.out.println("PINGServer started with server IP: " + ipAddress.getHostAddress() + ", port: " + port + " ...\n");
            while (true)
            {
                //construct ping receive packet
                byte[] receiveData = new byte[MAX_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket); //Receive ping packet

                String receivedPingPacket = new String(receivePacket.getData()); //store our packet into a string
                InetAddress clientAddress = receivePacket.getAddress(); //get client ip address
                int clientPort = receivePacket.getPort(); //get client port address
                String[] values = getVariables(receivedPingPacket); //get variables to store in array and access later
                
                //random integer between 1 and 100
                int rand = (int) (Math.random() * 100) + 1;

                if (rand <= loss) //Check if packet will be dropped
                {
                    //dropped packet information
                    System.out.println("IP:" + clientAddress.getHostAddress() + 
                                       " :: Port:" + port + 
                                       " :: ClientID:" + values[2] + 
                                       " :: Seq#:" + values[3] + 
                                       " :: DROPPED");
                    
                    //get header and payload information from packet
                    String header = getHeader(receivedPingPacket);
                    String payload = getPayload(receivedPingPacket);

                    //Print the response packet that was received
                    System.out.println("----------Received Ping Request Packet Header----------");
                    System.out.println(header); //Print the received ping packet header                   
                    System.out.println("---------Received Ping Request Packet Payload------------");
                    System.out.println(payload); //Print the received ping packet payload
                    System.out.println("---------------------------------------\n");
                } 
                else
                {
                    //received packet information
                    System.out.println("IP:" + clientAddress.getHostAddress() + 
                                       " :: Port:" + port + 
                                       " :: ClientID:" + values[2] + 
                                       " :: Seq#:" + values[3] + 
                                       " :: RECEIVED");
                    
                    //get header and payload information from packet
                    String header = getHeader(receivedPingPacket);
                    String payload = getPayload(receivedPingPacket);

                    //Print the response packet that was received
                    System.out.println("----------Received Ping Request Packet Header----------");
                    System.out.println(header); //Print the received ping packet header                   
                    System.out.println("---------Received Ping Request Packet Payload------------");
                    System.out.println(payload); //Print the received ping packet payload
                    System.out.println("---------------------------------------\n");
                    
                    String capHeader = header.toUpperCase(); //capitalize the header
                    String capPayload = payload.toUpperCase(); //capitalize the payload

                    String responsePayload = capHeader + capPayload; //combine our header and payload into one string

                    //Construct the ping response packet
                    byte[] responseData = (responsePayload).getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);

                    socket.send(responsePacket); //Send the response packet

                    //Print the response packet that was sent
                    System.out.println("-----------Ping Response Packet Header ----------");
                    System.out.println(capHeader); //Capitalized header
                    System.out.println("-----------Ping Response Packet Payload ----------");
                    System.out.println(capPayload); // Capitalized payload
                    System.out.println("---------------------------------------\n");
                }
            }
        } 
        catch(SocketException e) //Error message if program is unsuccessful in creating the socket
        {
            System.out.println("ERR - cannot create PINGServer socket using port number " + port);
        }
        catch (IOException e)   
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
