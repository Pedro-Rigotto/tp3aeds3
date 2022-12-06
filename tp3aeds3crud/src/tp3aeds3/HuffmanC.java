package tp3aeds3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;

/* Huffman coding , decoding */

public class HuffmanC {
    static final boolean readFromFile = false;
    static final boolean newTextBasedOnOldOne = false;
    static RandomAccessFile raf,teste;

    static PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    static TreeMap<Character, String> codes = new TreeMap<>();
    static String text = "";
    static String encoded = "";
    static String decoded = "";
    static int ASCII[] = new int[12558];

   
    public static boolean handleNewText(Scanner scanner,RandomAccessFile arq) throws IOException {
        arq.seek(0);
        while(arq.getFilePointer() < arq.length() -1){//Enquanto o arquivo nao chegar ao fim
            text += arq.readChar();//ler todos os caracteres lidos que possuem
        }

            ASCII = new int[40000];//Array para armazenar a posicao de cada caractere
            //Inicia as variaveis e os NOS
            nodes.clear();
            codes.clear();
            encoded = "";
            decoded = "";
            calculateCharIntervals(nodes);//Calcula a frequencia de cada char que aparece no arquivo
            buildTree(nodes);//Carrega a arvore com todos os NOS null
            generateCodes(nodes.peek(), "");//Gera o codigo do caminho ate chegar na sequencia de bits

            raf = new RandomAccessFile("dados/contasHuffman.db", "rw");
            encodeText(raf);//Escreve em outro arquivo o arquivo original codificado
            decodeText(arq);//Sobreecreve o arquivo original com a decodificacao
             
            return false;

    }


    private static void decodeText(RandomAccessFile arq) throws IOException {
        decoded = "";
        Node node = nodes.peek();
        for (int i = 0; i < encoded.length(); ) {
            Node tmpNode = node;
            while (tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {//enquanto o arquivo armazenado dentro da string encoded nao chegar ao fim e a direita e esquerda do NO for diferente de null
                if (encoded.charAt(i) == '1')//se o NO na posicao passado for igual a 1 passa a ser o NO da direita 
                    tmpNode = tmpNode.right;
                else tmpNode = tmpNode.left;// se não o No na posicao passada for igual a 0 passa a ser o NO da esquerda
                i++;
            }
            if (tmpNode != null)
                if (tmpNode.character.length() == 1)
                    decoded += tmpNode.character;
                else
                    System.out.println("Input not Valid");

        }
        arq.seek(0);//coloca o ponteiro no arquivo na primeira posicao
        for(int i = 0; i < decoded.length() ;i++)    arq.writeChar(decoded.charAt(i));//enquanto o arquivo decodificado dentro da String decoded não chegar ao fim sobreescreve o arquivo original
    }

    public static void encodeText(RandomAccessFile raf) throws IOException {
        encoded = "";
        for (int i = 0; i < text.length(); i++)//enquanto o tamanho do arquivo nao chega ao fim 
            encoded += codes.get(text.charAt(i));//encoded recebe o valor gerado pelo caminho da arvore
        raf.writeUTF(encoded);
    }//Obs:Os dados estão armazenados dentro da string text

    private static void buildTree(PriorityQueue<Node> vector) {//Carrega a arvore com todos os NOS null
        while (vector.size() > 1)
            vector.add(new Node(vector.poll(), vector.poll()));
    }

    private static void calculateCharIntervals(PriorityQueue<Node> vector) {//Calcula a probabilidade(frequência) de cada char do texto

        for (int i = 0; i < text.length(); i++){
            ASCII[text.charAt(i)]++;
        }
            

        for (int i = 0; i < ASCII.length; i++)
            if (ASCII[i] > 0) {
                vector.add(new Node(ASCII[i] / (text.length() * 1.0), ((char) i) + ""));
            }
    }

    private static void generateCodes(Node node, String s) {
        if (node != null) {//se o codigo passado for diferente de null
            if (node.right != null)//se o no da direita nao estiver vazio recebe 1 por ser a direita da arvore
                generateCodes(node.right, s + "1");

            if (node.left != null)//se o no da esquerda nao estiver vazio recebe 1 por ser a direita da arvore
                generateCodes(node.left, s + "0");

            if (node.left == null && node.right == null)
                codes.put(node.character.charAt(0), s);
        }
    }//Obs:A pesquisa do No a direita recebe 1 e o da esquerda recebe 0
}

class Node {
    Node left, right;
    double value;
    String character;

    public Node(double value, String character) {
        this.value = value;
        this.character = character;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        character = left.character + right.character;
        if (left.value < right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }
}