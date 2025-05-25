// CLIENTE DA VELHA: conecta-se ao servidor e envia/recebe jogadas

package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class clienteDaVelha {
    private Socket socket;
    private PrintStream saida;
    private BufferedReader entrada;
    private jogoDaVelha jogo;
    private boolean jogoEmAndamento = false;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        System.out.print("Digite seu nome: ");
        String nome = teclado.nextLine();
        System.out.print("Digite o servidor: ");
        String servidor = teclado.nextLine();
        System.out.print("Digite a porta: ");
        int porta = teclado.nextInt();

        teclado.close();
            
        clienteDaVelha cliente = new clienteDaVelha(); // inicia o cliente
        cliente.conectar(servidor, porta, nome);
    }

    public boolean conectar(String servidor, int porta, String nome) {
        try {
            // conecta ao servidor
            socket = new Socket(servidor, porta);
            saida = new PrintStream(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // envia o nome do jogador
            saida.println(nome);
            
            // inicia thread para receber mensagens do servidor
            Thread threadRecepcao = new Thread(() -> processaMensagensServidor(nome)); // inicia a thread chamando o método e substituindo a run()
            threadRecepcao.start();
            
            return true;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Erro ao conectar: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void processaMensagensServidor(String meuNome) {
        try {
            String mensagem;
            while((mensagem = entrada.readLine()) != null) {
                final String mensagemFinal = mensagem;
                
                // processa a mensagem recebida
                SwingUtilities.invokeLater(() -> {
                    if(mensagemFinal.startsWith("INICIAR:")) {
                        // inicia o jogo
                        boolean primeiroJogador = Boolean.parseBoolean(mensagemFinal.substring(8));
                        jogo = new jogoDaVelha(saida, meuNome, primeiroJogador);
                        jogoEmAndamento = true;
                    }
                    else if(mensagemFinal.startsWith("JOGADA:")) {
                        // recebeu jogada do oponente
                        String[] partes = mensagemFinal.split(":");
                        if(partes.length == 3) {
                            String jogador = partes[1];
                            String jogada = partes[2];
                            
                            if(jogo != null) {
                                jogo.atualizaJogada(jogador, jogada);
                                
                                // verifica se o jogo terminou após a jogada
                                // verificaFimDeJogo();
                            }
                        }
                    }
                    else if(mensagemFinal.startsWith("FIM:")) {
                        // recebeu notificação de fim de jogo
                        String resultado = mensagemFinal.substring(4);
                        if(jogo != null) {
                            jogo.notificaResultado(resultado);
                            jogoEmAndamento = false;
                        }
                    }
                });
            }
        }
        catch(IOException e) {
            if(jogoEmAndamento) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Conexão com o servidor perdida!", 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                });
            }
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}