package br.com.gtheil.service;

import br.com.gtheil.bean.ChatMessage;
import br.com.gtheil.bean.ChatMessage.Action;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Felipe
 */
public class ServerService {

    private ServerSocket serverSocket; // variável que contém as informações de conexão do servidor
    private Socket socket; // socket do servidor, que escuta por requisições dos clientes
    private Map<String, ObjectOutputStream> mapOnline = new HashMap<String, ObjectOutputStream>(); // lista que armazena todos os clientes que se conectarem ao servidor

    /**
     * Cria uma conexão exclusiva com um cliente através de um socket, com
     * thread dedicada até que um novo cliente seja conectado, então uma segunda
     * conexão dedicada é criada e assim por diante
     */
    public ServerService() {
        try {
            serverSocket = new ServerSocket(5555); // atribui a porta 5555 à conexão

            System.out.println("Servidor iniciado com sucesso!");

            // enquanto o servidor estiver rodando
            while (true) {
                socket = serverSocket.accept(); // inicializa o socket do servidor

                new Thread(new ListenerSocket(socket)).start(); // inicializa uma nova thread com o socket ouvinte
            }

        } catch (IOException ex) {
            Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // implementa interface Runnable para que possa ser executada por threads
    private class ListenerSocket implements Runnable {

        private ObjectOutputStream output; // objeto que executa o envio de mensagens do servidor
        private ObjectInputStream input; // objeto que recebe as mensagens da aplicação cliente

        public ListenerSocket(Socket socket) { // socket que fica na escuta
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream()); // inicializa a variável de saída do socket recebido como parâmetro
                this.input = new ObjectInputStream(socket.getInputStream()); // Inicializa a variável de entrada do socket recebido como parâmetro
            } catch (IOException ex) {
                Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Método executado por cada thread
        @Override
        public void run() {
            ChatMessage message = null; // variável que recebe a mensagem enviada pelo cliente

            try {
                while ((message = (ChatMessage) input.readObject()) != null) { // converte o retorno do método readObject ao formato de um objeto da classe ChatMessage
                    Action action = message.getAction(); // instância de um objeto da classe Action, que recebe a ação da mensagem que está sendo enviada

                    if (action.equals(Action.CONNECT)) { // caso seja um pedido de conexão
                        boolean isConn = connect(message, output); // é chamado o método de conexão
                        if (isConn == true) { // caso a conexão seja bem-sucedida
                            mapOnline.put(message.getName(), output); // adiciona o nome do usuário à lista de usuários online
                            sendOnline(); // chama o método de envio da lista de usuários
                        }
                    } else if (action.equals(Action.DISCONNECT)) { // caso seja um pedido de desconexão do chat
                        disconnect(message, output); // é chamado o método de desconexão
                        sendOnline(); // chama o método de envio da lista de usuários
                        return; // força a saída do loop
                    } else if (action.equals(Action.SEND_ONE)) { // caso seja um envio de mensagem privada
                        sendOne(message); // chama o método de envio de mensagem
                    } else if (action.equals(Action.SEND_ALL)) { // caso seja um envio de mensagem a todos os usuários
                        sendAll(message); // chama o método de envio de mensagem a todos os usuários
                    }
                }
            } catch (IOException ex) {
                ChatMessage cm = new ChatMessage(); // instancia um novo objeto ChatMessage
                cm.setName(message.getName()); // atribui a nova instancia o nome do usuário da mensagem original
                disconnect(cm, output); // desconecta o socket
                sendOnline(); // chama o método de envio da lista de usuários
                System.out.println(message.getName() + " desconectou-se.");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Método de conexão ao servidor, que recebe uma mensagem enviada pelo cliente e o objeto de saída do servidor
    private boolean connect(ChatMessage message, ObjectOutputStream output) {
        // caso não hajam usuários conectados
        if (mapOnline.size() == 0) {
            message.setText("YES"); // cria mensagem de resposta ao cliente informando que ele foi conectado
            send(message, output); // envia a mensagem ao cliente
            return true; // retorna que a conexão foi bem-sucedida
        }

        if (mapOnline.containsKey(message.getName())) { // caso já exista um cliente com este nome
            message.setText("NO"); // cria mensagem de resposta ao cliente informando que ele não foi conectado
            send(message, output); // envia a mensagem ao cliente
            return false; // retorna que a conexão foi recusada
        } else { // caso o registro atual não tenha o mesmo nome de algum usuário já online
            message.setText("YES"); // cria mensagem de resposta ao cliente informando que ele foi conectado
            send(message, output); // envia a mensagem ao cliente
            return true; // retorna que a conexão foi bem-sucedida
        }
    }

    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        mapOnline.remove(message.getName()); // remove o cliente desconectado da lista de usuários online através do seu nome

        message.setText("desconectou-se."); // cria mensagem de resposta aos demais clientes informando que o cliente desconectou-se do servidor
        message.setAction(Action.SEND_ONE); // seta a ação da mensagem como sendo para enviar uma mensagem
        sendAll(message); // envia a mensagem a todos os usuários

        System.out.println(message.getName() + " desconectou-se."); // teste
    }

    // Método de envio de mensagem
    private void send(ChatMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message); // o objeto de saída envia a mensagem
        } catch (IOException ex) {
            Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Método de envio de mensagem privada
    private void sendOne(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) { // percorre a lista de usuários online
            if (kv.getKey().equals(message.getNamePrivate())) { // caso o nome do registro atual for igual ao nome do usuário selecionado
                try {
                    kv.getValue().writeObject(message); // o objeto de saída envia a mensagem
                } catch (IOException ex) {
                    Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    // Método de envio de mensagem a todos usuários
    private void sendAll(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) { // percorre a lista de usuários online
            if (!kv.getKey().equals(message.getName())) { // caso o nome do registro atual seja diferente do nome do cliente que está enviando a mensagem
                message.setAction(Action.SEND_ONE); // seta a ação da mensagem para que seja enviada para cada cliente
                try {
                    System.out.println(message.getName() + ": " + message.getText());
                    kv.getValue().writeObject(message); // a mensagem é enviada
                } catch (IOException ex) {
                    Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void sendOnline() {
        Set<String> setNames = new HashSet<String>(); // lista que contém os usuários online a ser enviada
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) { // percorre a lista de usuários online
            setNames.add(kv.getKey()); // adiciona a lista que será enviada
        }

        ChatMessage message = new ChatMessage(); // nova instacia do objeto ChatMessage
        message.setAction(Action.USERS_ONLINE); // seta a ação da mensagem para ser requisição dos usuários online
        message.setSetOnline(setNames); // seta a lista de usuários para ser enviada

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) { // percorre a lista de usuários online
            message.setName(kv.getKey()); // adiciona o usuário atual à mensagem para ser enviada à lista
            try {
                kv.getValue().writeObject(message); // a mensagem é enviada
            } catch (IOException ex) {
                Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
