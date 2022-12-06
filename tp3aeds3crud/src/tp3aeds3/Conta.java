package tp3aeds3;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Conta {
	private char lapide;
	private int idConta;
	private String nomePessoa;
	private String[] email;
	private String nomeUsuario;
	private String senha;
	private String cpf;
	private String cidade;
	private int transferenciasRealizadas;
	private float saldoConta;
	
	public Conta() {
		super();
		this.lapide = ' ';
		this.idConta = -1;
		this.nomePessoa = "";
		this.email = null;
		this.nomeUsuario = "";
		this.senha = "";
		this.cpf = "00000000000";
		this.cidade = "";
		this.transferenciasRealizadas = 0;
		this.saldoConta = 0F;
	}
	public Conta(int idConta, String nomePessoa, String[] email, String nomeUsuario, String senha, String cpf,
			String cidade, int transferenciasRealizadas, float saldoConta) {
		super();
		this.lapide = ' ';
		this.idConta = idConta;
		this.nomePessoa = nomePessoa;
		this.email = email;
		this.nomeUsuario = nomeUsuario;
		this.senha = senha;
		if(cpf.length()==11)
			this.cpf = cpf;
		else
			this.cpf = "00000000000";
		this.cidade = cidade;
		this.transferenciasRealizadas = transferenciasRealizadas;
		this.saldoConta = saldoConta;
	}
	public char getLapide() {
		return lapide;
	}
	public void setLapide(char lapide) {
		this.lapide = lapide;
	}
	public int getIdConta() {
		return idConta;
	}
	public void setIdConta(int idConta) {
		this.idConta = idConta;
	}
	public String getNomePessoa() {
		return nomePessoa;
	}
	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}
	public String[] getEmail() {
		return email;
	}
	public void setEmail(String[] email) {
		this.email = email;
	}
	public String getNomeUsuario() {
		return nomeUsuario;
	}
	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		if(cpf.length()==11)
			this.cpf = cpf;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public int getTransferenciasRealizadas() {
		return transferenciasRealizadas;
	}
	public void setTransferenciasRealizadas(int transferenciasRealizadas) {
		this.transferenciasRealizadas = transferenciasRealizadas;
	}
	public float getSaldoConta() {
		return saldoConta;
	}
	public void setSaldoConta(float saldoConta) {
		this.saldoConta = saldoConta;
	}
	
	public int length() {
		int tamanho = -1;
		//tamanho = Integer.SIZE /*idconta*/ + 
		//	 	  Integer.SIZE /*nomepessoa length*/ + 
		//	 	  nomePessoa.length()*8 /*nomepessoa*/ + 
		//		  Integer.SIZE /*email[] length*/;
		//for(int i=0; i<email.length; i++) {
        //	tamanho += Integer.SIZE /*email[i] length*/ + 
        //			   email[i].length()*8 /*email[i]*/;
        //}
		//tamanho += Integer.SIZE /*nomeusuario length*/ + 
		//		   nomeUsuario.length()*8 /*nomeusuario*/ + 
		//		   Integer.SIZE /*senha length*/ + 
		//		   senha.length()*8 /*senha*/ + 
		//		   11*8 /*cpf*/ + 
		//		   Integer.SIZE /*cidade length*/ + 
		//		   cidade.length()*8 /*cidade*/ + 
		//		   Integer.SIZE /*transferenciasrealizadas*/ + 
		//		   Float.SIZE /*saldoconta*/;
		try {
			tamanho = toByteArray().length;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return tamanho;
	}
	
	public byte[] toByteArray() throws IOException{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeChar(lapide);
        dos.writeInt(idConta);
        dos.writeInt(nomePessoa.length());
        dos.writeUTF(nomePessoa);
        dos.writeInt(email.length);
        for(int i=0; i<email.length; i++) {
        	dos.writeInt(email[i].length());
        	dos.writeUTF(email[i]);
        }
        dos.writeInt(nomeUsuario.length());
        dos.writeUTF(nomeUsuario);
        dos.writeInt(senha.length());
        dos.writeUTF(senha);
        dos.writeUTF(cpf);
        dos.writeInt(cidade.length());
        dos.writeUTF(cidade);
        dos.writeInt(transferenciasRealizadas);
        dos.writeFloat(saldoConta);
        
        
        return baos.toByteArray();
    }
	
	public String toString() {
		String s = "";
		s = "\nID: " + idConta +
			"\nNome: " + nomePessoa;
			for(int i=1; i<=email.length; i++) {
	        	s = s + "\nEmail " + i + ": " + email[i-1];
	        }
		s = s + "\nNome de usuário: " + nomeUsuario +
			"\nSenha: " + senha +
			"\nCPF: " + cpf +
			"\nCidade: " + cidade +
			"\nTransferências realizadas: " + transferenciasRealizadas +
			"\nSaldo da conta: " + saldoConta;
		return s;
	}
}
