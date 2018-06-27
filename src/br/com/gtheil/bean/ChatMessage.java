package br.com.gtheil.bean;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/**
 *
 * @author Felipe
 */
public class ChatMessage implements Serializable {
    // lista enumerada de ações que podem ser executadas ao cliente enviar uma mensagem ao servidor ou vice-versa
    public enum Action {
        CONNECT, DISCONNECT, SEND_ONE, SEND_ALL, USERS_ONLINE
    }
    
    private String name; // nome do cliente que está enviando a mensagem
    private String text; // conteúdo da mensagem
    private String namePrivate; // nome do cliente que receberá uma mensagem privada
    private Set<String> setOnline = new HashSet<String>(); // lista de clientes atualmente online
    private Action action; // instancia a lista

    /**
     * getters e setters das variáveis da mensagem
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNamePrivate() {
        return namePrivate;
    }

    public void setNamePrivate(String namePrivate) {
        this.namePrivate = namePrivate;
    }

    public Set<String> getSetOnline() {
        return setOnline;
    }

    public void setSetOnline(Set<String> setOnline) {
        this.setOnline = setOnline;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
    
}
