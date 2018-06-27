package br.com.gtheil.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Felipe
 */
public class ServerService {
    
    private ServerSocket serverSocket; // variável que contém a porta de conexão do servidor
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnline = new HashMap<String, ObjectOutputStream>(); // lista que armazena todos os clientes que se conectarem ao servidor
    
     /**
      * Cria uma conexão exclusiva com um cliente através de um socket, com thread dedicada
      * até que um novo cliente seja conectado, então uma segunda conexão dedicada é criada
      * e assim por diante
      */
    public ServerService() {
        try {
            serverSocket = new ServerSocket(5555); // atribui a porta 5555 à conexão
            
            // enquanto o servidor estiver rodando
            while(true) {
                socket = serverSocket.accept(); // inicializa o socket com a conexão da variável serverSocket
                
                new Thread(new ListenerSocket(socket)).start(); // inicializa uma nova thread para executar o método de escuta do socket
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // implementa interface Runnable para que possa ser executada por threads
    private class ListenerSocket implements Runnable{

        private ObjectOutputStream output; // variável que executa o envio de mensagens do servidor
        private ObjectInputStream input; // variável que recebe as mensagens da aplicação cliente
        
        public ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream()); // inicializa a variável de saída do socket recebido como parâmetro
                this.input = new ObjectInputStream(socket.getInputStream()); // Inicializa a variável de entrada do socket recebido como parâmetro
            } catch (IOException ex) {
                Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    
    }
}
