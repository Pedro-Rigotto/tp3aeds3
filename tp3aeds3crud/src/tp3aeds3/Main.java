package tp3aeds3;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.*;
import java.lang.Math;

/**
 * 
 * @author Pedro Corrêa Rigotto - Matrícula 762281, Henrique Padua França Lommes -  Matrícula 733377
 *
 */

public class Main {
    public static Scanner sc = new Scanner(System.in);

	private static ListaInvertida listaInvertida = new ListaInvertida();
	
	private static int numOp;
    
    
    public static long buscaId (RandomAccessFile arq, long comeco, int idDesejada) { // retorna a posicao do registro com a id desejada no arquivo, antes do indicador de tamanho
    	try {
	    	arq.seek(comeco);
			int ultimaId = arq.readInt();
			int tamRegAtual;
			long pos0;
			int idAtual = 0;
			if(ultimaId > 0 ) { // se tiver mais de um registro
				do {
					pos0 = arq.getFilePointer();
					tamRegAtual = arq.readInt();
					if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
						idAtual = arq.readInt();
						
						if(idAtual!=idDesejada) { // se nao for o registro desejado, pula para o proximo
							arq.seek(pos0);
							arq.readInt();
							arq.skipBytes(tamRegAtual);
						}
						
					} else { // se o registro atual tiver sido deletado
						arq.seek(pos0); // vai para o proximo registro
						arq.readInt();
						arq.skipBytes(tamRegAtual);
					}
				} while (idAtual!=ultimaId && idAtual!=idDesejada); // repete para cada registro ate chegar no ultimo ou ate chegar na id desejada
				
				if(idAtual == idDesejada) { // se encontramos o registro desejado, retorna a posicao do registro no arquivo
					return pos0;
				} else { // se nao encontramos o registro, retorna -1
					return -1;
				}
			} else { // se nao tiver nenhum registro
				return -1;
			}
    	} catch(IOException e) {
			System.out.println(e.getMessage());
		}
    	return -1;
    }


	public static Conta criar (RandomAccessFile arq, long comeco) {
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta;
		int numEmails;
		int idAtual = 0;
		long pos0;
		int ultimaId;
		int tamRegAtual;
		int tamString;
		int numEmailsSeek;
		String nomeUsuarioSeek;
		byte[] ba;
		int maiorId;
		
		System.out.println("\n=== CRIAR UMA CONTA ===\n"); // le os dados que o usuario quer inserir
		System.out.println("Nome:");
		nomePessoa = sc.nextLine();
		System.out.println("Número de emails:");
		numEmails = sc.nextInt();
		sc.nextLine();
		email = new String[numEmails];
		for(int i=1; i<=numEmails; i++) {
			System.out.println("Email " + i + ":");
			email[i-1] = sc.nextLine();
		}
		System.out.println("Nome de usuário:");
		nomeUsuario = sc.nextLine();
		System.out.println("Senha:");
		senha = sc.nextLine();
		try {
			senha = OneTimePad.encryptionSenha(senha);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("CPF:");
		cpf = sc.nextLine();
		System.out.println("Cidade:");
		cidade = sc.nextLine();
		System.out.println("Saldo da conta:");
		saldoConta = sc.nextFloat();
		sc.nextLine();
		
		if(cpf.length() != 11) { // se o tamanho do cpf for invalido cancela a inclusao
			System.out.println("\nErro: CPF com formato incorreto. Aperte enter para continuar.\n\n");
			sc.nextLine();
			return null;
		} else { // checa se o nome de usuario ja foi usado
			try {
				arq.seek(comeco);
				ultimaId = arq.readInt();
				maiorId = ultimaId;
				if(ultimaId > 0 ) { // se tiver mais de um registro
					do {
						tamRegAtual = arq.readInt();
						pos0 = arq.getFilePointer();
						if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
							idAtual = arq.readInt();
							if(idAtual > maiorId) { // procura pela maior id
								maiorId = idAtual;
							}
							tamString = arq.readInt(); // le o tamanho do nome
							arq.readUTF(); // pula o nome
							numEmailsSeek = arq.readInt();
							for(int i=0; i<numEmailsSeek; i++) { // pula todos os emails
								tamString = arq.readInt();
								arq.readUTF();
							}
							tamString = arq.readInt();
							nomeUsuarioSeek = arq.readUTF(); // le o nome de usuario
							if(nomeUsuarioSeek.equals(nomeUsuario)) { // se ja houver o nome de usuario cancela a inclusao 
								System.out.println("\nErro: Nome de usuário já existente. Aperte enter para continuar.\n\n");
								sc.nextLine();
								return null;
							} else { // pula pro proximo registro
								arq.seek(pos0);
								arq.skipBytes(tamRegAtual);
							}
							
						} else { // se o registro atual tiver sido deletado
							arq.seek(pos0 + tamRegAtual); // vai para o proximo registro
						}
					} while (idAtual != ultimaId); // repete para cada registro ate chegar no ultimo
				} else { // se a ultima id for -1 (nao tiver registros) salva a maior id como 0 para a nova conta ser id 1
					ultimaId = 0;
					maiorId = 0;
				}
				// inclui o registro sendo adicionado
				Conta novaConta = new Conta(maiorId+1, nomePessoa, email, nomeUsuario, senha, cpf, cidade, 0, saldoConta);
				ba = novaConta.toByteArray();
				long endFinal = arq.getFilePointer();
				arq.writeInt(ba.length);
				arq.write(ba);
				arq.seek(comeco);
				arq.writeInt(maiorId+1);
				
				// adiciona o registro ao arquivo hash
				adicionaHash(comeco, novaConta, endFinal);
				
				//adiciona o registro ao arquivo de lista invertida
				String idString = novaConta.getIdConta() + "";
				listaInvertida.createArqLista(nomePessoa, Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaNome.db");
				listaInvertida.createArqLista(cidade, Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaCidade.db");
				
				System.out.println("\nConta criada com sucesso.");
				System.out.println(novaConta.toString() + "\n");
				System.out.println("Aperte enter para continuar.");
				sc.nextLine();
				return novaConta;
			} catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
		
	}
	
	
	public static Conta buscar (RandomAccessFile arq, long comeco) {
		System.out.println("\n=== BUSCAR UMA CONTA ===\n");
		System.out.println("Digite a ID da conta que quer exibir:");
		
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta;
		int numEmails;
		int transferenciasRealizadas;
		int idAtual = 0;
		long pos0;
		int ultimaId;
		int tamRegAtual;
		int tamString;
		int idDesejada;
		
		idDesejada = sc.nextInt();
		sc.nextLine();
		
		try {
			arq.seek(comeco);
			ultimaId = arq.readInt();
			if(ultimaId > 0 ) { // se tiver mais de um registro
				do {
					tamRegAtual = arq.readInt();
					pos0 = arq.getFilePointer();
					if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
						idAtual = arq.readInt();
						
						if(idAtual!=idDesejada) { // se nao for o registro desejado, pula para o proximo
							arq.seek(pos0);
							arq.skipBytes(tamRegAtual);
						}
						
					} else { // se o registro atual tiver sido deletado
						arq.seek(pos0 + tamRegAtual); // vai para o proximo registro
					}
				} while (idAtual!=ultimaId && idAtual!=idDesejada); // repete para cada registro ate chegar no ultimo ou ate chegar na id desejada
				
				if(idAtual == idDesejada) { // se encontramos o registro desejado, le os dados do registro e o imprime
					tamString = arq.readInt();
					nomePessoa = arq.readUTF();
					numEmails = arq.readInt();
					email = new String[numEmails];
					for(int i=0; i<numEmails; i++) { // le todos os emails
						tamString = arq.readInt();
						email[i] = arq.readUTF();
					}
					tamString = arq.readInt();
					nomeUsuario = arq.readUTF();
					tamString = arq.readInt();
					senha = arq.readUTF();
					senha = OneTimePad.decryptionSenha(senha);
					cpf = arq.readUTF();
					tamString = arq.readInt();
					cidade = arq.readUTF();
					transferenciasRealizadas = arq.readInt();
					saldoConta = arq.readFloat();
					
					Conta conta = new Conta(idAtual, nomePessoa, email, nomeUsuario, senha, cpf, cidade, transferenciasRealizadas, saldoConta); // cria um objeto para ser impresso
					System.out.println("\nConta #" + idAtual + ":");
					System.out.println(conta.toString());
					System.out.println("\nAperte enter para continuar.");
					sc.nextLine();
					return conta;
				} else {
					System.out.println("\nConta não encontrada. Aperte enter para continuar.");
					sc.nextLine();
					return null;
				}
			} else { // se nao tiver nenhum registro
				System.out.println("\nBanco de dados vazio. Aperte enter para continuar.");
				sc.nextLine();
				return null;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	
	public static void transferir(RandomAccessFile arq, long comeco) {
		System.out.println("\n=== FAZER UMA TRANSFERÊNCIA ===\n");
		
		int id1, id2;
		float valor;
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta, saldoConta2;
		int numEmails;
		int transferenciasRealizadas;
		int idAtual = 0;
		long pos0, pos1, posBucketNovo, pos3;
		int ultimaId;
		int tamRegAtual;
		int tamString;
		int idDesejada;
		
		System.out.println("Número da conta de origem:"); // le as contas e o valor a ser transferido
		id1 = sc.nextInt();
		sc.nextLine();
		System.out.println("Número da conta de destino:");
		id2 = sc.nextInt();
		sc.nextLine();
		System.out.println("Valor a ser transferido:");
		valor = sc.nextFloat();
		sc.nextLine();
		
		if(valor>0) { // se o valor for valido
			// vai ate a primeira id e checa se a conta tem fundos suficientes
			try {
				idDesejada = id1;
				// busca se a id existe e faz a checagem de fundos
				arq.seek(comeco);
				ultimaId = arq.readInt();
				if(ultimaId > 0 ) { // se tiver mais de um registro
					do {
						tamRegAtual = arq.readInt();
						pos0 = arq.getFilePointer();
						if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
							idAtual = arq.readInt();
							
							if(idAtual!=idDesejada) { // se nao for o registro desejado, pula para o proximo
								arq.seek(pos0);
								arq.skipBytes(tamRegAtual);
							}
							
						} else { // se o registro atual tiver sido deletado
							arq.seek(pos0 + tamRegAtual); // vai para o proximo registro
						}
					} while (idAtual<ultimaId && idAtual!=idDesejada); // repete para cada registro ate chegar no ultimo ou ate chegar na id desejada
					
					if(idAtual == idDesejada) { // se encontramos o registro desejado, verifica se tem fundos
						tamString = arq.readInt();
						nomePessoa = arq.readUTF();
						numEmails = arq.readInt();
						email = new String[numEmails];
						for(int i=0; i<numEmails; i++) { // le todos os emails
							tamString = arq.readInt();
							email[i] = arq.readUTF();
						}
						tamString = arq.readInt();
						nomeUsuario = arq.readUTF();
						tamString = arq.readInt();
						senha = arq.readUTF();
						cpf = arq.readUTF();
						tamString = arq.readInt();
						cidade = arq.readUTF();
						pos1 = arq.getFilePointer(); // salva o local das transferencias realizadas da conta 1
						transferenciasRealizadas = arq.readInt();
						saldoConta = arq.readFloat();
						if(saldoConta >= valor) { // se a conta tiver fundos, continua com a transferencia
							// checa se existe conta com a id da conta 2
							posBucketNovo = buscaId(arq, comeco, id2); // salva o endereco da conta 2
							if(posBucketNovo!=-1) { // se existe a conta 2, continua com a transferencia
								arq.seek(pos1); // volta para antes das transferencias realizadas na conta de origem
								arq.writeInt(transferenciasRealizadas + 1); // sobe o numero de transferencias realizadas
								arq.writeFloat(saldoConta - valor); // grava o novo saldo da conta
								arq.seek(posBucketNovo); // volta para o comeco da conta de destino
								tamRegAtual = arq.readInt(); // le a conta 2 ate o saldo
								arq.readChar();
								arq.readInt();
								tamString = arq.readInt();
								nomePessoa = arq.readUTF();
								numEmails = arq.readInt();
								email = new String[numEmails];
								for(int i=0; i<numEmails; i++) { // le todos os emails
									tamString = arq.readInt();
									email[i] = arq.readUTF();
								}
								tamString = arq.readInt();
								nomeUsuario = arq.readUTF();
								tamString = arq.readInt();
								senha = arq.readUTF();
								cpf = arq.readUTF();
								tamString = arq.readInt();
								cidade = arq.readUTF();
								pos1 = arq.getFilePointer();
								transferenciasRealizadas = arq.readInt();
								pos3 = arq.getFilePointer(); // salva o local do saldo da conta 2
								saldoConta2 = arq.readFloat();
								arq.seek(pos3); // volta para antes do saldo
								arq.writeFloat(saldoConta2 + valor); // grava o saldo da conta 2 apos a transferencia
								System.out.println("\nTransferência realizada com sucesso.\nNovo saldo: " + saldoConta + "\n\nAperte enter para continuar.");
								sc.nextLine();
								return;
							} else { // se nao existe a conta 2
								System.out.println("\nConta de destino não encontrada. Aperte enter para continuar.");
								sc.nextLine();
								return;
							}
							
						} else { // se a conta nao tiver fundos, cancela a transacao
							System.out.println("\nA conta não tem fundos suficientes para essa transação. Aperte enter para continuar.");
							sc.nextLine();
							return;
						}
					} else {
						System.out.println("\nConta de origem não encontrada. Aperte enter para continuar.");
						sc.nextLine();
						return;
					}
				} else { // se nao tiver nenhum registro
					System.out.println("\nBanco de dados vazio. Aperte enter para continuar.");
					sc.nextLine();
					return;
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		} else { // se o valor nao for valido, cancela a transferencia
			System.out.println("\nValor inválido. Aperte enter para continuar.");
			sc.nextLine();
			return;
		}
		return;
	}
	
	
	public static void deletar (RandomAccessFile arq, long comeco) { // chama a funcao que remove um registro
		System.out.println("\n=== DELETAR UMA CONTA ===\n");
		System.out.println("Número da conta que quer deletar:");
		int id = sc.nextInt();
		sc.nextLine();
		
		if(remove(arq, comeco, id)) {
			deletaHash(comeco, id); // remove o registro do arquivo de hash
			String idString = id + "";
			listaInvertida.DeleteAllIdForList(Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaNome.db"); // remove o registro da lista invertida
			listaInvertida.DeleteAllIdForList(Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaCidade.db"); // remove o registro da lista invertida
			System.out.println("\nConta deletada com sucesso. Aperte enter para continuar.");
			sc.nextLine();
			return;
		} else {
			System.out.println("\nErro ao deletar a conta. Aperte enter para continuar.");
			sc.nextLine();
			return;
		}
	}
	
	
	public static boolean remove (RandomAccessFile arq, long comeco, int id) { // funcao que marca a lapide de um registro
		long posReg = buscaId(arq, comeco, id);
		int ultimaId;
		if (posReg > 0) { // se a posicao no arquivo for valida
			try {
				arq.seek(comeco);
				ultimaId = arq.readInt();
				if(id == ultimaId) { // se a id for a ultima
					long pos0, posAnterior;
					int tamRegAtual, idDesejada, idAtual, idAnterior;
					idDesejada = id;
					idAtual = 0;
					idAnterior = 0;
					posAnterior = posReg;
					do {
						pos0 = arq.getFilePointer();
						tamRegAtual = arq.readInt();
						if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
							idAtual = arq.readInt();
							
							if(idAtual!=idDesejada) { // se nao for o registro desejado, pula para o proximo
								idAnterior = idAtual;
								arq.seek(pos0);
								arq.readInt();
								arq.skipBytes(tamRegAtual);
								posAnterior = pos0; // salva a posicao do ultimo registro antes do que foi deletado
							}
							
						} else { // se o registro atual tiver sido deletado
							arq.seek(pos0); // vai para o proximo registro
							arq.readInt();
							arq.skipBytes(tamRegAtual);
						}
					} while (idAtual<ultimaId && idAtual!=idDesejada); // repete para cada registro ate chegar no ultimo ou ate chegar na id desejada
					int novaUltimaId;
					if(idAnterior > 0) {
						arq.seek(posAnterior);
						tamRegAtual = arq.readInt();
						arq.readChar();
						novaUltimaId = arq.readInt(); // salva qual sera a nova ultima id
					} else {
						novaUltimaId = 0; // se nao tem id anterior, a nova ultima id apos a remocao sera 0
					}
					arq.seek(comeco);
					arq.writeInt(novaUltimaId); // grava a nova ultima id no cabecalho
				}
				arq.seek(posReg);
				arq.readInt();
				arq.writeChar('*'); // marca a lapide
				return true;
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		} else { // se a posicao nao for valida, cancela a remocao
			return false;
		}
		return false;
	}
	
	
	public static Conta leRegistro (RandomAccessFile arq, long comeco, long pos0) { // le um registro e retorna esse registro como objeto (nao le o tamanho do registro e ignora a lapide)
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta;
		int numEmails;
		int tamString;
		int transferenciasRealizadas;
		int id;
		
		try {
			arq.readChar();
			id = arq.readInt();
			tamString = arq.readInt();
			nomePessoa = arq.readUTF();
			numEmails = arq.readInt();
			email = new String[numEmails];
			for(int i=0; i<numEmails; i++) { // le todos os emails
				tamString = arq.readInt();
				email[i] = arq.readUTF();
			}
			tamString = arq.readInt();
			nomeUsuario = arq.readUTF();
			tamString = arq.readInt();
			senha = arq.readUTF();
			senha = OneTimePad.decryptionSenha(senha);
			cpf = arq.readUTF();
			tamString = arq.readInt();
			cidade = arq.readUTF();
			transferenciasRealizadas = arq.readInt();
			saldoConta = arq.readFloat();
			
			Conta conta = new Conta(id, nomePessoa, email, nomeUsuario, senha, cpf, cidade, transferenciasRealizadas, saldoConta); // cria um objeto para ser retornado
			return conta;
		}  catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return new Conta();
	}
	
	
	public static void escreveRegistro (RandomAccessFile arq, long pos0, Conta conta) { // escreve uma conta na posicao dada (escreve tambem o tamanho, nao grava ultimaId)
		try {
			arq.seek(pos0);
			byte[] ba = conta.toByteArray();
			arq.writeInt(ba.length);
			arq.write(ba);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}
	
	
	public static void imprimeArquivo (RandomAccessFile arq, long comeco) { // imprime as ids de um arquivo
		int ultimaId;
		int tamRegAtual;
		long pos0;
		int idAtual;
		
		try {
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = -1;
			System.out.print("| ");
			while(idAtual != ultimaId) { // varre o arquivo e imprime as ids
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				if(arq.readChar() != '*') {
					idAtual = arq.readInt();
					System.out.print(idAtual + ", ");
				} else {
					System.out.print("*, ");
				}
				arq.seek(pos0);
				arq.skipBytes(tamRegAtual);
				System.out.print(tamRegAtual + "B | "); // teste
			}
			System.out.println("");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static Conta alterar (RandomAccessFile arq, long comeco) { // altera um registro. se o tamanho do registro mudar, marca a lapide do origininal e adiciona o registro alterado no final do arquivo
		System.out.println("\n=== ALTERAR UMA CONTA ===\n");
		
		int id;
		long pos0, pos1, posBucketNovo, posOrig;
		int ultimaId;
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta;
		int numEmails;
		int tamString;
		int tamReg, tamReg2, tamRegAtual;
		int idAtual = 0;
		int numEmailsSeek;
		String nomeUsuarioSeek;
		byte[] ba;
		
		System.out.println("Número da conta que deseja alterar: ");
		id = sc.nextInt();
		sc.nextLine();
		posOrig = buscaId(arq, comeco, id); // procura o endereco do registro a ser alterado
		
		if(posOrig == -1) { // se nao encontrou o registro, da um erro e cancela a operacao
			System.out.println("\nRegistro não encontrado. Aperte enter para continuar.");
			sc.nextLine();
			return null;
		}
		
		try {
			arq.seek(posOrig); // vai ate o registro desejado
			tamReg = arq.readInt();
			pos1 = arq.getFilePointer();
			Conta conta1 = leRegistro(arq, comeco, pos1); // le o registro a ser alterado
			System.out.println(conta1.toString());
			System.out.println("\nDigite os novos dados que deseja salvar.\n");
			
			System.out.println("Nome:");
			nomePessoa = sc.nextLine();
			System.out.println("Número de emails:");
			numEmails = sc.nextInt();
			sc.nextLine();
			email = new String[numEmails];
			for(int i=1; i<=numEmails; i++) {
				System.out.println("Email " + i + ":");
				email[i-1] = sc.nextLine();
			}
			System.out.println("Nome de usuário:");
			nomeUsuario = sc.nextLine();
			System.out.println("Senha:");
			senha = sc.nextLine();
			senha = OneTimePad.encryptionSenha(senha);
			System.out.println("CPF:");
			cpf = sc.nextLine();
			System.out.println("Cidade:");
			cidade = sc.nextLine();
			System.out.println("Saldo da conta:");
			saldoConta = sc.nextFloat();
			sc.nextLine();
			
			if(cpf.length() != 11) { // se o tamanho do cpf for invalido cancela a operacao
				System.out.println("\nErro: CPF com formato incorreto. Aperte enter para continuar.\n\n");
				sc.nextLine();
				return null;
			} else { // checa se o nome de usuario ja foi usado
				arq.seek(comeco);
				ultimaId = arq.readInt();
				if(ultimaId > 0 ) { // se tiver mais de um registro
					do {
						tamRegAtual = arq.readInt();
						pos0 = arq.getFilePointer();
						if(arq.readChar() != '*') { // se o registro atual nao tiver sido deletado
							idAtual = arq.readInt();
							tamString = arq.readInt(); // le o tamanho do nome
							arq.readUTF(); // pula o nome
							numEmailsSeek = arq.readInt();
							for(int i=0; i<numEmailsSeek; i++) { // pula todos os emails
								tamString = arq.readInt();
								arq.readUTF();
							}
							tamString = arq.readInt();
							nomeUsuarioSeek = arq.readUTF(); // le o nome de usuario
							if(nomeUsuarioSeek.equals(nomeUsuario) && !nomeUsuarioSeek.equals(conta1.getNomeUsuario())) { // se ja houver o nome de usuario cancela a inclusao 
								System.out.println("\nErro: Nome de usuário já existente. Aperte enter para continuar.\n\n");
								sc.nextLine();
								return null;
							} else { // pula pro proximo registro
								arq.seek(pos0);
								arq.skipBytes(tamRegAtual);
							}
							
						} else { // se o registro atual tiver sido deletado
							arq.seek(pos0 + tamRegAtual); // vai para o proximo registro
						}
					} while (idAtual != ultimaId); // repete para cada registro ate chegar no ultimo
				}
				
				// se chegou ate aqui o registro eh valido para ser adicionado
				Conta conta2 = new Conta(id, nomePessoa, email, nomeUsuario, senha, cpf, cidade, conta1.getTransferenciasRealizadas(), saldoConta); // cria um objeto para ser inserido
				tamReg2 = conta2.length();
				if(tamReg2 == tamReg) { // se os registros tiverem o mesmo tamanho, escreve no mesmo lugar
					arq.seek(pos1);
					ba = conta2.toByteArray(); 
					arq.write(ba); // escreve o registro no mesmo lugar
					System.out.println("\nRegistro alterado com sucesso.");
					System.out.println(conta2.toString());
					System.out.println("\nAperte enter para continuar.");
					sc.nextLine();
					return conta2;
				} else { // se os registros nao tiverem o mesmo tamanho, deleta o registro e adiciona de novo no final do arquivo
					remove(arq, comeco, id);
					posBucketNovo = buscaId(arq, comeco, ultimaId);
					arq.seek(comeco);
					arq.writeInt(id); // salva a id como a ultima id
					if(posBucketNovo > 0) { // se o registro deletado nao foi o unico do bd
						arq.seek(posBucketNovo); // vai ate o ultimo registro
						tamRegAtual = arq.readInt();
						arq.skipBytes(tamRegAtual); // pula o ultimo registro
					}
					
					long posNova = arq.getFilePointer();
					
					alteraHash(comeco, id, posNova); // altera o endereco no arquivo hash
					String idString = conta2.getIdConta() + "";
					listaInvertida.updateLista(conta2.getNomePessoa(), Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaNome.db", false); // altera o arquivo de lista invertida
					listaInvertida.updateLista(conta2.getCidade(), Byte.parseByte(idString), "dados/listaInvertida/listaInvertidaCidade.db", false); // altera o arquivo de lista invertida
					
					arq.writeInt(tamReg2);
					ba = conta2.toByteArray();
					arq.write(ba); // escreve os novos dados no final do arquivo
					System.out.println("\nRegistro alterado com sucesso.");
					System.out.println(conta2.toString());
					System.out.println("\nAperte enter para continuar.");
					sc.nextLine();
					return conta2;
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return null;
	}
	
	
	public static void copiaArquivo (RandomAccessFile arqOrigem, long comeco, RandomAccessFile arqDestino) { // copia um arquivo no lugar de outro arquivo
		long pos0, pos1;
		Conta contaTemp;
		int tamRegAtual;
		int ultimaId;
		int idAtual;
		
		try {
			arqOrigem.seek(comeco);
			arqDestino.seek(comeco);
			
			// le qual eh a ultima id
			ultimaId = arqOrigem.readInt();
			
			if(ultimaId > 0) {
				// escreve a ultima id
				arqDestino.writeInt(ultimaId);
				
				do {

					// le o registro atual
					tamRegAtual = arqOrigem.readInt();
					pos0 = arqOrigem.getFilePointer();
					contaTemp = leRegistro (arqOrigem, comeco, pos0);
					idAtual = contaTemp.getIdConta();
					//System.out.println(contaTemp.toString()); // teste
					
					// se o registro atual nao tiver sido deletado
					if(contaTemp.getLapide() != '*') {
						
						// escreve o registro atual
						pos1 = arqDestino.getFilePointer();
						escreveRegistro(arqDestino, pos1, contaTemp);
					}
					
				} while (idAtual != ultimaId);
				
			} else {
				arqDestino.writeInt(-1);
				return;
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
			
	public static void intercalacaoBalanceada (RandomAccessFile arq, long comeco) {
		try {
			System.out.println("\n=== INTERCALAÇÃO BALANCEADA COMUM ===\n");
			
			int m, n; // m registros, n caminhos
			int ultimaId;
			int idAtual = 0;
			int tamRegAtual;
			long pos0, pos1, posBucketNovo;
			Conta contatemp;
			int arquivoFinal = 0;
			
			System.out.println("Por favor, informe...\nNúmero de registros que cabem na memória:");
			m = sc.nextInt();
			sc.nextLine();
			System.out.println("Número de caminhos:");
			n = sc.nextInt();
			sc.nextLine();
			ArrayList<RandomAccessFile> arqTemp = new ArrayList<RandomAccessFile>();
			List<Conta> memoria = new ArrayList<Conta>(m);
			int [] ultimaId2 = new int [2*n];
			int[] ultimoSalvo = new int[2*n];
			
			
			arq.seek(comeco);
			ultimaId = arq.readInt();
			System.out.println("\nArquivo antes da ordenação:");
			/*while(idAtual != ultimaId) { // imprime a ordem do arquivo antes da ordenacao
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				if(arq.readChar() != '*') {
					idAtual = arq.readInt();
					System.out.print(idAtual + " ");
				}
				arq.seek(pos0);
				arq.skipBytes(tamRegAtual);
			}
			System.out.println("");*/
			imprimeArquivo(arq, comeco);
			
			for(int i=0; i<2*n; i++) { // inicia os RandomAccesFiles dos arquivos temporarios
				arqTemp.add(new RandomAccessFile("dados/arqTemp" + i + ".db", "rw"));
				arqTemp.get(i).writeInt(-1); // escreve -1 como sendo a ultima id
				ultimaId2[i] = -1;
			}
			
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = 0;
			
			for(int i=0; idAtual != ultimaId; i++){ // faz a distribuicao
				// carrega a memoria com os dados
				while(memoria.size()<m && idAtual != ultimaId) { // carrega os m registros na memoria
					tamRegAtual = arq.readInt();
					pos1 = arq.getFilePointer();
					if(arq.readChar()!='*') {
						arq.seek(pos1);
						contatemp = leRegistro(arq, comeco, pos1);
						memoria.add(contatemp);
						idAtual = contatemp.getIdConta();
					} else {
						arq.seek(pos1);
						arq.skipBytes(tamRegAtual);
					}
				}
				
				//for(Conta k : memoria) { // teste
				//	System.out.print(k.getIdConta());
				//}
				
				memoria.sort(Comparator.comparing(Conta::getIdConta)); // ordena a memoria
				
				//for(Conta k : memoria) { // teste
				//	System.out.print(k.getIdConta());
				//}
				//System.out.println(memoria.size()); // teste
				
				if(arqTemp.get(i%n).length() == 0) { // se o arquivo for vazio, escreve -1 como a ultima id
					arqTemp.get(i%n).seek(comeco);
					arqTemp.get(i%n).writeInt(-1);
					posBucketNovo = arqTemp.get(i%n).getFilePointer(); // salva a posicao atual
					//System.out.println("arquivo vazio"); // teste
				} else {
					posBucketNovo = arqTemp.get(i%n).getFilePointer(); // salva a posicao atual
					arqTemp.get(i%n).seek(comeco); // navega ate o comeco do arquivo temporario para gravar qual foi a ultima id
					arqTemp.get(i%n).writeInt(memoria.get(memoria.size()-1).getIdConta()); // salva qual eh a ultima id na memoria
					arqTemp.get(i%n).seek(posBucketNovo); // navega ate a posicao do comeco do bloco atual
				}
				for(Conta contaTemp : memoria) { // grava os registros da memoria no arquivo temporario
					//System.out.println("escreveu registro " + contaTemp.getIdConta()); // teste
					arqTemp.get(i%n).seek(comeco);
					arqTemp.get(i%n).writeInt(contaTemp.getIdConta()); // escreve a ultima id
					ultimaId2[i%n] = contaTemp.getIdConta();
					arqTemp.get(i%n).seek(posBucketNovo);
					escreveRegistro(arqTemp.get(i%n), posBucketNovo, contaTemp);
					posBucketNovo = arqTemp.get(i%n).getFilePointer();
				}
				
				memoria.clear(); // limpa a memoria
				
				//System.out.println("Arquivo " + i%n + ":"); // teste
				//System.out.println("ultimaid " + ultimaId); // teste
				//imprimeArquivo(arqTemp.get(i%n), comeco); // teste
			}
			
			//System.out.println("INTERCALAÇÃO"); // teste
			
			// do {
				// do {
					// enquanto bloco nao acabou
						// para cada arquivo temporario do lado atual
							// se for a primeira passada ou for o que saiuDaFita
								// se a posicao do bloco atual for < tamanho do bloco ordenado
									// se nao tiver acabado o arquivo
										// le o registro atual
										// aumenta a posBlocoAtual
									// se tiver acabado o arquivo
										// salva o blocoAcabou
								// se a posicao do bloco atual for >= tamanho do bloco ordenado
									// salva o blocoAcabou
						// checa o menor na memoria
							// se o registro for null pula ele
						// salva o saiuDaFita
						// salva o registro no arquivo de saida
						// salva o ultimoSalvo
						// remove o registro da memoria
						// checa se todos os blocos acabaram
					// volta as posBlocoAtual para 0
					// reseta os blocos
					// troca o arquivo de saida 
					// checa se todos os registros foram lidos
				// } repete enquanto todos ultimoSalvo forem diferentes do ultimaId2 para os n arquivos atuais
				// dobra o tamBlocoOrdenado
				// troca os arquivos atuais
				// troca a fitaDeSaida
					// limpa os novos arquivos de saida
			// } repete enquanto tiver mais de um arquivo de saida com dados
			
			int[] posBlocoAtual = new int [n]; // tamanho do bloco
			int tamBlocoOrdenado = m;
			int saiuDaFita = 0;
			boolean[] blocoAcabou = new boolean[n];
			Conta[] memoria2 = new Conta[n];
			int fitaAtual;
			int ordem = 0;
			long[] pos = new long[n*2];
			int menor;
			int indiceMenor;
			int fitaDeSaida = n;
			boolean todosAcabaram = false;
			boolean todosRegistrosLidos;
			int naoOrdem;
			boolean temMaisDeUmComDados = true;
			int arqComDados = n;
			
			// zera o ultimoSalvo, posBlocoAtual e pos e volta o ponteiro para o comeco dos arquivos temporarios
			for(int i=0; i<n*2; i++) {
				ultimoSalvo[i] = -1;
				arqTemp.get(i).seek(comeco);
				ultimaId2[i] = arqTemp.get(i).readInt(); // salva a ultima id 
				pos[i] = arqTemp.get(i).getFilePointer();
			}
			for(int i=0; i<n; i++) {
				posBlocoAtual[i] = 0;
			}
			
			// do {
			do {
				
				// reseta os ponteiros para o comeco do arquivo
				for(int i=0; i<n*2; i++) {
					arqTemp.get(i).seek(comeco);
					arqTemp.get(i).readInt();
					pos[i] = arqTemp.get(i).getFilePointer();
				}
				
				// do {
				do {
					
					// enquanto bloco nao acabou
					while(!todosAcabaram) {
						
						// para cada arquivo temporario do lado atual
						for(int i=0; i<n; i++) {
							
							// se for a primeira passada ou for o que saiuDaFita
							fitaAtual = (ordem*n) + i;
							if(posBlocoAtual[fitaAtual%n] == 0 || fitaAtual == saiuDaFita) {
								
								// se a posicao do bloco atual for < tamanho do bloco ordenado
								if(posBlocoAtual[fitaAtual%n] < tamBlocoOrdenado) {
									
									// se nao tiver acabado o arquivo
									if(ultimoSalvo[fitaAtual] != ultimaId2[fitaAtual]) {
										
										// le o registro atual
										//System.out.print("tentou ler registro na fita " + fitaAtual + " na posição " + pos[fitaAtual] + "\nfita " + fitaAtual + ": "); // teste
										//imprimeArquivo(arqTemp.get(fitaAtual), comeco); // teste
										//System.out.println("ultimoSalvo[" + fitaAtual + "] = " + ultimoSalvo[fitaAtual] + " ultimaId2[" + fitaAtual + "] = " + ultimaId2[fitaAtual] + " posBlocoAtual = " + posBlocoAtual[fitaAtual%n]); // teste
										arqTemp.get(fitaAtual).seek(pos[fitaAtual]);
										tamRegAtual = arqTemp.get(fitaAtual).readInt();
										memoria2[i] = leRegistro(arqTemp.get(fitaAtual), comeco, pos[fitaAtual]);
										pos[fitaAtual] = arqTemp.get(fitaAtual).getFilePointer();
										// ultimoSalvo[fitaAtual] = memoria2[i].getIdConta();
										//System.out.println("leu registro " + memoria2[i].getIdConta() + " posBlocoAtual[" + fitaAtual%n + "] calculado = " + (posBlocoAtual[fitaAtual%n] + 1)); // teste
										
										// aumenta a posBlocoAtual
										posBlocoAtual[fitaAtual%n]++;
										//System.out.println("depois da leitura posBlocoAtual[" + fitaAtual%n + "] = " + posBlocoAtual[fitaAtual%n]); // teste
									}
									// se tiver acabado o arquivo
									else {
										
										// salva o blocoAcabou
										blocoAcabou[fitaAtual%n] = true;
									}
								}
								// se a posicao do bloco atual for >= tamanho do bloco ordenado
								else {
									
									// salva o blocoAcabou
									blocoAcabou[fitaAtual%n] = true;
								}
							}
						}
						
						//for(int i=0; i<n; i++) // teste
						//	if(memoria2[i] != null) // teste
						//		System.out.print(memoria2[i].getIdConta() + " "); // teste
						//System.out.println(""); // teste
						
						
						/*if(memoria2[0] != null) {
							menor = memoria2[0].getIdConta();
							indiceMenor = 0;
						} else { 
							
							// se o registro de indice 0 for null busca o proximo registro not null
							menor = 2147483647;
							indiceMenor = 2147483647;
							boolean achouProximo = false;
							for(int i=0; i<n && !achouProximo; i++) {
								if(memoria2[i] != null) {
									menor = memoria2[i].getIdConta();
									indiceMenor = i;
									achouProximo = true;
								} else {
									menor = 2147483647;
									indiceMenor = 2147483647;
								}
							}
						}*/
						
						// checa o menor na memoria
							// se o registro for null pula ele
						menor = 2147483647;
						indiceMenor = 2147483647;
						for(int i=0; i<n; i++) {
							if(memoria2[i] != null) {
								if(memoria2[i].getIdConta() < menor) {
									menor = memoria2[i].getIdConta();
									indiceMenor = i;
								}
							}
						}
						
						// se achou um registro, salva ele
						if(indiceMenor != 2147483647) { 
							
							// salva o saiuDaFita
							saiuDaFita = indiceMenor + (ordem*n);
							int fitaQueFoiLida = (ordem*n) + indiceMenor; 
							
							//System.out.println("Tentou escrever registro " + menor + " de indice " + indiceMenor + " no arquivo " + fitaDeSaida); // teste
							// salva o registro no arquivo de saida
							arqTemp.get(fitaDeSaida).seek(comeco);
							arqTemp.get(fitaDeSaida).writeInt(menor);
							escreveRegistro(arqTemp.get(fitaDeSaida), pos[fitaDeSaida], memoria2[indiceMenor]);
							pos[fitaDeSaida] = arqTemp.get(fitaDeSaida).getFilePointer();
							//pos[saiuDaFita] = arqTemp.get(saiuDaFita).getFilePointer();
							
							// salva o ultimoSalvo
							ultimaId2[fitaDeSaida] = memoria2[indiceMenor].getIdConta();
							ultimoSalvo[fitaQueFoiLida] = memoria2[indiceMenor].getIdConta();
							//System.out.println("escreveu registro " + menor + " de indice " + indiceMenor + " no arquivo " + fitaDeSaida + ", ultimoSalvo[" + fitaQueFoiLida + "] = " + ultimoSalvo[fitaQueFoiLida]); // teste
							
							// remove o registro da memoria
							memoria2[indiceMenor] = null;
						} else {
							//System.out.println("não escreveu nenhum registro"); // teste
						}
						
						// checa se todos os blocos acabaram
						todosAcabaram = true;
						for(int i=0; i<n; i++) {
							if(!blocoAcabou[i]) {
								todosAcabaram = false;
							}
						}
						
						//System.out.println("fita de saida " + fitaDeSaida + ":"); // teste
						//imprimeArquivo(arqTemp.get(fitaDeSaida), comeco); // teste
						
					}
					
					// volta as posBlocoAtual para 0
					for(int i=0; i<n; i++) {
						posBlocoAtual[i] = 0;
						blocoAcabou[i] = false;
						//System.out.println("zerou posBloco["+i+"]"); // teste
					}
					
					// reseta os blocos
					todosAcabaram = false;
					
					// troca o arquivo de saida
					//System.out.print("fita de saida antes: " + fitaDeSaida); // teste
					if (ordem == 0) {
						naoOrdem = 1;
					} else {
						naoOrdem = 0;
					}
					fitaDeSaida = (naoOrdem*n) + ((fitaDeSaida+1)%n);
					//System.out.println(" fita de saida depois: " + fitaDeSaida); // teste
					
					// checa se todos os registros foram lidos
					todosRegistrosLidos = true;
					for(int i=0; i<n; i++) {
						if(ultimoSalvo[(ordem*n) + i] != ultimaId2[(ordem*n) + i]) {
							todosRegistrosLidos = false;
						}
						//System.out.println("final do while todosRegistrosLidos ultimoSalvo[" + ((ordem*n) + i) + "] = " + ultimoSalvo[(ordem*n) + i] + " ultimaId2[" + ((ordem*n) + i) + "] = " + ultimaId2[(ordem*n) + i]); // teste
						//sc.nextLine();
					}
					
				// } repete enquanto todos ultimoSalvo forem diferentes do ultimaId2 para os n arquivos atuais
				} while (!todosRegistrosLidos);
				
				// dobra o tamBlocoOrdenado
				tamBlocoOrdenado *= 2;
				
				// troca os arquivos atuais
				if(ordem == 0) {
					ordem = 1;
					naoOrdem = 0;
				} else {
					ordem = 0;
					naoOrdem = 1;
				}
				
				// troca a fitaDeSaida
				//System.out.print("troca grupo de fitas. fita de saida antes: " + fitaDeSaida); // teste
				fitaDeSaida = naoOrdem*n;
				//System.out.println(" fita de saida depois: " + fitaDeSaida); // teste
				
				// limpa os novos arquivos de saida
				for(int i=0; i<n; i++) {
					int arquivoAtual = (naoOrdem*n) + i; 
					arqTemp.get(arquivoAtual).setLength(0);
					arqTemp.get(arquivoAtual).writeInt(-1);
					pos[arquivoAtual] = arqTemp.get(arquivoAtual).getFilePointer();
					//System.out.println("limpou o ultimoSalvo do arquivo " + ((ordem*n) + i)); // teste
					ultimoSalvo[(ordem*n) + i] = -1; // ultimo salvo do arquivo de entrada
					ultimaId2[arquivoAtual] = -1;
				}
				
				// checa se tem mais de um com dados
				temMaisDeUmComDados = false;
				int numComDados = 0;
				for(int i=0; i<n; i++) {
					int arquivoAtual = (ordem*n) + i;
					
					//System.out.println("arquivo " + arquivoAtual + ":"); // teste
					//imprimeArquivo(arqTemp.get(arquivoAtual), comeco); // teste
					//System.out.println("length = " + arqTemp.get(arquivoAtual).length()); // teste
					if(arqTemp.get(arquivoAtual).length() > 4) {
						numComDados++;
						arqComDados = arquivoAtual;
						//System.out.println("arquivo " + (arquivoAtual) + " tem dados"); // teste
						arquivoFinal = arquivoAtual;
					}
					// sc.nextLine(); // teste
					
					// reseta os ponteiros para o comeco do arquivo
					arqTemp.get(arquivoAtual).seek(comeco);
					arqTemp.get(arquivoAtual).readInt();
					pos[arquivoAtual] = arqTemp.get(arquivoAtual).getFilePointer();
				}
				if(numComDados > 1) {
					temMaisDeUmComDados = true;
				} 
				
			// } repete enquanto tiver mais de um arquivo de saida com dados
			} while (temMaisDeUmComDados);
			//System.out.println("arquivo final com dados: "); // teste
			//imprimeArquivo(arqTemp.get(arqComDados), comeco); // teste
			
			// escreve sobre o arquivo de dados
			copiaArquivo(arqTemp.get(arquivoFinal), comeco, arq);
			System.out.println("\nArquivo após a ordenação:");
			imprimeArquivo(arq, comeco);
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static long getEndDir (RandomAccessFile arqDir, long comeco, int idDesejada) { // varre o arquivo de diretorio buscando o registro de id selecionado e retorna o endereco do bucket correspondente
		long endDir = -1;
		try {
			arqDir.seek(comeco);
			int profGlobal = arqDir.readInt();
			double tamDir = Math.pow(2, profGlobal);
			// para cada valor no diretorio
			for(int i=0; i<tamDir; i++) { 
				// checa se eh o bucket da id desejada
				if(i == idDesejada % tamDir) {	
					// salva o endereco do bucket
					endDir = arqDir.readLong();
				} else {
					// pula para o proximo
					arqDir.readLong();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return endDir;
	}
	
	
	public static void criaHash(RandomAccessFile arq, long comeco) { // cria o arquivo de hash inicial
		// abre o arquivo de indice hash
		// limpa o arquivo de indice hash
		// abre o arquivo de diretorio
		// limpa o arquivo de diretorio
		// inicializa os arquivos de indice e diretorio com p=1 e buckets vazios
		// para cada registro no arquivo
			// le o registro
			// ve no diretorio em qual bucket vai cair
			// acessa o bucket
			// se o bucket ja estiver cheio
				// se a profundidade local for menor que a profundidade global
					// aumenta a profundidade local
					// cria um bucket novo
					// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1)) 
					// rebalanceia o bucket atual com o seu segundo %
					// insere o registro no bucket equivalente
				// se a profundidade local for igual a profundidade global
					// aumenta a profundidade local
					// aumenta a profundidade global
					// dobra o tamanho do diretorio
					// copia os ponteiros antigos do diretorio para os campos novos
					// cria um bucket novo
					// troca o ponteiro do segundo registro de % atual para o bucket novo
					// rebalanceia o bucket atual com o seu segundo %
					// insere o registro no bucket equivalente
			// se o bucket nao estiver cheio
				// insere o registro no bucket
		// end while
		
		try {
			
			int ultimaId;
			int idAtual;
			Conta contaTemp;
			int tamRegAtual;
			long pos0, pos1, posBucketNovo;
			long endDir;
			int profGlobal;
			int profLocal;
			int numRegs;
			char lapide;
			int ultimoBucket;
			double tamDir;
						
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("dados/hash.db", "rw");
			
			// limpa o arquivo de indice hash
			arqHash.setLength(0);
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("dados/diretorio.db", "rw");
			
			// limpa o arquivo de diretorio
			arqDir.setLength(0);
			
			// inicializa os arquivos de indice e diretorio com p=1 e buckets vazios
			arqDir.seek(comeco);
			arqDir.writeInt(1); // profundidade global
			profGlobal = 1;
			arqHash.seek(comeco);
			arqHash.writeInt(1); // numero ultimo bucket
			for(int i=0; i<2; i++) {
				arqDir.writeLong(arqHash.getFilePointer()); // escreve o endereco do bucket correspondente
				arqHash.writeInt(1); // profundidade local
				arqHash.writeInt(0); // numero de elementos
				for(int j=0; j<4; j++) {
					arqHash.writeInt(-1); // chave
					arqHash.writeLong(-1); // endereco
				}
			}
						
			
			// para cada registro no arquivo
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = -1;
			while (idAtual != ultimaId) {
				
				// le o registro
				pos1 = arq.getFilePointer(); // endereco antes do tamanho do registro, eh o que sera gravado no diretorio
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				lapide = arq.readChar();
				arq.seek(pos0);
				contaTemp = leRegistro(arq, comeco, pos0);
				if(lapide != '*') {
					idAtual = contaTemp.getIdConta();
					
					// ve no diretorio em qual bucket vai cair
					endDir = getEndDir(arqDir, comeco, idAtual);
					
					// acessa o bucket
					arqHash.seek(endDir);
					profLocal = arqHash.readInt();
					numRegs = arqHash.readInt();
					
					// se o bucket ja estiver cheio
					if(numRegs == 4) {
						
						// se a profundidade local for menor que a profundidade global
						if(profLocal < profGlobal) {
							
							// aumenta a profundidade local
							profLocal += 1;
							arqHash.seek(endDir);
							arqHash.writeInt(profLocal);
							
							// cria um bucket novo
							arqHash.seek(comeco);
							ultimoBucket = arqHash.readInt();
							for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
								arqHash.skipBytes(56);
							}
							posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
							arqHash.writeInt(profLocal);
							arqHash.writeInt(0); // numero de registros
							for(int j=0; j<4; j++) { // escreve valores -1
								arqHash.writeInt(-1); // chave
								arqHash.writeLong(-1); // endereco
							}
							
							// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1))
							arqDir.seek(comeco);
							profGlobal = arqDir.readInt(); // profundidade global
							tamDir = Math.pow(2, profGlobal);
							for(int i=0; i<tamDir; i++) {
								if(((i >> profLocal-1) % 2) == 1 && (i % Math.pow(2, (profGlobal-1))) == (idAtual % Math.pow(2, (profGlobal-1)))) { // se a id atual for parte da 2a metade das ids que pertencem ao grupo que contem ponteiros para o mesmo bucket 
									arqDir.writeLong(posBucketNovo); // grava a posicao do novo bucket
								}
								else { // senao, pula para o proximo
									arqDir.skipBytes(8);
								}
							}
							
							// rebalanceia o bucket atual com o seu segundo %
								
								// le os registros
							int[] chave = new int [5];
							long[] endereco = new long [5];
							arqHash.seek(endDir);
							arqHash.readInt(); // pula a profundidade
							arqHash.readInt(); // pula o numero de elementos (eh 4)
							for(int i=0; i<4; i++) {
								chave[i] = arqHash.readInt();
								endereco[i] = arqHash.readLong();
							}
								// define em qual posicao vai ficar
							int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
							for(int i=0; i<4; i++) {
								if(idAtual < chave[i]) {
									novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
									i = 4; // break
								} else if(idAtual == chave[i]) {
									System.out.println("Erro: ID duplicada");
									return;
								}
							}
							
								// insere o registro e remaneja os existentes
							for(int i=3; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							
							// insere os registros nos buckets equivalentes
							long posPrimeiro, posSegundo;
							arqHash.seek(endDir); // vai para a posicao do primeiro bucket
							arqHash.readInt(); // pula a profundidade
							posPrimeiro = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
							arqHash.readInt(); // pula a profundidade
							posSegundo = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							int numRegs2;
							for(int i=0; i<5; i++) {
								if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
									arqHash.seek(posPrimeiro);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posPrimeiro);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
									
								} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
									arqHash.seek(posSegundo);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posSegundo);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								}
							}
							
							// aumenta o ultimoBucket
							ultimoBucket += 1;
							arqHash.seek(comeco);
							arqHash.writeInt(ultimoBucket);
						}
						
						// se a profundidade local for igual a profundidade global
						else {
							
							// aumenta a profundidade local
							profLocal += 1;
							arqHash.seek(endDir);
							arqHash.writeInt(profLocal);
							
							// aumenta a profundidade global
							profGlobal += 1;
							arqDir.seek(comeco);
							arqDir.writeInt(profGlobal);
							
							// dobra o tamanho do diretorio
							tamDir = Math.pow(2, profGlobal);
							
							// copia os ponteiros antigos do diretorio para os campos novos
							long posMetade1 = arqDir.getFilePointer();
							arqDir.skipBytes((int) ((tamDir/2) * 8));
							long posMetade2 = arqDir.getFilePointer();
							long endAtual;
							for(int i=0; i<tamDir/2; i++) { // varre o arquivo de diretorio copiando os enderecos para a segunda metade
								arqDir.seek(posMetade1);
								endAtual = arqDir.readLong();
								posMetade1 = arqDir.getFilePointer();
								arqDir.seek(posMetade2);
								arqDir.writeLong(endAtual);
								posMetade2 = arqDir.getFilePointer();
							}
							
							// cria um bucket novo
							arqHash.seek(comeco);
							ultimoBucket = arqHash.readInt();
							for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
								arqHash.skipBytes(56);
							}
							posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
							arqHash.writeInt(profLocal);
							arqHash.writeInt(0); // numero de registros
							for(int j=0; j<4; j++) { // escreve valores -1
								arqHash.writeInt(-1); // chave
								arqHash.writeLong(-1); // endereco
							}
							
							// aumenta o ultimoBucket
							ultimoBucket += 1;
							arqHash.seek(comeco);
							arqHash.writeInt(ultimoBucket);
							
							// troca o ponteiro do segundo registro de % atual para o bucket novo
							arqDir.seek(comeco);
							arqDir.readInt(); // pula a profundidade global
							arqDir.skipBytes((int) ((idAtual % Math.pow(2, profGlobal-1)) + Math.pow(2, profGlobal-1)) * 8); // pula para o segundo registro de % atual
							arqDir.writeLong(posBucketNovo);
							
							// rebalanceia o bucket atual com o seu segundo %
							
								// le os registros
							int[] chave = new int [5];
							long[] endereco = new long [5];
							arqHash.seek(endDir);
							arqHash.readInt(); // pula a profundidade
							arqHash.readInt(); // pula o numero de elementos (eh 4)
							for(int i=0; i<4; i++) {
								chave[i] = arqHash.readInt();
								endereco[i] = arqHash.readLong();
							}
								// define em qual posicao vai ficar
							int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
							for(int i=0; i<4; i++) {
								if(idAtual < chave[i]) {
									novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
									i = 4; // break
								} else if(idAtual == chave[i]) {
									System.out.println("Erro: ID duplicada");
									return;
								}
							}
							
								// insere o registro e remaneja os existentes
							for(int i=3; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							
							// insere os registros nos buckets equivalentes
							long posPrimeiro, posSegundo;
							arqHash.seek(endDir); // vai para a posicao do primeiro bucket
							arqHash.readInt(); // pula a profundidade
							posPrimeiro = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
							arqHash.readInt(); // pula a profundidade
							posSegundo = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							int numRegs2;
							for(int i=0; i<5; i++) {
								if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
									arqHash.seek(posPrimeiro);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posPrimeiro);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
									arqHash.seek(posSegundo);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posSegundo);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								}
							}
						}
					}
					
					// se o bucket nao estiver cheio
					else {
						
						// insere o registro no bucket
						
							// le os registros
						int[] chave = new int [4];
						long[] endereco = new long [4];
						for(int i=0; i<numRegs; i++) {
							chave[i] = arqHash.readInt();
							endereco[i] = arqHash.readLong();
						}
						
							// define em qual posicao vai ficar
						int novaPosicao = numRegs; // se for maior que todos a posicao eh a ultima
						for(int i=0; i<numRegs; i++) {
							if(idAtual < chave[i]) {
								novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
								i = numRegs; // break
							} else if(idAtual == chave[i]) {
								System.out.println("Erro: ID duplicada");
								return;
							}
						}
						
							// insere o registro e remaneja os existentes
						if(numRegs > 0) {
							for(int i=numRegs-1; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							numRegs += 1;
						} else {
							chave[0] = idAtual;
							endereco[0] = pos1;
							numRegs = 1;
						}
						
							// aumenta o numero de elementos no bucket
						arqHash.seek(endDir);
						arqHash.readInt(); // profundidade local
						arqHash.writeInt(numRegs);
						
							// grava o bucket novo
						for(int i=0; i<numRegs; i++) {
							arqHash.writeInt(chave[i]);
							arqHash.writeLong(endereco[i]);
						}
					}
				}
			// end while
			}
				
			imprimeArqDir(arqDir, comeco);
			imprimeArqHash(arqHash, comeco);
			System.out.println("\nArquivo inicial de hash criado.\n\nAperte enter para continuar.");
			sc.nextLine();
			arqHash.close();
			arqDir.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void adicionaHash(long comeco, Conta conta, long enderecoArq) {
		// ve no diretorio em qual bucket vai cair
		// acessa o bucket
		// se o bucket ja estiver cheio
			// se a profundidade local for menor que a profundidade global
				// aumenta a profundidade local
				// cria um bucket novo
				// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1)) 
				// rebalanceia o bucket atual com o seu segundo %
				// insere o registro no bucket equivalente
			// se a profundidade local for igual a profundidade global
				// aumenta a profundidade local
				// aumenta a profundidade global
				// dobra o tamanho do diretorio
				// copia os ponteiros antigos do diretorio para os campos novos
				// cria um bucket novo
				// troca o ponteiro do segundo registro de % atual para o bucket novo
				// rebalanceia o bucket atual com o seu segundo %
				// insere o registro no bucket equivalente
		// se o bucket nao estiver cheio
			// insere o registro no bucket
		
		int ultimaId;
		int idAtual;
		Conta contaTemp;
		int tamRegAtual;
		long pos0, pos1, posBucketNovo;
		long endDir;
		int profGlobal;
		int profLocal;
		int numRegs;
		char lapide;
		int ultimoBucket;
		double tamDir;
		
		try {
			
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("dados/diretorio.db", "rw");
			
			// salva a profundidade global
			arqHash.seek(comeco);
			profGlobal = arqHash.readInt();
						
			idAtual = conta.getIdConta();
			pos1 = enderecoArq;
			
			// ve no diretorio em qual bucket vai cair
			endDir = getEndDir(arqDir, comeco, idAtual);
			
			// acessa o bucket
			arqHash.seek(endDir);
			profLocal = arqHash.readInt();
			numRegs = arqHash.readInt();
			
			// se o bucket ja estiver cheio
			if(numRegs == 4) {
				
				// se a profundidade local for menor que a profundidade global
				if(profLocal < profGlobal) {
					
					// aumenta a profundidade local
					profLocal += 1;
					arqHash.seek(endDir);
					arqHash.writeInt(profLocal);
					
					// cria um bucket novo
					arqHash.seek(comeco);
					ultimoBucket = arqHash.readInt();
					for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
						arqHash.skipBytes(56);
					}
					posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
					arqHash.writeInt(profLocal);
					arqHash.writeInt(0); // numero de registros
					for(int j=0; j<4; j++) { // escreve valores -1
						arqHash.writeInt(-1); // chave
						arqHash.writeLong(-1); // endereco
					}
					
					// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1))
					arqDir.seek(comeco);
					profGlobal = arqDir.readInt(); // profundidade global
					tamDir = Math.pow(2, profGlobal);
					for(int i=0; i<tamDir; i++) {
						if(((i >> profLocal-1) % 2) == 1 && (i % Math.pow(2, (profGlobal-1))) == (idAtual % Math.pow(2, (profGlobal-1)))) { // se a id atual for parte da 2a metade das ids que pertencem ao grupo que contem ponteiros para o mesmo bucket 
							arqDir.writeLong(posBucketNovo); // grava a posicao do novo bucket
						}
						else { // senao, pula para o proximo
							arqDir.skipBytes(8);
						}
					}
					
					// rebalanceia o bucket atual com o seu segundo %
						
						// le os registros
					int[] chave = new int [5];
					long[] endereco = new long [5];
					arqHash.seek(endDir);
					arqHash.readInt(); // pula a profundidade
					arqHash.readInt(); // pula o numero de elementos (eh 4)
					for(int i=0; i<4; i++) {
						chave[i] = arqHash.readInt();
						endereco[i] = arqHash.readLong();
					}
						// define em qual posicao vai ficar
					int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
					for(int i=0; i<4; i++) {
						if(idAtual < chave[i]) {
							novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
							i = 4; // break
						} else if(idAtual == chave[i]) {
							System.out.println("Erro: ID duplicada");
							return;
						}
					}
					
						// insere o registro e remaneja os existentes
					for(int i=3; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					
					// insere os registros nos buckets equivalentes
					long posPrimeiro, posSegundo;
					arqHash.seek(endDir); // vai para a posicao do primeiro bucket
					arqHash.readInt(); // pula a profundidade
					posPrimeiro = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
					arqHash.readInt(); // pula a profundidade
					posSegundo = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					int numRegs2;
					for(int i=0; i<5; i++) {
						if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
							arqHash.seek(posPrimeiro);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posPrimeiro);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
							
						} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
							arqHash.seek(posSegundo);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posSegundo);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						}
					}
					
					// aumenta o ultimoBucket
					ultimoBucket += 1;
					arqHash.seek(comeco);
					arqHash.writeInt(ultimoBucket);
				}
				
				// se a profundidade local for igual a profundidade global
				else {
					
					// aumenta a profundidade local
					profLocal += 1;
					arqHash.seek(endDir);
					arqHash.writeInt(profLocal);
					
					// aumenta a profundidade global
					profGlobal += 1;
					arqDir.seek(comeco);
					arqDir.writeInt(profGlobal);
					
					// dobra o tamanho do diretorio
					tamDir = Math.pow(2, profGlobal);
					
					// copia os ponteiros antigos do diretorio para os campos novos
					long posMetade1 = arqDir.getFilePointer();
					arqDir.skipBytes((int) ((tamDir/2) * 8));
					long posMetade2 = arqDir.getFilePointer();
					long endAtual;
					for(int i=0; i<tamDir/2; i++) { // varre o arquivo de diretorio copiando os enderecos para a segunda metade
						arqDir.seek(posMetade1);
						endAtual = arqDir.readLong();
						posMetade1 = arqDir.getFilePointer();
						arqDir.seek(posMetade2);
						arqDir.writeLong(endAtual);
						posMetade2 = arqDir.getFilePointer();
					}
					
					// cria um bucket novo
					arqHash.seek(comeco);
					ultimoBucket = arqHash.readInt();
					for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
						arqHash.skipBytes(56);
					}
					posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
					arqHash.writeInt(profLocal);
					arqHash.writeInt(0); // numero de registros
					for(int j=0; j<4; j++) { // escreve valores -1
						arqHash.writeInt(-1); // chave
						arqHash.writeLong(-1); // endereco
					}
					
					// aumenta o ultimoBucket
					ultimoBucket += 1;
					arqHash.seek(comeco);
					arqHash.writeInt(ultimoBucket);
					
					// troca o ponteiro do segundo registro de % atual para o bucket novo
					arqDir.seek(comeco);
					arqDir.readInt(); // pula a profundidade global
					arqDir.skipBytes((int) ((idAtual % Math.pow(2, profGlobal-1)) + Math.pow(2, profGlobal-1)) * 8); // pula para o segundo registro de % atual
					arqDir.writeLong(posBucketNovo);
					
					// rebalanceia o bucket atual com o seu segundo %
					
						// le os registros
					int[] chave = new int [5];
					long[] endereco = new long [5];
					arqHash.seek(endDir);
					arqHash.readInt(); // pula a profundidade
					arqHash.readInt(); // pula o numero de elementos (eh 4)
					for(int i=0; i<4; i++) {
						chave[i] = arqHash.readInt();
						endereco[i] = arqHash.readLong();
					}
						// define em qual posicao vai ficar
					int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
					for(int i=0; i<4; i++) {
						if(idAtual < chave[i]) {
							novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
							i = 4; // break
						} else if(idAtual == chave[i]) {
							System.out.println("Erro: ID duplicada");
							return;
						}
					}
					
						// insere o registro e remaneja os existentes
					for(int i=3; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					
					// insere os registros nos buckets equivalentes
					long posPrimeiro, posSegundo;
					arqHash.seek(endDir); // vai para a posicao do primeiro bucket
					arqHash.readInt(); // pula a profundidade
					posPrimeiro = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
					arqHash.readInt(); // pula a profundidade
					posSegundo = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					int numRegs2;
					for(int i=0; i<5; i++) {
						if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
							arqHash.seek(posPrimeiro);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posPrimeiro);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
							arqHash.seek(posSegundo);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posSegundo);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						}
					}
				}
			}
			
			// se o bucket nao estiver cheio
			else {
				
				// insere o registro no bucket
				
					// le os registros
				int[] chave = new int [4];
				long[] endereco = new long [4];
				for(int i=0; i<numRegs; i++) {
					chave[i] = arqHash.readInt();
					endereco[i] = arqHash.readLong();
				}
				
					// define em qual posicao vai ficar
				int novaPosicao = numRegs; // se for maior que todos a posicao eh a ultima
				for(int i=0; i<numRegs; i++) {
					if(idAtual < chave[i]) {
						novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
						i = numRegs; // break
					} else if(idAtual == chave[i]) {
						System.out.println("Erro: ID duplicada");
						return;
					}
				}
				
					// insere o registro e remaneja os existentes
				if(numRegs > 0) {
					for(int i=numRegs-1; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					numRegs += 1;
				} else {
					chave[0] = idAtual;
					endereco[0] = pos1;
					numRegs = 1;
				}
				
					// aumenta o numero de elementos no bucket
				arqHash.seek(endDir);
				arqHash.readInt(); // profundidade local
				arqHash.writeInt(numRegs);
				
					// grava o bucket novo
				for(int i=0; i<numRegs; i++) {
					arqHash.writeInt(chave[i]);
					arqHash.writeLong(endereco[i]);
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}
	
	
	public static void imprimeArqHash(RandomAccessFile arq, long comeco) {
		try {
			arq.seek(comeco);
			int numBuckets = arq.readInt()+1;
			int n;
			System.out.println("Numero de buckets: " + numBuckets);
			for(int i=0; i<numBuckets; i++) {
				System.out.print("Bucket " + i + " p local: " + arq.readInt() + " n: ");
				n = arq.readInt();
				System.out.println(n);
				for(int j=0; j<4; j++) {
					System.out.println(" c " + arq.readInt() + " e " + arq.readLong());
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}
	
	
	public static void imprimeArqDir(RandomAccessFile arq, long comeco) {
		try {
			arq.seek(comeco);
			int prof = arq.readInt();
			int n = (int) Math.pow(2, prof);
			System.out.println("Profundidade global: " + prof);
			for(int i=0; i<n; i++) {
				System.out.println(" bucket c " + i + " e " + arq.readLong());
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void deletaHash(long comeco, int idConta) { // deleta um registro do arquivo de hash, id recebida como parametro
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// remove o registro desejado
			// reescreve o bucket sem o registro
			
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// remove o registro desejado
			for(int i=posReg; i<n-1; i++) {
				chave[i] = chave[i+1];
				endereco[i] = endereco[i+1];
			}
			
			// reescreve o bucket sem o registro
			arqHash.seek(endBucket);
			arqHash.readInt(); // pula a profundidade local
			arqHash.writeInt(n-1); // escreve o novo numero de registros
			for(int i=0; i<n-1; i++) {
				arqHash.writeInt(chave[i]); // escreve a chave
				arqHash.writeLong(endereco[i]); // escreve o endereco
			}
			arqHash.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void alteraHash(long comeco, int idConta, long novoEnd) { // altera o endereco do registro no arquivo hash, recebe a id e o endereco novo como parametro
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// altera o registro desejado
			// reescreve o bucket com o novo endereco
			
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// altera o registro desejado
			endereco[posReg] = novoEnd;

			// reescreve o bucket com o novo endereco
			arqHash.seek(endBucket);
			arqHash.readInt(); // pula a profundidade local
			arqHash.readInt(); // pula o numero de elementos
			for(int i=0; i<n; i++) {
				arqHash.writeInt(chave[i]); // escreve a chave
				arqHash.writeLong(endereco[i]); // escreve o endereco
			}
			arqHash.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void buscaHash(RandomAccessFile arq, long comeco) {
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// vai ate o endereco encontrado no arquivo de contas
			// le o registro
			// imprime o registro
			
			
			// pede a id desejada para o usuario
			System.out.println("\n=== BUSCAR UMA CONTA POR HASH ===\n");
			System.out.println("Digite a ID da conta que quer exibir:");
			int idConta = sc.nextInt();
			sc.nextLine();
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// vai ate o endereco encontrado no arquivo de contas
			arq.seek(endereco[posReg]);
			long pos0 = arq.getFilePointer();
			
			// le o registro
			arq.readInt(); // pula o tamanho do registro
			Conta conta = leRegistro(arq, comeco, pos0);
			
			// imprime o registro
			System.out.println("\nEndereço do registro: " + pos0);
			System.out.println(conta.toString());
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
						
			arqHash.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void buscaListaInvertida() {
		// pergunta se o usuario quer nome ou cidade
		System.out.println("\n=== BUSCAR IDS POR LISTA INVERTIDA ===\n");
		System.out.println("Você quer buscar por nome ou cidade? 1) Nome - 2) Cidade");
		int tipo = sc.nextInt();
		sc.nextLine();
		
		if (tipo == 1) { // se for por nome
			// pede o nome desejado ao usuario
			System.out.println("\nDigite o nome desejado:");
			String nome = sc.nextLine();
			
			// chama a funcao de busca passando o nome desejado e o arquivo correto
			listaInvertida.searchList(nome, "dados/listaInvertida/listaInvertidaNome.db");
			
		} else if (tipo == 2) { // se for por cidade
			// pede a cidade desejada ao usuario
			System.out.println("\nDigite a cidade desejada:");
			String cidade = sc.nextLine();
			
			// chama a funcao de busca passando a cidade desejada e o arquivo correto
			listaInvertida.searchList(cidade, "dados/listaInvertida/listaInvertidaCidade.db");
		} else { // se digitou invalido
			System.out.println("\nOpção inválida. Aperte enter para continuar.\n");
			sc.nextLine();
			return;
		}
		
		System.out.println("\nAperte enter para continuar.\n");
		sc.nextLine();
		
		return;
	}
	
	
	public static void opcaoImprimeArquivo(RandomAccessFile arq, long comeco) {
		System.out.println("\nArquivo de contas:");
		imprimeArquivo(arq, comeco);
		System.out.println("\nAperte enter para continuar.");
		sc.nextLine();
	}
	
	
	public static void comprimeLZW(RandomAccessFile arq, long comeco) {
	  /*1. No início o dicionário contém todas as raízes possíveis e I é vazio;
		2. c <= próximo caractere da sequência de entrada;
		3. A string I+c existe no dicionário?
			a. se sim,
				i. I <= I+c;
			b. se não,
				i. coloque a palavra código correspondente a I na sequência codificada;
				ii. adicione a string I+c ao dicionário;
				iii. I <= c;
		4. Existem mais caracteres na sequência de entrada ?
			a. se sim,
				i. volte ao passo 2;
			b. se não,
				ii. coloque a palavra código correspondente a I na sequência codificada;
				iii. FIM.*/
		
		byte byteAtual;
		ArrayList<byte[]> dicionario = new ArrayList<byte[]>();
		byte[] itemAtual;
		byte[] I;
		ArrayList<Byte> sequenciaCodificada = new ArrayList<Byte>();
		
		try {
			arq.seek(comeco);
			
			// inicia o dicionario
			for(int i=0; i<256; i++) {
				itemAtual = new byte[1];
				itemAtual[0] = (byte) i;
				dicionario.add(itemAtual);
			}
			
			//1. No início o dicionário contém todas as raízes possíveis e I é vazio;
			I = new byte[0];
			boolean acabou = false;
			boolean primeiraMetade = true;
			while(!acabou) {
				
				//2. c <= próximo caractere da sequência de entrada;
				byteAtual = arq.readByte();
				
				//3. A string I+c existe no dicionário?
				itemAtual = new byte[I.length + 1];
				for(int i=0; i<I.length; i++) {
					itemAtual[i] = I[i];
				}
				itemAtual[I.length] = byteAtual;
				boolean contem = false;
				for(int i=0; i<dicionario.size(); i++) {
					if(dicionario.get(i).length == itemAtual.length) {
						boolean encontrouTodos = true;
						for (int j=0; j<itemAtual.length; j++) {
							if(itemAtual[j] != dicionario.get(i)[j]) {
								encontrouTodos = false;
							}
						}
						if(encontrouTodos) {
							contem = true;
						}
					}
				}
				
				//a. se sim,
				if(contem) {
					
					//i. I <= I+c;
					I = new byte[itemAtual.length];
					for(int i=0; i<itemAtual.length; i++) {
						I[i] = itemAtual[i];
					}
				} 
				
				//b. se não,
				else {
					
					//i. coloque a palavra código correspondente a I na sequência codificada;
					int posicaoEncontrada = -1;
					for(int i=0; i<dicionario.size(); i++) {
						if(dicionario.get(i).length == I.length) {
							boolean encontrouTodos = true;
							for (int j=0; j<I.length; j++) {
								if(I[j] != dicionario.get(i)[j]) {
									encontrouTodos = false;
								}
							}
							if(encontrouTodos) {
								posicaoEncontrada = i;
							}
						}
					}
					if(primeiraMetade) { // se a sequencia codificada estiver em uma posicao em que eu devo inserir bytes na primeira ordem, entao adiciona os dois bytes da posicao de I. Senao, usa os ultimos 4 bits do ultimo byte da sequencia e adiciona o segundo byte.
						byte byte1 = (byte) ((posicaoEncontrada >> 4) & 0xFF);
						byte byte2 = (byte) ((posicaoEncontrada << 4) & 0xF0);
						sequenciaCodificada.add(byte1);
						sequenciaCodificada.add(byte2);
						primeiraMetade = false; 
					} else {
						byte byte1 = (byte) (((posicaoEncontrada >> 8) & 0xF) | sequenciaCodificada.get(sequenciaCodificada.size() - 1));
						byte byte2 = (byte) (posicaoEncontrada & 0xFF);
						sequenciaCodificada.remove(sequenciaCodificada.size() - 1);
						sequenciaCodificada.add(byte1);
						sequenciaCodificada.add(byte2);
						primeiraMetade = true;
					}
					
					//ii. adicione a string I+c ao dicionário;
					dicionario.add(itemAtual);
					
					//iii. I <= c;
					I = new byte[1];
					I[0] = byteAtual;
				}
				
				//4. Existem mais caracteres na sequência de entrada ?
					//a. se sim,
						//i. volte ao passo 2;
				//b. se não,
				if(arq.getFilePointer() == arq.length() - 1) {
					
					//ii. coloque a palavra código correspondente a I na sequência codificada;
					int modAtual = I[0];
					ArrayList<Byte> sequenciaAtual = new ArrayList<Byte>();
					for(int i=0; i<12; i++) {
						sequenciaAtual.add((byte) (modAtual & 1));
						modAtual = modAtual >> 1;
					}
										
					//iii. FIM.
					acabou = true;
					
					//salva no arquivo
					RandomAccessFile arqComprimido = new RandomAccessFile("dados/contasLZW.db", "rw");
					arqComprimido.seek(comeco);
					arqComprimido.setLength(0);
					for(Byte p : sequenciaCodificada) {
						arqComprimido.writeByte(p);
					}
					System.out.println("\nArquivo inicial: " + arq.length() + " bytes");
					System.out.println("Arquivo final: " + arqComprimido.length() + " bytes");
					System.out.println("Compressão: " + (((float) arqComprimido.length() / arq.length()) * 100) + "% do tamanho");
					System.out.println("\nAperte enter para continuar.");
					sc.nextLine();
					arqComprimido.close();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void descomprimeLZW(RandomAccessFile arq, long comeco) {
	  /*1. No início o dicionário contém todas as raízes possíveis;
		2. cW <= primeira palavra código na sequência codificada (sempre é uma raiz);
		3. Coloque a string(cW) na sequência de saída;
		4. pW <= cW;
		5. cW <= próxima palavra código da sequência codificada;
		6. A string(cW) existe no dicionário ?
			a. se sim,
				i. coloque a string(cW) na sequência de saída;
				ii. P <= string(pW);
				iii. C <= primeiro caracter da string(cW);
				iv. adicione a string P+C ao dicionário;
			b. se não,
				i. P <= string(pW);
				ii. C <= primeiro caracter da string(pW);
				iii. coloque a string P+C na sequência de saída e adicione-a ao dicionário;
		7. Existem mais palavras código na sequência codificada ?
			a. se sim,
				i. volte ao passo 4;
			b. se não,
				i. FIM.*/
		

		try {
			// lê o arquivo codificado
			RandomAccessFile arqComprimido = new RandomAccessFile("dados/contasLZW.db", "rw");
			if(arqComprimido.length() == 0) {
				System.out.println("\nArquivo vazio.\n\nAperte enter para continuar.\n");
				sc.nextLine();
				arqComprimido.close();
				return;
			}
			
			ArrayList<Integer> sequenciaCodificada = new ArrayList<Integer>();
			boolean primeiraMetade = true;
			long pos0 = arqComprimido.getFilePointer();
			for(int i=0; arqComprimido.getFilePointer() < arqComprimido.length()-1; i++) {
				if(primeiraMetade) {
					int aux = arqComprimido.readUnsignedByte();
					pos0 = arqComprimido.getFilePointer();
					aux = aux << 4;
					aux = aux | (((int) arqComprimido.readUnsignedByte() >> 4) & 15);
					sequenciaCodificada.add(aux);
					primeiraMetade = false;
					arqComprimido.seek(pos0);
				} else {
					int aux;
					aux = arqComprimido.readUnsignedByte() & 15;
					aux = aux << 8;
					int x = arqComprimido.readUnsignedByte();
					aux = aux | x ;
					sequenciaCodificada.add(aux);
					primeiraMetade = true;
				}
			}
			
			// 1. No início o dicionário contém todas as raízes possíveis;
			// inicia o dicionario
			ArrayList<byte[]> dicionario = new ArrayList<byte[]>();
			ArrayList<Byte> sequenciaSaida = new ArrayList<Byte>();
			int cW, pW;
			byte[] stringC, stringP;
			byte[] itemAtual;
			byte c;
			byte[] p;
			for(int i=0; i<256; i++) {
				itemAtual = new byte[1];
				itemAtual[0] = (byte) i;
				dicionario.add(itemAtual);
			}
			
			// 2. cW <= primeira palavra código na sequência codificada (sempre é uma raiz);
			cW = sequenciaCodificada.get(0);
			
			// 3. Coloque a string(cW) na sequência de saída;
			stringC = dicionario.get(cW);
			for(int i=0; i<stringC.length; i++) {
				sequenciaSaida.add(stringC[i]);
			}
			
			// 4. pW <= cW;
			boolean acabou = false;
			if(sequenciaCodificada.size() == 1) {
				acabou = true;
			}
			int indiceSeqCod = 1;
			while(!acabou) {
				pW = cW;
				
				// 5. cW <= próxima palavra código da sequência codificada;
				cW = sequenciaCodificada.get(indiceSeqCod);
				indiceSeqCod++;
				
				// 6. A string(cW) existe no dicionário ?
				boolean existe = false;
				if(cW < dicionario.size()) {
					existe = true;
					stringC = dicionario.get(cW);
				} else {
					existe = false;
				}
				
				// a. se sim,
				if(existe) {
					
					// i. coloque a string(cW) na sequência de saída;
					for(int i=0; i<stringC.length; i++) {
						sequenciaSaida.add(stringC[i]);
					}
					
					// ii. P <= string(pW);
					stringP = dicionario.get(pW);
					p = stringP;
					
					// iii. C <= primeiro caracter da string(cW);
					c = stringC[0];
					
					// iv. adicione a string P+C ao dicionário;
					byte[] stringParaAdicionar = new byte[p.length + 1];
					for(int i=0; i<p.length; i++) {
						stringParaAdicionar[i] = p[i];
					}
					stringParaAdicionar[p.length] = c;
					dicionario.add(stringParaAdicionar);
				}
				
				// b. se não,
				else {
					
					// i. P <= string(pW);
					stringP = dicionario.get(pW);
					p = stringP;
					
					// ii. C <= primeiro caracter da string(pW);
					c = stringC[0];
					
					// iii. coloque a string P+C na sequência de saída e adicione-a ao dicionário;
					byte[] stringParaAdicionar = new byte[p.length + 1];
					for(int i=0; i<p.length; i++) {
						stringParaAdicionar[i] = p[i];
					}
					stringParaAdicionar[p.length] = c;
					dicionario.add(stringParaAdicionar);
					for(int i=0; i<stringParaAdicionar.length; i++) {
						sequenciaSaida.add(stringParaAdicionar[i]);
					}
				}
				
				// 7. Existem mais palavras código na sequência codificada ?
					// a. se sim,
						// i. volte ao passo 4;
					// b. se não,
						// i. FIM.
				if(indiceSeqCod == sequenciaCodificada.size()) {
					acabou = true;
				}
			}
			
			// salva os dados no arquivo
			arq.seek(comeco);
			arq.setLength(0);
			for(Byte byteAtual : sequenciaSaida) {
				arq.writeByte(byteAtual);
			}
			
			System.out.println("\nArquivo decodificado:");
			imprimeArquivo(arq, comeco);
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
			
			
			arqComprimido.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void opcaoHuffman(RandomAccessFile arq, long comeco) {
		try {
			HuffmanC.handleNewText(sc,arq);
			RandomAccessFile arqComprimido = new RandomAccessFile("dados/contasHuffman.db", "rw");
			System.out.println("\nArquivo codificado com sucesso.");
			System.out.println("\nArquivo inicial: " + arq.length() + " bytes");
			System.out.println("Arquivo final: " + arqComprimido.length() + " bytes");
			System.out.println("Compressão: " + (((float) arqComprimido.length() / arq.length()) * 100) + "% do tamanho");
			
			System.out.println("\nArquivo decodificado:");
			imprimeArquivo(arq, comeco);
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
			arqComprimido.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void buscaPadrao (RandomAccessFile arq, long comeco) {
		int idAtual = -1;
		ArrayList<Integer> contasEncontradas = new ArrayList<Integer>();
		int ultimaId;
		
		try {
			arq.seek(comeco);
			ultimaId = arq.readInt();
			
			// pergunta para o usuario qual sera o texto a ser procurado
			System.out.println("\nDigite o texto que deseja buscar: (máximo 64 caracteres)");
			String padrao = sc.nextLine();
			
			// para cada registro no arquivo
			while(idAtual != ultimaId) {
				boolean achou = false;
				numOp = 0;
				
				// le o registro
				int tamReg = arq.readInt(); // tamanho do registro
				long pos0 = arq.getFilePointer();
				char lapide = arq.readChar();
				arq.seek(pos0);
				Conta conta = leRegistro(arq, comeco, pos0);
				if(lapide != '*') {
					idAtual = conta.getIdConta();
					
					
					// checa se o padrao procurado existe no nome
					if(shiftAnd(conta.getNomePessoa(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe no nome de usuario
					if(shiftAnd(conta.getNomeUsuario(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe no email
					for(int i=0; i<conta.getEmail().length; i++) {
						if(shiftAnd(conta.getEmail()[i], padrao)) {
							achou = true;
						}
					}
					
					// checa se o padrao procurado existe no cpf
					if(shiftAnd(conta.getCpf(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe na cidade
					if(shiftAnd(conta.getCidade(), padrao)) {
						achou = true;
					}
					
					// se achou, adiciona ao array de contas encontradas
					if(achou) {
						contasEncontradas.add(conta.getIdConta());
					}
				}
				
				// pula para o proximo registro
				arq.seek(pos0);
				arq.skipBytes(tamReg);
			}
			
			// imprime as contas encontradas
			System.out.println("\nOperações realizadas: " + numOp);
			if(contasEncontradas.size() > 0) {
				System.out.println("\nTexto encontrado nas contas:");
				for(Integer p : contasEncontradas) {
					System.out.print(p);
					if(p != contasEncontradas.get(contasEncontradas.size() -1)) {
						System.out.print(", ");
					} else {
						System.out.println("\n\nAperte enter para continuar.");
						sc.nextLine();
					}
				}
			} else {
				System.out.println("\nTexto não encontrado no arquivo.");
				System.out.println("\nAperte enter para continuar.");
				sc.nextLine();
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	
	public static boolean shiftAnd(String texto, String padrao) { // retorna true se o padrao foi encontrado e false se nao foi
		boolean encontrou = false;
		
		// se o padrao cabe no texto
		if(texto.length() >= padrao.length()) {
			
			// carrega a lista de caracteres para fazer as mascaras
			String caracteres = "";
			for(int i=0; i<padrao.length(); i++) {
				boolean jaTem = false;
				for(int j=0; j<caracteres.length(); j++) {
					if(caracteres.charAt(j) == padrao.charAt(i)) {
						jaTem = true;
					}
				}
				if(!jaTem) {
					caracteres += padrao.charAt(i);
				}
			}
			
			// carrega as mascaras
			long[] mascara = new long[caracteres.length() + 1];
			for(int i=0; i<mascara.length-1; i++) {
				mascara[i] = 0;
				for(int j=0; j<padrao.length(); j++) {
					if(caracteres.charAt(i) == padrao.charAt(j)) {
						mascara[i] = mascara[i] | ((long) 1 << (63-j));
					}
				}
			}
			mascara[caracteres.length()] = 0;
			
			// faz o shift-and
			long resultadoAnterior = (long) 1 << 63;
			long resultadoAtual;
			for(int i=0; i<texto.length(); i++) {
				char caracterAtual = texto.charAt(i);
				int indiceCharAtual = caracteres.length();
				for(int j=0; j<caracteres.length(); j++) {
					if(caracterAtual == caracteres.charAt(j)) {
						indiceCharAtual = j;
					}
				}
				
				// faz o and
				resultadoAtual = resultadoAnterior & mascara[indiceCharAtual];
				
				// faz o shift
				resultadoAnterior = (resultadoAtual >> 1) | ((long) 1 << 63);
				
				// verifica se encontrou
				if((resultadoAtual & ((long) 1 << (63 - (padrao.length() - 1)))) == ((long) 1 << (63 - (padrao.length() - 1)))) {
					encontrou = true;
				}
				
				numOp++;
			}
			
			return encontrou;
		} else {
			return false;
		}
	}
	
	
	public static void main(String[] args) {
	    long comeco;
	    boolean sair = false;
	    String opcao;
	    Conta conta1 = new Conta();
	    
	    try {
	    	RandomAccessFile arq = new RandomAccessFile("dados/contas.db", "rw"); // abre o arquivo de contas
	    	comeco = arq.getFilePointer();
	    	if(arq.length()==0) { // se o arquivo estiver vazio, escreve que a ultima id eh -1 no cabecalho
	    		arq.writeInt(-1);
	    		arq.seek(comeco); // volta pro inicio do arquivo
	    	}
	    	
	    	while(!sair) { // mostra o menu enquanto a pessoa nao escolher sair
	    		System.out.println("=== SISTEMA DE CONTAS ===\n");
	    		System.out.println("Escolha uma opção:");
	    		System.out.println("1) Criar uma conta");
	    		System.out.println("2) Buscar uma conta");
	    		System.out.println("3) Fazer uma transferência");
	    		System.out.println("4) Alterar uma conta");
	    		System.out.println("5) Deletar uma conta");
	    		System.out.println("6) Intercalação balanceada comum");
	    		System.out.println("7) Criar arquivo de índice hash");
	    		System.out.println("8) Buscar uma conta via arquivo de índice hash");
	    		System.out.println("9) Buscar IDs por lista invertida");
	    		System.out.println("10) Imprimir o arquivo de dados");
	    		System.out.println("11) Comprimir o arquivo de dados usando LZW");
	    		System.out.println("12) Decodificar o arquivo de dados criado com LZW");
	    		System.out.println("13) Codificar e decodificar o arquivo de dados usando Huffman");
	    		System.out.println("14) Buscar um texto no arquivo de dados via Shift-And Exato");
	    		System.out.println("S) Sair");
	    		opcao = sc.nextLine();
	    		switch(opcao) { // trata as opcoes
	    			case "1":
	    				conta1 = criar(arq, comeco);
	    				break;
	    			case "2":
	    				conta1 = buscar(arq, comeco);
	    				break;
	    			case "3":
	    				transferir(arq, comeco);
	    				break;
	    			case "4":
	    				conta1 = alterar(arq, comeco);
	    				break;
	    			case "5":
	    				deletar(arq, comeco);
	    				break;
	    			case "6":
	    				intercalacaoBalanceada(arq, comeco);
	    				break;
	    			case "7":
	    				criaHash(arq, comeco);
	    				break;
	    			case "8":
	    				buscaHash(arq, comeco);
	    				break;
	    			case "9":
	    				buscaListaInvertida();
	    				break;
	    			case "10":
	    				opcaoImprimeArquivo(arq, comeco);
	    				break;
	    			case "11":
	    				comprimeLZW(arq, comeco);
	    				break;
	    			case "12":
	    				descomprimeLZW(arq, comeco);
	    				break;
	    			case "13":
	    				opcaoHuffman(arq, comeco);
	    				break;
	    			case "14":
	    				buscaPadrao(arq, comeco);
	    				break;
	    			case "s":
	    				sair = true;
	    				System.out.println("Saindo...");
	    				sc.close();
	    				break;
	    			case "S":
	    				sair = true;
	    				System.out.println("Saindo...");
	    				sc.close();
	    				break;
	    			default:
	    				System.out.println("\nOpção inválida. Tente novamente.\n\n");
	    				break;
	    		}
	    	}
	    }catch(IOException e) {
	    	System.out.println(e.getMessage());
	    }
	}
}
