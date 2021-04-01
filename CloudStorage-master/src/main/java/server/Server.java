package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	public Server() {
		ExecutorService service = Executors.newFixedThreadPool(4);

		try (ServerSocket server = new ServerSocket(1235)){
			System.out.println("Server started");
			AuthService.connect();
			System.out.println("Data base connected");

			while (true) {
				service.execute(new ClientHandler(server.accept()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
