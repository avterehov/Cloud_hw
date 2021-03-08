package client;

import javax.print.event.PrintJobAttributeListener;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		JTextArea ta = new JTextArea();
		// TODO: 02.03.2021
		// list file - JList
		DefaultListModel dlm = new DefaultListModel();
		JList serverFiles = new JList();
 		JPanel buttomPanel = new JPanel();
		JPanel listOfFilesPanel = new JPanel();
		JButton uploadButton = new JButton("Upload");
		JButton downloadButton = new JButton("Download");
		JButton deleteButton = new JButton("Delete");
		frame.getContentPane().add(BorderLayout.SOUTH, buttomPanel);
		frame.getContentPane().add(BorderLayout.NORTH, ta);
		frame.getContentPane().add(BorderLayout.CENTER, listOfFilesPanel);
		buttomPanel.add(uploadButton);
		buttomPanel.add(downloadButton);
		buttomPanel.add(deleteButton);
		listOfFilesPanel.add(serverFiles);
		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
		});

		downloadButton.addActionListener(a -> {
			System.out.println(downloadFile(ta.getText()));
		});

		deleteButton.addActionListener(a -> {
			System.out.println(deleteFile(ta.getText()));
		});

	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
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

	private String downloadFile (String fileName){
		try {
			out.writeUTF("download");
			out.writeUTF(fileName);
			File file = new File ("client" + File.separator + fileName);
			if (!file.exists()){
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
			String status = in.readUTF();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
		}return "ERROR3";

	}

	private String deleteFile (String fileName){
		try {

			out.writeUTF("delete");
			out.writeUTF(fileName);
			String status = in.readUTF();

		} catch (IOException e) {
			e.printStackTrace();
			}
		return "done";
	}

/*



private void getList(){
		try {
			String command = in.readUTF();
			if ("refresh".equals(command)){
				int length = in.readInt();
				String [] listOfFiles = new String[length];

				for (int i = 1; i < length; i++ ){
					listOfFiles [i] = in.readUTF();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
