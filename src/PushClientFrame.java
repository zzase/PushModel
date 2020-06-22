import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PushClientFrame extends JFrame {
	private Receiver text = new Receiver();
	private JButton startBtn = new JButton("����");
	private Socket socket = null;
	private BufferedReader in = null;
	public PushClientFrame() {
		super("push client");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400,400);
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(new JScrollPane(text));
		c.add(startBtn,BorderLayout.SOUTH);
		
		startBtn.addActionListener(new MyAction());
		
		Component [] ch = c.getComponents();
		for(int i=0; i<ch.length; i++) {
			ch[i].setFont(new Font("����",Font.PLAIN,25));
		}
		
		text.setFont(new Font("����",Font.PLAIN,25));
		
		setVisible(true);
	}
	
	public void handleError(String msg) {
		System.out.println(msg);
		System.exit(0);
	}
	
	private void setup() {
		try {
			socket = new Socket("localhost",9999); //binding
			text.append("����Ϸ�\n");
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			handleError(e.getMessage());
		}
	}
	
	class MyAction implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setup();
			Thread th = new Thread(text);
			th.start();
		}
		
	}
	class Receiver extends JTextArea implements Runnable{
		
		public void run() {
			while(true) {
				try {
					String msg = in.readLine();
					this.append(msg+"\n");
				} catch (IOException e) {
					handleError(e.getMessage());
				}
			}
		}
	}
	public static void main(String[] args) {
		new PushClientFrame();
		
	}

}
