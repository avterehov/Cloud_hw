package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private String clientName;
	private  File repository;

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}

	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClientCmd();
	}



	private void runClientCmd(){
		Scanner sc = new Scanner (System.in);


		// авторизация клиента

		autorisation();

		// создание папки для клиента если ее еще нет

		repository  = new File ("client" + File.separator + clientName);
		if (!repository.exists()){
			repository.mkdir();
		}

		//обработка входящих команд от пользователя

		String command;
		String fileName;
		while (true) {
		command = sc.nextLine();

		if (command.startsWith("upload ")){
			fileName = command.split(" ")[1];
			System.out.println(sendFile(fileName));
		}else if (command.startsWith("download ")) {
			fileName = command.split(" ")[1];
			System.out.println(downloadFile(fileName));
		}else if (command.startsWith("delete ")){
			fileName = command.split(" ")[1];
			System.out.println(deleteFile(fileName));
		}else if (command.equals("list")){
			System.out.println("Files:");
			printList(downloadFileList());
		}else if(command.equals("help")){
				System.out.println("Commands: \n" +
									"help  - 	list of commands \n" +
									"download 'file name' - to get file from server \n" +
									"upload 'file name'- send file to server\n" +
									"delete 'file name' - delete file on server\n" +
									"list - to get list of file on server");

		}else{
			System.out.println("ERROR. Wrong command!");
		}
		}

	}

	//метод авторизации. отправляет команду, логин, пароль.
	// после проверки сервером получает ответ и ник пользователя


	private  void autorisation(){
		try {
		while (true) {
			System.out.println("for test:		login1 		password1");
			Scanner sc = new Scanner(System.in);
			System.out.println("login:");
			String login = sc.nextLine();
			System.out.println("password:");
			String password = sc.nextLine();
			try {
				out.writeUTF("auth");
			} catch (IOException e) {
				e.printStackTrace();
			}

			out.writeUTF(login);

			out.writeUTF(password);
			String status = in.readUTF();
			System.out.println(status);

			if ("authOk".equals(status)) {
				clientName = in.readUTF();
				System.out.println("Hello " + clientName);
				break;
			} else {
				System.out.println("try again");
			}
		}
		} catch (IOException e) {
				e.printStackTrace();
			}
		}


	// метод для вывода списка загруженного листа с файлами

	private void printList (List <String> files){
		for (String file : files){
			System.out.println(file);
		}
	}

	// метод для получения списка файлов репозитория с сервера

	private List<String> downloadFileList() {

		List<String> list = new ArrayList<>();
		try {
			StringBuilder sb = new StringBuilder();
			out.writeUTF("list-files");
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if (sb.toString().endsWith("end")) {
					break;
				}
			}
			String fileString = sb.substring(0, sb.toString().length() - 4);
			list = Arrays.asList(fileString.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}


// метод для отправки файла

	private String sendFile(String filename) {
		try {
			File file = new File(repository + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();

				String status = in.readUTF();
				return status;
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	// метод для загрузки файла

	private String downloadFile (String fileName){
		try {
			out.writeUTF("download");
			out.writeUTF(fileName);
			File file = new File (repository + File.separator + fileName);
			if (!file.exists()){
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
			return ("DONE");

			//если прнимаю статус от сервера то он этот статус писет в файл не понимаю как исправить
			//пока поставил такой кастыль, что сообщение
			//String status = in.readUTF();
			//return status;

		} catch (IOException e) {
			e.printStackTrace();
		}return "ERROR3";

	}

	// метод для удаления файла на сервере
	private String deleteFile (String fileName){
		try {

			out.writeUTF("delete");
			out.writeUTF(fileName);
			String status = in.readUTF();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			}
		return "ERROR";
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
