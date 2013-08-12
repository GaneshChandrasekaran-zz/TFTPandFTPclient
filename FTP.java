/**
 * filename: FTP.java
 *
 * Version: 1.0
 *
 * Revisions: None
 *
 */

/**
 *
 * Program to implement an FTP client that allows you to download files from FTP
 * server and implement various FTP commands
 *
 * @author GANESH CHANDRASEKARAN
 */
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FTP {

	// The prompt
	public static final String PROMPT = "FTP> ";
	public static InputStream input, dataInput;
	public static OutputStream output, dataOutput;
	public static BufferedReader br = null, br1 = null;
	public static String userName;
	public static boolean debug = false;
	public static Socket socket;
	public static String host;
	public static final int DEFAULT_PORT = 21;
	public static boolean eof = false;
	public static boolean isPassive = true;
	public static String tempHost;
	// Information to parse commands
	public static final String COMMANDS[] = { "ascii", "binary", "cd", "cdup",
			"debug", "dir", "get", "help", "passive", "put", "pwd", "quit",
			"user" };
	public static final int ASCII = 0;
	public static final int BINARY = 1;
	public static final int CD = 2;
	public static final int CDUP = 3;
	public static final int DEBUG = 4;
	public static final int DIR = 5;
	public static final int GET = 6;
	public static final int HELP = 7;
	public static final int PASSIVE = 8;
	public static final int PUT = 9;
	public static final int PWD = 10;
	public static final int QUIT = 11;
	public static final int USER = 12;
	public static int tempPort = 0;
	// Help message
	public static final String[] HELP_MESSAGE = {
			"ascii      --> Set ASCII transfer type",
			"binary     --> Set binary transfer type",
			"cd <path>  --> Change the remote working directory",
			"cdup       --> Change the remote working directory to the",
			"               parent directory (i.e., cd ..)",
			"debug      --> Toggle debug mode",
			"dir        --> List the contents of the remote directory",
			"get path   --> Get a remote file",
			"help       --> Displays this text",
			"passive    --> Toggle passive/active mode",
			"put path   --> Transfer the specified file to the server",
			"pwd        --> Print the working directory on the server",
			"quit       --> Close the connection to the server and terminate",
			"user login --> Specify the user name (will prompt for password" };

	/**
	 * 
	 * Reads the message from Server Stream
	 * 
	 * @param br
	 *            BufferedReader Object
	 * @exception IOException
	 *                If an input or output exception occurs
	 */
	public void readFromServerStream(BufferedReader br) throws IOException {

		String message;
		do {
			message = br.readLine();
			System.out.println(message);

			if (((message.startsWith("220")) || (message.startsWith("230")))
					&& message.charAt(3) != '-') {
				break;
			}
		} while (message != null);
	}

	/**
	 * Prints the message from the server
	 * 
	 * @throws IOException
	 */
	private void printMessage() throws IOException {

		String message;
		message = br.readLine();
		System.out.println(message);
	}

	/**
	 * 
	 * Connects to the FTP server
	 * 
	 * @param host
	 *            String object specifies the host name
	 * @param defaultPort
	 *            integer object specifies the default ftp port
	 */
	public void connect(String host, int defaultPort) {

		String userInput[] = null;
		String argv[] = { "", "" };

		try {
			Socket sock = new Socket(host, defaultPort);
			input = sock.getInputStream();
			output = sock.getOutputStream();
			br = new BufferedReader(new InputStreamReader(input));
			tempPort = sock.getLocalPort();
			readFromServerStream(br);

			System.out.println("Please enter your username: ");

			userInput = readInput();
			argv[0] = "user";
			argv[1] = userInput[0];

			execute(USER, argv);

		} catch (UnknownHostException e) {
			System.out.println("****Unknown Host Exception****");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("****IOException****");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Reads the input from command line
	 * 
	 * @return inputArgs String
	 */
	public String[] readInput() {
		String input = "";
		Scanner in = new Scanner(System.in);
		boolean eof = false;
		String inputArgs[] = null;

		try {
			input = in.nextLine();
		} catch (NoSuchElementException e) {
			eof = true;
		}

		if (!eof && input.length() > 0) {
			inputArgs = input.split("\\s+");
		}
		if (eof) {
			inputArgs[0] = "EOF";
		}

		return inputArgs;
	}

	/**
	 * 
	 * Establishes connection with the server
	 * 
	 * @exception IOException
	 *                If an input or output exception occurs
	 */
	public static void establishConnection() throws IOException {

		String ftpCommand = null;
		byte[] bufferCommand = null;
		String message = "";
		String splitMessage[] = null;
		int m, n, serverPort = 0;

		if (isPassive) {

			ftpCommand = "PASV " + "\n";
			bufferCommand = ftpCommand.getBytes();
			output.write(bufferCommand);
			output.flush();
		} else {
			bufferCommand = tempHost.getBytes();
			output.write(bufferCommand);
			output.flush();
		}

		if (debug) {
			System.out.println("****Connected to server****");
		}
		message = br.readLine();
		System.out.println(message);
		splitMessage = message.split(",");

		m = Integer.parseInt(splitMessage[4]);
		n = Integer.parseInt(splitMessage[5].substring(0,
				splitMessage[5].length() - 2));
		serverPort = m * 256 + n;

		socket = new Socket(host, serverPort);
		dataInput = socket.getInputStream();
		dataOutput = socket.getOutputStream();
		br1 = new BufferedReader(new InputStreamReader(dataInput));
	}

	/**
	 * 
	 * Executes the command specified by the user
	 * 
	 * @param cmd
	 * @param tempArray
	 */
	public void execute(int cmd, String[] tempArray) {
		String ftpCommand = "";

		byte[] bufferCommand = null;
		String message = null;

		try {
			// Execute the command
			switch (cmd) {
			case ASCII:
				ftpCommand = "TYPE A " + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out.println("****Set to TYPE A****");
				}
				printMessage();
				break;

			case BINARY:
				ftpCommand = "TYPE I " + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out.println("****Set to TYPE I****");
				}
				printMessage();
				break;

			case CD:
				if (tempArray.length == 1) {
					ftpCommand = "CWD " + "\n";
					bufferCommand = ftpCommand.getBytes();
					output.write(bufferCommand);
					output.flush();

					if (debug) {
						System.out.println("****CWD****");
					}
					printMessage();
				} else {
					ftpCommand = "CWD " + tempArray[1] + "\n";
					bufferCommand = ftpCommand.getBytes();
					output.write(bufferCommand);
					output.flush();

					if (debug) {
						System.out.println("****CWD set to: " + tempArray[1]
								+ " ****");
					}
					printMessage();
				}
				break;

			case CDUP:
				ftpCommand = "CDUP " + "\n";

				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out.println("****CDUP****");
				}
				printMessage();
				break;

			case DEBUG:
				if (debug) {
					debug = false;
					System.out.println("Debugging Mode: OFF");
				} else {
					debug = true;
					System.out.println("Debugging Mode: ON");
				}
				break;

			case DIR:
				establishConnection();

				ftpCommand = "LIST " + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out.println("****Directory List****");
				}

				printMessage();

				int j;
				while ((j = dataInput.read()) != -1) {
					System.out.print((char) j);
				}
				socket.close();
				printMessage();
				break;

			case GET:
				String fileName;
				if (tempArray.length > 1) {
					fileName = tempArray[1];
					establishConnection();

					ftpCommand = "RETR " + fileName + "\n";
					bufferCommand = ftpCommand.getBytes();
					output.write(bufferCommand);
					output.flush();

					if (debug) {
						System.out.println("****Retrieve " + fileName + "****");
					}

					message = br.readLine();
					System.out.println(message);
					if (!message.startsWith("150")) {
						break;
					}

					writeToFile(fileName);
					socket.close();
					while (true) {
						message = br.readLine();
						if (message != null && message.length() > 3) {
							if (message.startsWith("226")
									&& message.charAt(3) != '-') {
								System.out.println(message);
								break;
							}
						}
					}
					;
				} else {
					System.out
							.println("Please enter the name of the file to be transferred");
				}
				break;

			case HELP:
				if (tempArray.length > 1) {
					for (int i = 0; i < HELP_MESSAGE.length; i++) {
						if (HELP_MESSAGE[i].startsWith(tempArray[1])) {
							System.out.println(HELP_MESSAGE[i]);
						}
					}
				} else {
					for (int i = 0; i < HELP_MESSAGE.length; i++) {
						System.out.println(HELP_MESSAGE[i]);
					}
				}
				break;

			case PASSIVE:
				if (isPassive) {

					InetAddress address = InetAddress.getLocalHost();
					tempHost = address.toString().replace(".", ",")
							.substring(7);
					int n1 = tempPort / 256;
					int n2 = tempPort % 256;
					tempHost = tempHost + "," + n1 + "," + n2;

					ftpCommand = "PORT " + tempHost + "\n";
					tempHost = ftpCommand;
					bufferCommand = ftpCommand.getBytes();
					output.write(bufferCommand);
					output.flush();

					while (true) {
						message = br.readLine();
						if (message != null && message.length() > 3) {
							if (message.startsWith("200")
									&& message.charAt(3) != '-') {
								System.out.println(message);
								break;
							}
						}
					}
					;
				} else {
					isPassive = true;
				}
				break;

			case PWD:
				ftpCommand = "PWD " + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out
							.println("****Print current working directory****");
				}
				printMessage();
				break;

			case QUIT:
				ftpCommand = "QUIT " + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();

				if (debug) {
					System.out.println("****Quit****");
				}
				printMessage();
				eof = true;
				break;

			case USER:
				userName = tempArray[1];

				ftpCommand = "USER " + userName + "\n";
				bufferCommand = ftpCommand.getBytes();
				output.write(bufferCommand);
				output.flush();
				message = br.readLine();
				System.out.println(message);

				if (message.startsWith("331")) {
					System.out.println("Enter Password: ");

					char[] getPassword = null;
					String sendPassword = "";
					Console con = System.console();
					getPassword = con.readPassword();
					sendPassword = new String(getPassword);
					ftpCommand = "PASS " + sendPassword + "\n";
					bufferCommand = ftpCommand.getBytes();
					output.write(bufferCommand);
					output.flush();
					message = br.readLine();
					System.out.println(message);
					if (message.startsWith("530")) {
						break;
					}
					readFromServerStream(br);
				}
				break;

			default:
				System.out.println("Invalid command");
			}
		} catch (IOException e) {
			System.out.println("IOException: ");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Writes the data into the local directory during transfer
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private void writeToFile(String fileName) throws IOException {
		int i = 0;
		byte[] buffer = new byte[512];
		File file = new File(fileName);

		// Writes the data to a local file
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		}

		FileOutputStream out = new FileOutputStream(file);

		while ((i = dataInput.read(buffer)) != -1) {
			byte[] temp = new byte[i];
			System.arraycopy(buffer, 0, temp, 0, i);
			out.write(temp);
			out.flush();
		}
	}

	/**
	 * Read commands and execute commands entered from the keyboard. The user
	 * may specify the address of the server from the command line.
	 * 
	 * @param args
	 *            command line arguments (optional host and port for server)
	 */
	public static void main(String args[]) {

		FTP obj = new FTP();

		String argv[] = { "", "" };

		if (args.length != 1) {
			System.err.println("Usage:  java FTP server");
			System.exit(1);
		}

		host = args[0];
		obj.connect(host, DEFAULT_PORT);

		// Command line is done - accept commands
		do {
			int cmd = -1;
			System.out.print(PROMPT);
			argv = obj.readInput();

			if (argv[0].equals("EOF")) {
				eof = true;
			}
			// What command was entered?
			for (int i = 0; i < COMMANDS.length && cmd == -1; i++) {
				if (COMMANDS[i].equalsIgnoreCase(argv[0])) {
					cmd = i;
				}
			}
			obj.execute(cmd, argv);
		} while (!eof);
	}
}
