// SERVIDOR DA VELHA: recebe conexões dos dois jogadores e repassa mensagens entre eles
package jogo;

import java.io.*;
import java.net.*;
import java.util.*;


public class servidorDaVelha {
    // lista de jogadores
    private static ArrayList<Servindo> jogadores = new ArrayList<>();
    public static void main(String[] args) {
        try {
            ServerSocket servidor = new ServerSocket(1234);
            System.out.println("Servidor do Jogo da Velha iniciado na porta " + 1234);
            System.out.println("Aguardando jogadores...");

            while(true) {
                Socket socket = servidor.accept();
                
                // cria um gerenciador para o novo cliente
                Servindo gerenciador = new Servindo(socket);
                jogadores.add(gerenciador);
                
                // inicia o jogo com 2 jogadores
                if(jogadores.size() == 2) {
                    System.out.println("Dois jogadores conectados. Iniciando jogo...");
                    iniciaJogo();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void iniciaJogo() {
        boolean primeiroJogadorComeca = new Random().nextBoolean();
        
        Servindo jogador1 = jogadores.get(0);
        Servindo jogador2 = jogadores.get(1);
        
        // seta os jogadores
        jogador1.setOponente(jogador2);
        jogador2.setOponente(jogador1);

        // cria um controlador de jogo compartilhado
        controladorJogo controlador = new controladorJogo(jogador1, jogador2);
        jogador1.setControladorJogo(controlador);
        jogador2.setControladorJogo(controlador);
        
        // diz quem começa
        jogador1.enviaMensagem("INICIAR:" + (primeiroJogadorComeca ? "true" : "false"));
        jogador2.enviaMensagem("INICIAR:" + (!primeiroJogadorComeca ? "true" : "false"));
        
        // inicia as threads dos gerenciadores
        new Thread(jogador1).start();
        new Thread(jogador2).start();
        
        // limpa a lista para um novo jogo
        jogadores.clear();
    }
}

// classe que controla o estado do jogo no servidor
class controladorJogo {
    private char[][] tabuleiro = new char[3][3];
    private Servindo jogador1;
    private Servindo jogador2;
    private boolean jogoAtivo = true;
    
    public controladorJogo(Servindo jogador1, Servindo jogador2) {
        this.jogador1 = jogador1;
        this.jogador2 = jogador2;

        // inicializa com tabuleiro vazio
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                tabuleiro[i][j] = ' ';
            }
        }
    }
    
    public synchronized void processarJogada(String jogada, Servindo jogadorAtual) {
        // caso não esteja ocorrendo o jogo
        if(!jogoAtivo) return;
        
        // converte jogada nas posição no tabuleiro
        int linha = jogada.charAt(0) - 'a'; // a=0, b=1, c=2
        int coluna = jogada.charAt(1) - '1'; // 1=0, 2=1, 3=2
        
        if(linha >= 0 && linha < 3 && coluna >= 0 && coluna < 3 && tabuleiro[linha][coluna] == ' ') {
            // determina símbolo baseado em qual jogador fez a jogada
            char simbolo = (jogadorAtual == jogador1) ? 'X' : 'O';
            tabuleiro[linha][coluna] = simbolo;
            
            // passa a jogada 
            Servindo oponente = (jogadorAtual == jogador1) ? jogador2 : jogador1;
            oponente.enviaMensagem("JOGADA: " + jogadorAtual.getNome() + ":" + jogada);
            
            // verifica se o jogo terminou
            verificaFimDeJogo();
        }
    }
    
    private void verificaFimDeJogo() {
    char simboloVencedor = verificaVencedor();
    
    if(simboloVencedor != ' ') {
        // converte o símbolo para o jogador correspondente
        Servindo jogadorVencedor;
        if(simboloVencedor == 'X') {
            jogadorVencedor = jogador1;
        }
        else {
            jogadorVencedor = jogador2;
        }
        
        String mensagem = "Acabou o jogo! Vencedor: " + jogadorVencedor.getNome();
        finalizaJogo(mensagem);
    }
    else if(verificaEmpate()) {
        // empate
        finalizaJogo("Acabou o jogo! EMPATE!");
    }
}
    
    private char verificaVencedor() {
        // verifica linhas
        for(int i = 0; i < 3; i++) {
            if(tabuleiro[i][0] != ' ' && tabuleiro[i][0] == tabuleiro[i][1] && tabuleiro[i][1] == tabuleiro[i][2]) {
                return tabuleiro[i][0]; 
            }
        }
        
        // verifica colunas
        for(int j = 0; j < 3; j++) {
            if(tabuleiro[0][j] != ' ' && tabuleiro[0][j] == tabuleiro[1][j] && tabuleiro[1][j] == tabuleiro[2][j]) {
                return tabuleiro[0][j];
            }
        }
        
        // verifica diagonais
        if(tabuleiro[0][0] != ' ' && tabuleiro[0][0] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][2]) {
            return tabuleiro[0][0];
        }
        else if(tabuleiro[0][2] != ' ' && tabuleiro[0][2] == tabuleiro[1][1] && tabuleiro[1][1] == tabuleiro[2][0]) {
            return tabuleiro[0][2];
        }
        
        return ' '; // sem vencedor
    }
    
    private boolean verificaEmpate() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(tabuleiro[i][j] == ' ') {
                    return false; // ainda há espaços vazios
                }
            }
        }
        return true; // tabuleiro cheio e sem vencedor
    }
    
    private void finalizaJogo(String resultado) {
        jogoAtivo = false;
        
        // envia resultado para ambos os jogadores
        jogador1.enviaMensagem("FIM:" + resultado);
        jogador2.enviaMensagem("FIM:" + resultado);
        
        // aguarda um pouco para garantir que as mensagens sejam enviadas
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // encerra as conexões
        jogador1.encerraJogo();
        jogador2.encerraJogo();
        
        System.out.println("Jogo finalizado: " + resultado);
    }
}

