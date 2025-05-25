// JOGO DA VELHA: deve enviar jogadas ao servidor e reagir a jogadas do oponente (interface)
package jogo;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
  
class jogoDaVelha extends JFrame implements ActionListener {
    private PrintStream os;
    private String nome;
    public boolean jogador1 = false; // indica a vez do jogador 1
    private char simbolo1; // simbolo do jogador1
    private char simbolo2; // simbolo do jogador 2

    JButton a1, a2, a3, b1, b2, b3, c1, c2, c3; // botões
    JPanel boardPanel;
    JLabel statusLabel; // status do jogo

    public jogoDaVelha(PrintStream os, String nome, boolean jogador1) {
        super("Jogo da Velha " + nome);

        this.os = os;
        this.nome = nome;
        this.jogador1 = jogador1; // define se é a sua vez baseado em quem começa

        // define os símbolos com base em quem começa
        if(jogador1) {
            simbolo1 = 'X';
            simbolo2 = 'O';
        }
        else {
            simbolo1 = 'O';
            simbolo2 = 'X';
        }
        
        // criando os botoes
        a1 = new JButton("");
        a2 = new JButton("");
        a3 = new JButton("");
        b1 = new JButton("");
        b2 = new JButton("");
        b3 = new JButton("");
        c1 = new JButton("");
        c2 = new JButton("");
        c3 = new JButton("");

        // aparencia inicial de cada botao
        a1.setHorizontalAlignment(JTextField.CENTER);
        a1.setFont(new Font("Arial", Font.BOLD, 40));
        a2.setHorizontalAlignment(JTextField.CENTER);
        a2.setFont(new Font("Arial", Font.BOLD, 40));
        a3.setHorizontalAlignment(JTextField.CENTER);
        a3.setFont(new Font("Arial", Font.BOLD, 40));
        b1.setHorizontalAlignment(JTextField.CENTER);
        b1.setFont(new Font("Arial", Font.BOLD, 40));
        b2.setHorizontalAlignment(JTextField.CENTER);
        b2.setFont(new Font("Arial", Font.BOLD, 40));
        b3.setHorizontalAlignment(JTextField.CENTER);
        b3.setFont(new Font("Arial", Font.BOLD, 40));
        c1.setHorizontalAlignment(JTextField.CENTER);
        c1.setFont(new Font("Arial", Font.BOLD, 40));
        c2.setHorizontalAlignment(JTextField.CENTER);
        c2.setFont(new Font("Arial", Font.BOLD, 40));
        c3.setHorizontalAlignment(JTextField.CENTER);
        c3.setFont(new Font("Arial", Font.BOLD, 40));

        // adicionando os botões ao painel com gridlayout
        boardPanel = new JPanel(new GridLayout(3, 3));
        boardPanel.add(a1);
        boardPanel.add(a2);
        boardPanel.add(a3);
        boardPanel.add(b1);
        boardPanel.add(b2);
        boardPanel.add(b3);
        boardPanel.add(c1);
        boardPanel.add(c2);
        boardPanel.add(c3);

        // criando o label de status
        statusLabel = new JLabel("Aguardando início do jogo...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // configurando o layout principal para incluir o status
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // adicionando listeners aos botões
        a1.addActionListener(this); // espera clicar no botao para ser feita a operacao
        a2.addActionListener(this);
        a3.addActionListener(this);
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        c1.addActionListener(this);
        c2.addActionListener(this);
        c3.addActionListener(this);
        
        a1.setActionCommand("a1");
        a2.setActionCommand("a2");
        a3.setActionCommand("a3");
        b1.setActionCommand("b1");
        b2.setActionCommand("b2");
        b3.setActionCommand("b3");
        c1.setActionCommand("c1");
        c2.setActionCommand("c2");
        c3.setActionCommand("c3");
        
        // configura o estado inicial dos botões
        atualizaEstadoBotoes();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500); // tamanho da janela

        setVisible(true);
    }

    public void actionPerformed (ActionEvent evento) {
        if(jogador1) {
            JButton botaoClicado = (JButton) evento.getSource();
            if (botaoClicado.getText().isEmpty()) {
                botaoClicado.setText(String.valueOf(simbolo1));
                botaoClicado.setEnabled(false);
                
                // envia jogada ao servidor
                os.println(botaoClicado.getActionCommand());
                
                // atualiza turno
                jogador1 = false;
                atualizaEstadoBotoes();
                atualizaStatusJogo();
            }
        }
    }

    public void atualizaJogada(String jogador, String jogada) {
        // variável que indica o botão que foi clicado
        JButton botao = getPosicaoBotao(jogada); 
    
        // se é uma jogada do jogador 2
        if(!jogador.equals(nome) && botao != null && botao.getText().isEmpty()) {
            botao.setText(String.valueOf(simbolo2));
            botao.setEnabled(false);
            
            // agora é a vez do jogador 1
            jogador1 = true;
            atualizaEstadoBotoes();
            atualizaStatusJogo();
        }
    }

    private JButton getPosicaoBotao(String posicao) {
        JButton botao = null;
        switch(posicao) {
            case "a1":
                botao = a1;
                break;
            case "a2":
                botao = a2;
                break;
            case "a3":
                botao = a3;
                break;
            case "b1":
                botao = b1;
                break;
            case "b2":
                botao = b2;
                break;
            case "b3":
                botao = b3;
                break;
            case "c1":
                botao = c1;
                break;
            case "c2":
                botao = c2;
                break;
            case "c3":
                botao = c3;
                break;
            default:
                botao = null;
                break;
        }
        
        return botao; // retorna qual botao foi clicado
    }

    private void atualizaEstadoBotoes() {
        // habilita ou desabilita todos os botões vazios dependendo de quem está jogando
        ArrayList<JButton> botoes = new ArrayList<JButton>();
        botoes.add(a1);
        botoes.add(a2);
        botoes.add(a3);
        botoes.add(b1);
        botoes.add(b2);
        botoes.add(b3);
        botoes.add(c1);
        botoes.add(c2);
        botoes.add(c3);
        
        for(JButton botao : botoes) {
            if(botao.getText().isEmpty()) {
                botao.setEnabled(jogador1); // conforme o estado do jogador, deixa disponível ou não
            }
        }
    }
    
    private void atualizaStatusJogo() {
        if(jogador1) {
            statusLabel.setText("Sua vez de jogar (" + simbolo1 + ")");
        }
        else {
            statusLabel.setText("Aguardando jogada do oponente (" + simbolo2 + ")");
        }
    }
    
    public void notificaResultado(String resultado) {
        // desabilita todos os botões ao final do jogo
        ArrayList<JButton> botoes = new ArrayList<JButton>();
        botoes.add(a1);
        botoes.add(a2);
        botoes.add(a3);
        botoes.add(b1);
        botoes.add(b2);
        botoes.add(b3);
        botoes.add(c1);
        botoes.add(c2);
        botoes.add(c3);
        
        for(JButton botao : botoes) {
            botao.setEnabled(false);
        }
        
        statusLabel.setText(resultado);
        JOptionPane.showMessageDialog(this, resultado, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }
}