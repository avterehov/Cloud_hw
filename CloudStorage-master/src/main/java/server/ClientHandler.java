package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		     DataInputStream in = new DataInputStream(socket.getInputStream())){
			while (true) {
				String command = in.readUTF();
				if ("upload".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());
						if (!file.exists()) {
							file.createNewFile();
						}
						long size = in.readLong();
						FileOutputStream fos = new FileOutputStream(file);
						byte[] buffer = new byte[256];
						for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						fos.close();
						out.writeUTF("DONE");
					} catch (Exception e) {
						out.writeUTF("ERROR2");
					}
				} else if ("download".equals(command)) {
					// TODO: 02.03.2021
					// realize download
					File file = new File ("server" + File.separator + in.readUTF());
					if (file.exists()) {
						long lengh = file.length();
						out.writeLong(lengh);
						FileInputStream fis = new FileInputStream(file);
						int read = 0;
						byte[] buffer = new byte[256];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						out.flush();
					}


				} else if ("delete".equals(command)) {
					// TODO: 02.03.2021
					// realize remove

					File file = new File("server" + File.separator + in.readUTF());
					if (file.exists()){
						file.delete();
						out.writeUTF("DONE");
					}else {
						out.writeUTF("ERROR1");
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/*

отправка списка файлов

public void refreshListofFiles (){
        File file = new File("server");
	   File [] listOfFiles = file.listFiles();
        DataOutputStream dos = null;
        try {      dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int listLengh = listOfFiles.length;
        try {
            dos.writeUTF("refresh");
            dos.writeInt(listLengh);
            for (int i = 1; i < listLengh; i++){
                try {
                    dos.writeUTF(listOfFiles[i].toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("LIST REFRESHED");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
*/

}
