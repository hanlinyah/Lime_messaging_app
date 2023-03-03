import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {
    public static Map<Socket,String> onlineSockets=new HashMap<>();

    public static void main(String[] args) {
        System.out.println("伺服器程序已啟動");

        try {
            ServerSocket serverSocket=new ServerSocket(getProperties.getport());

            while(true){
                Socket socket=serverSocket.accept();

                new ServerReader(socket).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
class ServerReader extends Thread{

    private Socket socket;

    public ServerReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream dis=null;
        String name=null;
        try {
            dis=new DataInputStream(socket.getInputStream());
            while(true){
                int flag=dis.readInt();
                if (flag==msgFlag.flagLogin){
                    name=dis.readUTF();
                    System.out.println(name+"從"+socket.getRemoteSocketAddress()+"登入了~~");
                    Server.onlineSockets.put(socket,name);
                }
                writMsg(flag,dis);
            }

        } catch (Exception e) {
            System.out.println(name+"從"+socket.getRemoteSocketAddress()+"下線了");
            Server.onlineSockets.remove(socket);

            try {
                writMsg(msgFlag.flagLogin,dis);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void writMsg(int flag, DataInputStream dis) throws Exception {
        String loginmsg=null;
        if(flag==msgFlag.flagLogin){
            StringBuilder onlineNamesList=new StringBuilder();
            Collection<String> onlineNames=Server.onlineSockets.values();
            if(onlineNames !=null && onlineNames.size()>0){
                for (String name: onlineNames){
                    onlineNamesList.append(name+getProperties.getspilit());
                }
                loginmsg=onlineNamesList.substring(0,onlineNamesList.lastIndexOf(getProperties.getspilit()));

                sendMsgToAll(flag,loginmsg);
            }
        } else if (flag==msgFlag.flagGroup ||flag==msgFlag.flagpPrivate) {
            String sendMsg=dis.readUTF();
            String sendName=Server.onlineSockets.get(socket);
            StringBuilder finalMsg=new StringBuilder();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss EEE");
            if(flag == msgFlag.flagGroup){
                finalMsg.append(sendName+"\t")
                        .append(sdf.format(System.currentTimeMillis()))
                        .append("\r\n")
                        .append("\t")
                        .append(sendMsg)
                        .append("\r\n");
                sendMsgToAll(flag,finalMsg.toString());
            } else if (flag==msgFlag.flagpPrivate) {
                finalMsg.append(sendName+"\t")
                        .append(sdf.format(System.currentTimeMillis()))
                        .append("對您發送私聊訊息\r\n")
                        .append("\t")
                        .append(sendMsg)
                        .append("\r\n");
                String destName=dis.readUTF();
                sendMsgToOne(destName,finalMsg.toString());
            }

        }
    }
    private void sendMsgToOne(String destName, String msg) throws Exception {
        Set<Socket> allOnlineSockets=Server.onlineSockets.keySet();
        for(Socket socket1 :allOnlineSockets){
            if (Server.onlineSockets.get(socket1).trim().equals(destName)){
                DataOutputStream dos=new DataOutputStream(socket1.getOutputStream());
                dos.writeInt(msgFlag.flagpPrivate);
                dos.writeUTF(msg);
                dos.flush();
            }
        }
    }

    private void sendMsgToAll(int flag, String msg) throws Exception {
        Set<Socket> allOnlineSockets=Server.onlineSockets.keySet();
        for(Socket socket1 :allOnlineSockets){
            DataOutputStream dos=new DataOutputStream(socket1.getOutputStream());
            dos.writeInt(flag);
            dos.writeUTF(msg);
            dos.flush();
        }
    }


}