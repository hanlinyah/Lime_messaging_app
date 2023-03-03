import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client implements ActionListener {
    private JFrame win=new JFrame();
    private  JFrame loginView;

    public JTextArea msgContent=new JTextArea(20,50);

    private JTextArea msgSend=new JTextArea(5,40);

    private  JTextField ipSetting,nameSetting;

    public  JList<String> onlineUsers=new JList<>();

    private JCheckBox isPrivateBtn=new JCheckBox("私聊");
    private JButton sendBtn=new JButton("發送");

    private Socket socket;


    public static void main(String[] args) {
        new Client().initView();


    }
    private  void initView(){
        win.setSize(480,360);

        LoginView();

//        LimeChatView();
    }

    private void LimeChatView() {

        JPanel bottomPanel=new JPanel(new BorderLayout());
            bottomPanel.add(msgSend);
                JPanel btns=new JPanel(new FlowLayout(FlowLayout.LEFT));
                    btns.add(sendBtn);
                    sendBtn.addActionListener(this);
                    btns.add(isPrivateBtn);
            bottomPanel.add(btns,BorderLayout.EAST);
        win.add(bottomPanel,BorderLayout.SOUTH);

        JScrollPane centerPane=new JScrollPane();
            msgContent.setEditable(false);
            centerPane.setViewportView(msgContent);

        win.add(centerPane,BorderLayout.CENTER);

        Box rightBox=new Box(BoxLayout.Y_AXIS);
            JScrollPane rightPane=new JScrollPane();
                onlineUsers.setFixedCellWidth(120);
                onlineUsers.setVisibleRowCount(20);
                rightPane.setViewportView(onlineUsers);
            rightBox.add(rightPane);
        win.add(rightBox,BorderLayout.EAST);

        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.pack();
        win.setLocationRelativeTo(null);
        win.setVisible(true);



    }
    private void LoginView() {
        loginView=new JFrame("Lime");
        loginView.setLayout(new GridLayout(3,1));
        loginView.setSize(400,200);

        JPanel ip=new JPanel();
            JLabel iplabel=new JLabel("伺服器IP:");
                ip.add(iplabel);
            ipSetting=new JTextField(20);
                ip.add(ipSetting);
        loginView.add(ip);

        JPanel name=new JPanel();
            JLabel namelabel=new JLabel(" 您的大名:");
                name.add(namelabel);
            nameSetting=new JTextField(20);
                name.add(nameSetting);
        loginView.add(name);

        JPanel btnView=new JPanel();
            JButton login=new JButton("登錄");
                btnView.add(login);
                login.addActionListener(this);
            JButton cancel=new JButton("取消");
                btnView.add(cancel);
                cancel.addActionListener(this);
        loginView.add(btnView);

        loginView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginView.setLocationRelativeTo(null);
        loginView.setVisible(true);


    }


    @Override
    public void actionPerformed(ActionEvent e) {
    JButton clickbtn= (JButton) e.getSource();
    switch (clickbtn.getText()){
        case "登錄":
            String ip=ipSetting.getText().toString();
            String name=nameSetting.getText().toString();
            String sysmsg="";
            if((ip == null) || !ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
                    sysmsg="請輸入正確的IP位址";
            }else if(name==null || !name.matches("\\S{1,}")){
                sysmsg="請輸入姓名";
            }
            if (!sysmsg.equals("")){
                JOptionPane.showMessageDialog(loginView,sysmsg);
            }else {
                win.setTitle("Lime @ "+name);

                try {
                    System.out.println(ip);
                    System.out.println(getProperties.getport());
                    socket=new Socket(ip,getProperties.getport());

                    new ClientReader(this,socket).start();

                    DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
                    dos.writeInt(msgFlag.flagLogin);
                    dos.writeUTF(name.trim());
                    dos.flush();

                    loginView.dispose();
                    LimeChatView();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;
        case "取消":
            System.exit(0);
            break;
        case "發送":
            String msgsend=msgSend.getText().toString();
            if(!msgsend.trim().equals("")){
                int flag =(isPrivateBtn.isSelected())? msgFlag.flagpPrivate:msgFlag.flagGroup;
                String selectName=onlineUsers.getSelectedValue();
                if(selectName!=null && !selectName.trim().equals("")){
                    msgsend="@"+selectName+":"+msgsend;
                }
                    try {
                        DataOutputStream dos =new DataOutputStream(socket.getOutputStream());
                        dos.writeInt(flag);
                        dos.writeUTF(msgsend);
                        if(flag==msgFlag.flagpPrivate){
                            dos.writeUTF(selectName);
                        }
                        dos.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

            }
            msgSend.setText(null);
            break;

    }

    }
}

class ClientReader extends  Thread{
    private Socket socket;
    private  Client client;

    public ClientReader(Client client,Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream dis=null;
        try {
            dis=new DataInputStream(socket.getInputStream());
            while(true){
                int flag=dis.readInt();
                if (flag==msgFlag.flagLogin){
                    String loginmsg=dis.readUTF();
                    String[] names=loginmsg.split(getProperties.getspilit());
                    client.onlineUsers.setListData(names);
                } else if (flag==msgFlag.flagGroup || flag==msgFlag.flagpPrivate) {
                    String msg=dis.readUTF();
                    client.msgContent.append(msg);
                    client.msgContent.setCaretPosition(client.msgContent.getText().length());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
