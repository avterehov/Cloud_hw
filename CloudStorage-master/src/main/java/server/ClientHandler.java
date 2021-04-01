package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	private final Socket socket;

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	private String clientName;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		     DataInputStream in = new DataInputStream(socket.getInputStream())){

			//процедура авторизации перед  началом обработки комманд от клиента

			while (true){
				String command = in.readUTF();
				if(command.equals("auth")) {
					String login = in.readUTF();
					String password = in.readUTF();
					String newNick = AuthService.getNickByLoginAndPass(login, password);

					if (newNick != null) {
						System.out.println(newNick);
						out.writeUTF("authOk");
						out.writeUTF(newNick);
						System.out.println("client "+ newNick + " ready for work");
						setClientName(newNick);
						break;

					}else {
						out.writeUTF("ERROR. login or password  is incorrect");
					}
				}
			}


			// создание индивидуальной папки для клиента, если она еще не создана


			File repository = new File ("server" + File.separator + clientName);
			if (!repository.exists()){
				repository.mkdir();
			}
			// начало обработки комманд от клиента

				while (true) {
				String command = in.readUTF();


				// обработка комманды от клиетна на загрузку файла
				if ("upload".equals(command)) {
					try {
						File file = new File(repository + File.separator + in.readUTF());
						if (!file.exists()) {
							file.createNewFile();
						}
						long size = in.readLong();
						FileOutputStream fos = new FileOutputStream(file);
						byte[] buffer = new byte[256];
						for (int i = 0; i < (size + 255) / 256; i++) {
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						fos.close();
						out.writeUTF("DONE");
					} catch (Exception e) {
						out.writeUTF("ERROR2");
					}
					//обработка команды на загрузку файла с сервера
					// тут есть какая-то ошибка в коде. клиент скачивает файл 1 раз и подвисает блокируя все остальные комманды


				} else if ("download".equals(command)) {
					File file = new File (repository + File.separator + in.readUTF());
					if (file.exists()) {
						long length = file.length();
						out.writeLong(length);
						FileInputStream fis = new FileInputStream(file);
						int read = 0;
						byte[] buffer = new byte[256];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						fis.close();
						out.flush();
						//out.writeUTF("Done");
						//System.out.println("отправил статус");

					}else {
						out.writeUTF("file did not found!");
					}
				//  обработка комманды на удаление файла
				} else if ("delete".equals(command)) {
					File file = new File(repository + File.separator  + in.readUTF());
					if (file.delete()){
						out.writeUTF("DONE");
					}else {
						out.writeUTF("ERROR1");
					}
				//обработка комманды на получение списка файлов в репозитории
				} else if (command.equals("list-files")){
						System.out.println(repository.toString());
						File [] files = repository.listFiles();
						StringBuffer sb = new StringBuffer();
						for (File f : files){
							System.out.println(f.toString());
							sb.append(f.getName()+ "\n");
						}
						sb.append("end");
						out.writeUTF(sb.toString());
				}	else {
					System.out.println("ERROR");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
