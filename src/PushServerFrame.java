import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PushServerFrame extends JFrame{
	
	private JTextArea clientList = new JTextArea(7,20);
	private JLabel clientCountLabel = new JLabel("0");
	private JLabel deliveredCountLabel = new JLabel("0");
	private MyTextField text = new MyTextField(10);
	
	public PushServerFrame() {
		super("push server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400,400);
		Container c = getContentPane();
		c.setLayout(new FlowLayout());
		c.add(new JLabel("�Է� â"));
		c.add(text);
		c.add(new JLabel("������ ��"));
		c.add(clientCountLabel);
		c.add(new JLabel("���� �Ϸ� ��"));
		c.add(deliveredCountLabel);
		c.add(new JScrollPane(clientList));
		
		Component [] ch = c.getComponents();
		for(int i=0; i<ch.length; i++) {
			ch[i].setFont(new Font("���",Font.PLAIN,25));
		}
		
		clientList.setFont(new Font("���",Font.PLAIN,25));
		
		setVisible(true);
		
		startService();
	}
	
	public void startService() {
		new ServerThread().start();
	}
	
	public void handleError(String msg) {
		System.out.println(msg);
		System.exit(0);
	}
	class ServerThread extends Thread {
		public void run() {
			ServerSocket listener = null;
			Socket socket = null;
			
			try {
				listener = new ServerSocket(9999);
			} catch (IOException e) {
			
				handleError(e.getMessage());
			}
			while(true) {
				try {
					socket = listener.accept(); //Ŭ���̾�Ʈ ���� ���
					clientList.append("client ����\n");
					//���� ������ ����� ���۽�Ŵ
					ServiceThread th = new ServiceThread(socket);
					clientCountLabel.setText(Integer.toString(threadCount));
					
					th.start();
				} catch (IOException e) {
					handleError(e.getMessage());
				}
			}
		}
	}
	
	private int threadCount = 0; //���� ������ Ŭ���̾�Ʈ �� 
	
	class ServiceThread extends Thread {
		private Socket socket;
		public ServiceThread(Socket socket) {
			this.socket = socket;
			threadCount++;
			clientCountLabel.setText(Integer.toString(threadCount));
			text.increaseDeliveredCount();
		}
		
		public void run() {
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				while(true) {
					String msg = text.get(); //getText() �ȿ��� ����ڰ� �Է¿Ϸ� ����Ű���� ���
					clientList.append(msg+"\n");
					out.write(msg+"\n");
					out.flush();
					
				}
			} catch (IOException e) {
				handleError(e.getMessage());
			}
			
		}
	}
	
	class MyTextField extends JTextField{
		private int deliveredCount = 0; //���� �Ϸ��� Ƚ��
		public MyTextField(int size) {
			super(size);
			this.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					put();
					//clearDeliveredCount();
				}
			});
		}
		
		public void increaseDeliveredCount() {
			deliveredCount++;
			deliveredCountLabel.setText(Integer.toString(deliveredCount));
		}
		
		public void clearDeliveredCount() {
			deliveredCount = 0;
			deliveredCountLabel.setText(Integer.toString(deliveredCount));
		}
		synchronized public String get() {
			//wait�� �ݵ�� synchronized�ؾ���
			try {
				this.wait(); // �� ��ü�� �ٸ� �����尡 notify()�� ������ ���
			} catch (InterruptedException e) {
				handleError(e.getMessage());
			}
			increaseDeliveredCount();
			
			if(deliveredCount == threadCount) //���۵� ���� ������ ���� ������ ������ �Ϸ�� �� 
				this.notify(); //�̺�Ʈ ����ġ ������(Action �������� �ڵ忡�� ���) 
			return this.getText();
		}
		
		synchronized public void put() {
			if(deliveredCount != threadCount) { //���۵ȼ��� ������ ���� ���������� ���� �Ϸ���� �ʾҴ�
				try {
					this.wait();
				} catch (InterruptedException e) {
					handleError(e.getMessage());
				}
			}
			clearDeliveredCount();
			this.notifyAll();
		}
	}
	
	public static void main(String[] args) {
		new PushServerFrame();

	}

}
