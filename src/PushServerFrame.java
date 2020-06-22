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
		c.add(new JLabel("입력 창"));
		c.add(text);
		c.add(new JLabel("접속자 수"));
		c.add(clientCountLabel);
		c.add(new JLabel("전송 완료 수"));
		c.add(deliveredCountLabel);
		c.add(new JScrollPane(clientList));
		
		Component [] ch = c.getComponents();
		for(int i=0; i<ch.length; i++) {
			ch[i].setFont(new Font("고딕",Font.PLAIN,25));
		}
		
		clientList.setFont(new Font("고딕",Font.PLAIN,25));
		
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
					socket = listener.accept(); //클라이언트 접속 대기
					clientList.append("client 접속\n");
					//서비스 스레드 만들고 시작시킴
					ServiceThread th = new ServiceThread(socket);
					clientCountLabel.setText(Integer.toString(threadCount));
					
					th.start();
				} catch (IOException e) {
					handleError(e.getMessage());
				}
			}
		}
	}
	
	private int threadCount = 0; //현재 접속한 클라이언트 수 
	
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
					String msg = text.get(); //getText() 안에서 사용자가 입력완료 엔터키까지 대기
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
		private int deliveredCount = 0; //전송 완료한 횟수
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
			//wait은 반드시 synchronized해야함
			try {
				this.wait(); // 이 객체를 다른 스레드가 notify()할 때까지 대기
			} catch (InterruptedException e) {
				handleError(e.getMessage());
			}
			increaseDeliveredCount();
			
			if(deliveredCount == threadCount) //전송된 수와 스레드 수가 같으면 전송이 완료된 것 
				this.notify(); //이벤트 디스패치 스레드(Action 리스너의 코드에서 대기) 
			return this.getText();
		}
		
		synchronized public void put() {
			if(deliveredCount != threadCount) { //전송된수와 스레드 수가 같지않으면 아직 완료되지 않았다
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
