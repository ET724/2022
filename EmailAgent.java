import java.io.BufferedReader; 

public class EmailAgent { 	
	private static String smtpServer = "smtp.163.com";
	private static String popServer = "pop.163.com";
	
	private static int smtpport = 465;
	private static int popport = 995;

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String account = null;
		String password = null;
		
		while(true){
			System.out.print("请输入您的邮箱账号(只支持qq、163邮箱、新浪邮箱)：");
			account = input.nextLine();
			if(account.indexOf("@qq.com") != -1){
				smtpServer = "smtp.qq.com";
				popServer = "pop.qq.com";
				break;
			}else if(account.indexOf("@163.com") != -1){		
				smtpServer = "smtp.163.com";
				popServer = "pop.163.com";
				break;
			}else if(account.indexOf("@sina.cn") != -1){
				smtpServer = "smtp.sina.cn";
				popServer = "pop.sina.cn";
				break;
			}else{
				System.out.println("未识别此邮箱，请重新输入");
			}
		}
		
		while(true){
			System.out.print("请输入密码：");
			password = input.nextLine();
			Socket socket = null;
			try{
				socket = SSLSocketFactory.getDefault().createSocket(smtpServer,smtpport);
				socket.setSoTimeout(10000);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter pw = new PrintWriter(socket.getOutputStream(),true); 
				String localhost = InetAddress.getLocalHost().getHostName();
				String base64account = new BASE64Encoder().encode(account.getBytes());
				String base64password = new BASE64Encoder().encode(password.getBytes()); 
				System.out.println("Server>"+br.readLine());
				pw.println("HELO "+localhost);
				String temp = br.readLine();
				if(temp.indexOf("250") == -1){
					System.out.println("无法连接服务器");
				}
				pw.println("AUTH LOGIN");
				temp = br.readLine();
				pw.println(base64account);
				temp = br.readLine();
				pw.println(base64password);
				temp = br.readLine();
				System.out.println("Server>"+temp);
				pw.println("QUIT");
				if(temp.indexOf("235") != -1){
					System.out.println("验证成功");
					socket.close();
					break;
				}else{
					System.out.println("验证失败");
				}
			}catch (IOException e){
				e.printStackTrace();
			}finally{ 
				if(socket !=null){ 
					try { 
						socket.close(); 
					} catch (IOException e) { 
						e.printStackTrace(); 
					} 
				}
			} 			
		}
		
