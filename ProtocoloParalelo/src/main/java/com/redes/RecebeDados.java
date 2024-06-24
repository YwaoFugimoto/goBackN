/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redes;

/**
 * @author flavio
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;


public class RecebeDados extends Thread {

    private final int portaLocalReceber = 2001;
    private final int portaLocalEnviar = 2002;
    private final int portaDestino = 2003;


    private void enviaAck(boolean fim) {

        try {
            InetAddress address = InetAddress.getByName("localhost");
            try (DatagramSocket datagramSocket = new DatagramSocket(portaLocalEnviar)) {
                String sendString = fim ? "F" : "A";

                byte[] sendData = sendString.getBytes();

                DatagramPacket packet = new DatagramPacket(
                        sendData, sendData.length, address, portaDestino);

                datagramSocket.send(packet);
            }
        } catch (SocketException ex) {
            Logger.getLogger(RecebeDados.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RecebeDados.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() { // Inicia contagem de timeOut?
        try {
            DatagramSocket serverSocket = new DatagramSocket(portaLocalReceber);
            byte[] receiveData = new byte[1400];
            try (FileOutputStream fileOutput = new FileOutputStream("saida")) {
                boolean fim = false;
                while (!fim) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    System.out.println("dado recebido");

                    byte[] tmp = receivePacket.getData();

                    Random rand = new Random();
                    double double_random = rand.nextDouble();
                    System.out.println("Valor aleatorio = " + double_random);

                    if (double_random <= 0.6) { // variavel aleatoria 0.6 > recebe
                        System.out.println("Perda do pacote.");
                        //probabilidade de 60% de perder
                        //gero um numero aleatorio contido entre [0,1]
                        //se numero cair no intervalo [0, 0,6]
                        //significa perda, logo, você não envia ACK
                        //para esse pacote, e não escreve ele no arquivo saida.
                        //se o numero cair no intervalo [0,6, 1,0]
                        //assume-se o recebimento com sucesso.

                    } else {
                        for (int i = 0; i < tmp.length; i = i + 4) {
                            int dados = ((tmp[i] & 0xff) << 24) + ((tmp[i + 1] & 0xff) << 16) + ((tmp[i + 2] & 0xff) << 8) + ((tmp[i + 3] & 0xff));

                            if (dados == -1) {
                                fim = true;
                                break;
                            }
                            fileOutput.write(dados);
                        }
                        enviaAck(fim);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Excecao: " + e.getMessage());

        }
    }
}