class Servindo implements Runnable {
    private Socket socket;
    private PrintWriter saida;
    private BufferedReader entrada;
    private String nome;
    private Servindo oponente;
    private boolean jogoAtivo = true;
    private controladorJogo controladorJogo;
    
    public Servindo(Socket socket) {
        this.socket = socket;
        try {
            this.saida = new PrintWriter(socket.getOutputStream(), true);
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // lê o nome do jogador
            this.nome = entrada.readLine();
            System.out.println("Jogador conectado: " + nome);
            
        } catch (IOException e) {
            System.out.println("Erro ao inicializar gerenciador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // define oponente
    public void setOponente(Servindo oponente) {
        this.oponente = oponente;
    }
    
    // seta o controlador do jogo
    public void setControladorJogo(controladorJogo controlador) {
        this.controladorJogo = controlador;
    }
    
    // pega o nome do jogador
    public String getNome() {
        return nome;
    }
    
    public void enviaMensagem(String mensagem) {
        if(saida != null) {
            saida.println(mensagem);
        }
    }
    
    public void encerraJogo() {
        jogoAtivo = false;
    }
    
    @Override
    public void run() {
        try {
            // processa as mensagens do cliente
            String mensagem;

            // enquanto estiver o jogo ativo e houver mensagens
            while(jogoAtivo && (mensagem = entrada.readLine()) != null) { 
                System.out.println("Mensagem de " + nome + ": " + mensagem);
                
                // se recebeu uma jogada, processa através do controlador
                if(mensagem.matches("[abc][123]")) {
                    controladorJogo.processarJogada(mensagem, this);
                }
            }
        }
        catch(IOException e) {
            System.out.println("Erro na comunicação com " + nome + ": " + e.getMessage());
        }
        finally {
            try {
                if(socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            System.out.println("Conexão com " + nome + " encerrada");
        }
    }
}