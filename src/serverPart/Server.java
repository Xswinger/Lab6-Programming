package serverPart;

import dto.Command;
import dto.Id;
import dto.Message;
import serverPart.mainClasses.Invoker;
import serverPart.utils.FileNameTaker;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static int serverPort = 63226;
    private static final DatagramSocket datagramSocket;

    static {
        try {
            datagramSocket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static String savingFile;
    public static boolean checkFileUpload;

    private static final byte[] inputDataBuffer = new byte[1024];


    public static void connectionSetup(DatagramPacket inputPacket) throws IOException {
        datagramSocket.receive(inputPacket);
        System.out.println("Input message is got");
        InetAddress senderAddress = inputPacket.getAddress();
        serverPort = inputPacket.getPort();
        byte[] sendingDataBuffer = Message.serialize(new Message(1, 1, "Connection successfully"));
        DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                senderAddress, serverPort);
        datagramSocket.connect(senderAddress, serverPort);
        System.out.println("Command processing");
        datagramSocket.send(sendingPacket);
    }

    //Получение команды, ее обработка и отправка назад
    public static void receiveData() throws IOException, ClassNotFoundException {
        checkFileUpload = false;
        DatagramPacket inputPacket = new DatagramPacket(inputDataBuffer, inputDataBuffer.length);
        DatagramPacket sendingPacket;
        connectionSetup(inputPacket);
        while (true) {
            specialCommand();
            datagramSocket.receive(inputPacket);
            InetAddress senderAddress = inputPacket.getAddress();
            serverPort = inputPacket.getPort();
            Command command = (Command) Command.deserialize(inputPacket.getData());
            Invoker invoker = new Invoker();
            byte[] sendingDataBuffer;
            if (!checkFileUpload) {
                Message message = FileNameTaker.getFileNameFromEnvironment(command);
                checkFileUpload = FileNameTaker.checkFileName(message);
                savingFile = System.getenv(command.getParameterOfCommand());
                sendingDataBuffer = Message.serialize(message);
                sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                        senderAddress, serverPort);
                datagramSocket.send(sendingPacket);
                //Отдельный if для команд с отправкой клиенту 1 сообщения
            } else if (Arrays.stream(new String[]{"history", "help", "execute_script", "show", "filter_by_distance", "print_descending", "exit"}).noneMatch(s -> s.equals(command.getNameOfCommand()))) {
                sendingDataBuffer = Message.serialize(invoker.choiceCommandManual(command).get(0));
                sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                        senderAddress, serverPort);
                datagramSocket.send(sendingPacket);
            } else if (command.getNameOfCommand().equals("exit")) {
                System.out.println("Ending processing...\n");
                Id.zeroingId();
                Id.zeroingIdSet();
                checkFileUpload = false;
                sendingDataBuffer = Message.serialize(invoker.choiceCommandManual(command).get(0));
                sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                        senderAddress, serverPort);
                datagramSocket.send(sendingPacket);
                datagramSocket.disconnect();
                break;
            } else {
                List<Message> arrayOfMessage = invoker.choiceCommandManual(command);
                int messageCount = arrayOfMessage.get(0).getMessageCount();
                sendingDataBuffer = Message.serialize(arrayOfMessage.get(0));
                sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                        senderAddress, serverPort);
                datagramSocket.send(sendingPacket);
                //Цикл отправки сообщений
                for (int g = 1; g < messageCount; g++) {
                    sendingDataBuffer = Message.serialize(arrayOfMessage.get(g));
                    DatagramPacket sendingNewPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length,
                            senderAddress, serverPort);
                    datagramSocket.send(sendingNewPacket);
                }
            }
        }
    }

    private static void specialCommand() throws IOException {
        if (System.in.available() > 0) {
            if (new Scanner(System.in).nextLine().equals("sc")) {
                System.out.println((new Invoker().choiceCommandManual(new Command("exit"))).
                        get(0).getContentString());
            }
        }
    }
}