		while(true){
			String option = null;
			System.out.println("请选择：quit:退出|send:发邮件|receive:收邮件");
			option = input.nextLine();
			if(option.equals("quit")){
				if(input != null){
					input.close();
				}
				break;
			}
			switch(option){
			case "send":
				System.out.print("请输入收件人地址：");
				String address = input.nextLine();
				System.out.print("请输入邮件主题：");
				String subject = input.nextLine();
				System.out.print("请输入邮件内容：");
				String content = input.nextLine();
				Message msg = new Message(account, password, account, address, subject, content);
				new EmailAgent().sendMail(msg);
				break;
			case "receive":
				new EmailAgent().popMail(account, password, input);
				break;
			default:
				System.out.println("未识别序号");
				break;
			}
			
		}
		Message msg = new Message("1324908543@qq.com", "1010465036@qq.com", "测试", "1111111"); 
		new EmailAgent().sendMail(msg); 
		new EmailAgent().popMail();
	}
	
	private static boolean isBase64(String str) {
	    if (str == null || str.length() == 0) {
	        return false;
	    } else {
	        if (str.length() % 4 != 0) {
	            return false;
	        }

	        char[] strChars = str.toCharArray();
	        for (char c:strChars) {
	            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') 
	                || c == '+' || c == '/' || c == '=') {
	                continue;
	            } else {
	                return false;
	            }
	        }
	        return true;
	    }
	}
	
	private void sendAndReceive(String str,BufferedReader br,PrintWriter pw) throws IOException { 
		if(str != null){ 
			System.out.println("Client>"+str); 
			pw.println(str); 
		} 
		String response; 
		if((response = br.readLine())!=null){ 
			System.out.println("Server>"+response); 
		} 
	}
	
	public void sendMail(Message msg){ 
		Socket socket = null; 
		try { 
			socket = SSLSocketFactory.getDefault().createSocket(smtpServer,smtpport);
			socket.setSoTimeout(10000);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream(),true); 
			String localhost = InetAddress.getLocalHost().getHostName(); 
			

			
			String base64account = new BASE64Encoder().encode(msg.username.getBytes());
			String base64password = new BASE64Encoder().encode(msg.password.getBytes());
			System.out.println("Server>"+br.readLine());
			sendAndReceive("HELO "+localhost, br, pw); 
			sendAndReceive("AUTH LOGIN", br, pw); 
			sendAndReceive(base64account, br, pw); 
			sendAndReceive(base64password, br, pw);  
			sendAndReceive("MAIL FROM:<"+msg.from+">", br, pw); 
			sendAndReceive("RCPT TO:<"+msg.to+">", br, pw); 
			pw.println("DATA"); 
			System.out.println("Client>"+ "DATA");
			pw.println("From:"+msg.from); 
			System.out.println("Client>"+"From:"+msg.from);
			pw.println("To:"+msg.to); 
			System.out.println("Client>"+"To:"+msg.to);
			pw.println("Subject:"+ msg.subject+"\r\n"); 
			System.out.println("Client>"+"Subject:"+msg.subject);
			pw.println(msg.content); 
			System.out.println("Client>"+ "Content:" + msg.content);
			
			sendAndReceive(".", br, pw); 
			sendAndReceive("QUIT", br, pw); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		} finally{ 
			if(socket!=null){ 
				try { 
					socket.close(); 
				} catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
	
	} 
	
	public void popMail(String account, String password, Scanner input){
		Socket socket = null;
        try {
            socket = SSLSocketFactory.getDefault().createSocket(popServer, popport);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream(),true); 
       
			System.out.println("Server>"+br.readLine());


			sendAndReceive("user "+account, br,pw);  //用户名
			sendAndReceive("PASS "+password, br,pw);  //用户名
		
            pw.println("stat");
            String temp[] = br.readLine().split(" ");
            int count = Integer.parseInt(temp[1]);//得到信箱中共有多少封邮件
            System.out.println("在收件箱共有" + count + "封邮件");
            while(true){
            	System.out.println("请输入：quit:退出|ckeck:查看邮件内容");
                String next = input.nextLine();
                if(next.equals("quit")){
                	break;
                }
                switch(next){
                	case "quit":
                		break;
                	case "check":
                		System.out.print("您希望打开第几封？(最新的邮件排在最后)>");
                        int open = Integer.parseInt(input.nextLine());
                        System.out.println("请输入字符集，如GBK, UTF-8：");
                        String charset = input.nextLine();
                        pw.println("retr " + open);
                        System.out.println("第" + open + "封邮件的内容:");
                        while (true) {
                            String reply = br.readLine();
                            if(isBase64(reply)){
                            	byte[] bt = new BASE64Decoder().decodeBuffer(reply);
                            	reply = new String(bt,charset);
                            }
                            System.out.println(reply);
                            if (reply.toLowerCase().equals(".")) {
                                break;
                            }
                        }
                        break;
                	default:
                		System.out.println("未识别操作");
                		break;
                }               
            }         
        } catch (IOException e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (socket != null ) {
                    socket.close();
                }
            } catch (IOException e) {}
        }
    }
} 

class Message{
	String username;
	String password;
	String from;
	String to; 
	String subject; 
	String content; 
	public Message(String username, String password, String from,String to,String subject,String content){
		this.username = username;
		this.password = password;
		this.from = from; 
		this.to = to; 
		this.subject = subject; 
		this.content = content; 
	} 
} 