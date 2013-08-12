/**
 * filename: TFTPReader.java
 * 
 * Version: 1.0
 * 
 * Revisions: None
 * 
 */

/**
 *
 * Program to implement a TFTP client that allows you to download files from TFTP server
 * 
 * @author GANESH CHANDRASEKARAN
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * The class reads and downloads files from TFTP server
 */

public class TFTPReader {

	public void usage() {
		System.err
				.println("Usage: [java] TFTPreader [netascii|octet]  tftp-host file");
	}

	public void download(String typeOfTransfer, String host, String fileName) {
		try {

			InetAddress server = InetAddress.getByName(host);

			DatagramSocket sock = new DatagramSocket();

			DatagramPacket sendPack, receivePack, ack;

			// The file name and mode of transfer are taken into a byte array

			byte[] fileName1 = fileName.getBytes();
			byte[] typeOfTransfer1 = typeOfTransfer.getBytes();

			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			// Creates an object of the output stream to write the data into
			// the file
			FileOutputStream out = new FileOutputStream(file, true);

			// We create a ByteBuffer and fill it with the opcode, filename,
			// mode of transfer in the format specified in RFC

			ByteBuffer requestToRead = ByteBuffer.allocate(2 + fileName1.length
					+ 1 + typeOfTransfer1.length + 1);

			requestToRead.put((byte) 0);
			requestToRead.put((byte) 1);
			requestToRead.put(fileName1);
			requestToRead.put((byte) 0);
			requestToRead.put(typeOfTransfer1);
			requestToRead.put((byte) 0);
			requestToRead.flip();

			byte[] request = requestToRead.array();

			// Sends the request datagram packet at port 69
			sendPack = new DatagramPacket(request, request.length, server, 69);
			sock.send(sendPack);
			sock.setSoTimeout(100000);

			byte[] receivePacket = new byte[516];

			int flag = 0;
			/*
			 * The while loop receives the packet from the TFTP server in the
			 * order it is sent and acknowledges the receipt of each packet in
			 * order to receive the next packet
			 */
			while (flag == 0) {
				receivePack = new DatagramPacket(receivePacket,
						receivePacket.length);

				sock.receive(receivePack);
				/*
				 * The following if condition checks if the packet contains
				 * error number and message
				 */

				if (receivePacket[1] == (byte) 5) {

					int i = 2, j = 0;
					byte[] errorNumber = new byte[2];
					errorNumber[j++] = receivePacket[i++];
					errorNumber[j++] = receivePacket[i++];
					byte[] errorMsg = new byte[receivePacket.length - 5];
					for (int k = 0; k < errorMsg.length; k++) {
						errorMsg[k] = receivePacket[i++];
					}
					/*
					 * If the error is for file not found, then the created file
					 * is deleted
					 */
					if (errorNumber[0] == (byte) 0
							&& errorNumber[1] == (byte) 1) {
						file.delete();
					}

					String errorMessage = new String(errorMsg);
					System.out.println("Error Number: " + errorNumber[0]
							+ errorNumber[1]);
					System.out.println("Error Message: " + errorMessage);
					flag = 1;
				}
				/*
				 * If the packet received contains the data, then the data is
				 * extracted by excluding the opcode and block number and
				 * written into the file.
				 * 
				 * The block number is extracted and sent back with the
				 * acknowledgment packet to the port from which the packet was
				 * received
				 */
				if (receivePacket[1] == (byte) 3) {
					byte data[] = new byte[receivePack.getLength() - 4];
					for (int i = 0; i < receivePack.getLength() - 4; i++) {
						data[i] = receivePacket[i + 4];
					}

					out.write(data);

					byte[] blockNumber = new byte[2];
					blockNumber[0] = receivePacket[2];
					blockNumber[1] = receivePacket[3];

					byte[] ACK = new byte[4];
					ACK[0] = (byte) 0;
					ACK[1] = (byte) 4;
					ACK[2] = blockNumber[0];
					ACK[3] = blockNumber[1];

					ack = new DatagramPacket(ACK, ACK.length, server,
							receivePack.getPort());
					sock.send(ack);

					if (receivePack.getLength() < 516) {
						flag = 1;
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		String typeOfTransfer, host, fileName;
		TFTPReader obj = new TFTPReader();

		// The if condition is used to check for valid number of arguments in
		// the command line
		if (args.length != 3) {
			obj.usage();
		} else {
			typeOfTransfer = args[0];
			host = args[1];
			fileName = args[2];
			obj.download(typeOfTransfer, host, fileName);
		}
	}
}