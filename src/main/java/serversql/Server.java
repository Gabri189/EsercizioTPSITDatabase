package serversql;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.*;

public class Server implements Runnable{ 	
  private static final int PORT = 3000;
	private static final File WEB_ROOT = new File("/home/cabox/workspace/EsercizioTPSITDatabase/file");
	private static final String DEFAULT_FILE = "index.html";
	private static final String FILE_NOT_FOUND = "404.html";
	private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
        private static final String fileJSON = "db/alunni.json";
        private static final String fileXML = "db/alunni.xml";
        private final String querySQL = "SELECT alunni.* FROM alunni";
        private final String urlSQL = "jdbc:mysql://";
        private final String serverNameSQL = "localhost";
        private final String portNumberSQL = ":3306/";
        private final String databaseNameSQL = "db_http";
        private final String userNameSQL = "root";
        private final String passwordSQL = "root";
        private final String connessioneSQL = urlSQL + serverNameSQL + portNumberSQL + databaseNameSQL;
        private Connection connessioneConDB = null;
        private Statement statementSQL = null;
        private ResultSet resultSetSQL = null;
	
	// verbose mode
	private static final boolean verbose = true;
	
	// Client Connection via Socket Class
	private Socket connect;
	
	public Server(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
                        // we listen until user halts server execution
			while (true) {
                            Server myServer = new Server(serverConnect.accept());
                            if (verbose)System.out.println("Connecton opened. (" + new Date() + ")");
                            // create dedicated thread to manage the client connection
                            Thread thread = new Thread(myServer);
                            thread.start();
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken();
			
			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				
				// we return the not supported file to the client
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//read content to return to client
				byte[] fileData = readFileData(file, fileLength);
					
				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
				
			} else {
				// GET or HEAD method
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}
        else if(fileRequested.equals("/" + fileXML))
        {
          connessioneConDB = DriverManager.getConnection(connessioneSQL, userNameSQL, passwordSQL);
          System.out.println("CONNESSIONE CON SQL STABILITA");
          statementSQL = connessioneConDB.createStatement();
          resultSetSQL = statementSQL.executeQuery(querySQL);
          Alunni p = new Alunni();
          while(resultSetSQL.next()) {
            p.add(new Alunno(resultSetSQL.getInt(1), resultSetSQL.getString(2), resultSetSQL.getString(3)));
            System.out.println("Valore inserito.\n");
          }
          XmlMapper xml = new XmlMapper();
          xml.writeValue(new File(WEB_ROOT + "/" + fileXML), p);
          File file = new File(WEB_ROOT + "/" + fileXML);
        }
        else if(fileRequested.equals("/" + fileJSON)) {
        ObjectMapper mapper = new ObjectMapper();
        connessioneConDB = DriverManager.getConnection(connessioneSQL, userNameSQL, passwordSQL);
        System.out.println("CONNESSIONE CON SQL STABILITA");
        statementSQL = connessioneConDB.createStatement();
        resultSetSQL = statementSQL.executeQuery(querySQL);
        Alunni p = new Alunni();
        while(resultSetSQL.next()) {
          p.add(new Alunno(resultSetSQL.getInt(1), resultSetSQL.getString(2), resultSetSQL.getString(3)));
          System.out.println("Valore inserito.\n");
        }
        mapper.writeValue(new File(WEB_ROOT + "/" + fileJSON), p);
        File file = new File(WEB_ROOT + "/" + fileJSON);
        }
        File file = new File(WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);
				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = readFileData(file, fileLength);
					
					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); // blank line between headers and content, very important !
					out.flush(); // flush character output stream buffer
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				
				if (verbose) {
					System.out.println("File " + fileRequested + " of type " + content + " returned");
				}
				
			}
			
		} catch (NoSuchFileException fnfe) {
                    try {
                            fileNotFound(out, dataOut, fileRequested);
                    } catch (IOException ioe) {
                            System.err.println("Error with file not found exception : " + ioe.getMessage());
                    }
			
		} catch (IOException ioe) {
                    System.err.println("Server error : " + ioe);
                }catch (SQLException sqlex) {
                    System.err.println("SQL error: " + sqlex);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileData = Files.readAllBytes(file.toPath());
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		return fileData;
	}
        
	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
                {
                    return "text/html";
                }
                else if (fileRequested.endsWith(".xml"))
                {
                    return "text/xml";
                }
                else if (fileRequested.endsWith(".json"))
                {
                    return "text/json";
                }
                else return "text/plain";
	}
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		if (verbose)System.out.println("File " + fileRequested + " not found");
	}
}