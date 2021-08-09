package fachada;

import java.util.List;

import org.graalvm.compiler.lir.aarch64.AArch64ControlFlow.ReturnOp;

import dao.DAO;
import dao.DAOLog;
import dao.DAOMensagem;
import dao.DAOUsuario;
import modelo.Log;
import modelo.Mensagem;
import modelo.Usuario;

public class Fachada {
	private static DAOUsuario daousuario = new DAOUsuario();  
	private static DAOMensagem daomensagem = new DAOMensagem();  
	private static DAOLog daolog = new DAOLog();  

	private static Usuario usuariologado=null;


	public static void inicializar() {
		DAO.open();
	}

	public static void finalizar(){
		DAO.close();
	}

	public static List<Usuario> listarUsuarios() {
		// nao precisa estar logado
		return daousuario.readAll();	
	}
	public static List<Mensagem> listarMensagens() {
		// nao precisa estar logado
		return daomensagem.readAll();	
	}

	public static List<Log> listarLogs() {
		// nao precisa estar logado
		return daolog.readAll();	
	}
	public static List<Mensagem> buscarMensagens(String termo) throws  Exception{
		
		
		/*
		 * nao precisa estar logado
		 * query no banco para obter mensagens do grupo que contenha
		 *  o termo (considerar case insensitive)
		 * 
		 */
		DAO.begin();
		List<Mensagem> retorno = DAOMensagem.queryMSGs(termo);
		if(retorno.isEmpty()) {
			DAO.rollback();
			throw new Exception("não existe mensagem com este termo.");
		}
		DAO.commit();
		return retorno;
	}

	public static Usuario criarUsuario(String nome, String senha) throws  Exception{
		// nao precisa estar logado
		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u != null) {
			DAO.rollback();	
			throw new Exception("criar usuario - usuario existente:" + nome);
		}

		u = new Usuario(nome+"/"+senha);
		daousuario.create(u);		
		DAO.commit();
		return u;
	}


	public static void login(String nome, String senha) throws Exception{		
		//verificar se ja existe um usuario logada
		if(usuariologado!=null)
			throw new Exception ("ja existe um usuario logado"+getLogado());

		DAO.begin();	
		Usuario u = daousuario.read(nome+"/"+senha);	
		if(u == null) {
			DAO.rollback();	
			throw new Exception("login - usuario inexistente:" + nome);
		}
		if(!u.ativo()) {
			DAO.rollback();	
			throw new Exception("login - usuario nao ativo:" + nome);
		}
		usuariologado = u;		//altera o logado na fachada

		Log log = new Log(usuariologado.getNome());
		daolog.create(log);
		DAO.commit();
	}
	public static void logoff() {		
		usuariologado = null; 		//altera o logado na fachada
	}

	public static Usuario getLogado() {
		return usuariologado;
	}



	public static Mensagem criarMensagem(String texto) throws Exception{
		/*
		 * tem que esta logado
		 * criar a mensagem, onde o criador é a usuario logada
		 * adicionar esta mensagem na lista de mensagens de cada usuario do grupo,
		 * incluindo a do criador
		 * retornar mensagem criada
		 */

		//para gerar o novo id da mensagem utilize:
		DAO.begin();
		Usuario usuariologado = getLogado();
		if(usuariologado == null) {
			DAO.rollback();
			throw new Exception("Usuário não está logado.");
		}
		int id = daomensagem.obterUltimoId();
		id++;
		Mensagem m = new Mensagem(id, usuariologado, texto);
		daomensagem.create(m);
		usuariologado.adicionar(m);
		DAO.commit();
		return m;
		
	}



	public static List<Mensagem> listarMensagensUsuario() throws Exception{
		
		if (getLogado() != null)
			return getLogado().getMensagens();
		else
			throw new Exception("O usuario não está logado");
		/*
		 * tem que esta logado
		 * retorna todas as mensagens do usuario logado
		 * 
		 */
	}


	public static void apagarMensagens(int... ids) throws  Exception{
		/*
		 * tem que esta logado
		 * recebe uma lista de numeros de id 
		 * (id é um numero entre 1 a N, onde N é a quatidade atual de mensagens do grupo)
		 * validar se ids são de mensagens criadas pelo usuario logado
		 * (um usuario nao pode apagar mensagens de outros usuarios)
		 * 
		 * remover cada mensagem da lista de mensagens do usuario logado
		 * apagar cada mensagem do banco 
		 */
		DAO.begin();
		Usuario usuariologado = getLogado();
		if(usuariologado == null) {
			DAO.rollback();
			throw new Exception("Usuário não está logado.");
		}
		int size = daomensagem.readAll().size();
		for (int i : ids) {
			if(i > size) {
				DAO.rollback();
				throw new Exception("Mensagem não encontrada.");
			}
			Mensagem m = daomensagem.read(i);
			if(!usuariologado.getMensagens().contains(m)) {
				DAO.rollback();
				throw new Exception("Mensagem não pertence ao usuário logado.");
			}
			if(m == null) {
				DAO.rollback();
				throw new Exception("Mensagem não encontrada.");
			}
			daomensagem.delete(m);
			usuariologado.remover(m);
		}
		DAO.commit();
		
		
	}

	public static void sairDoGrupo() throws  Exception{
		/*
		 * tem que esta logado
		 * 
		 * criar a mensagem "fulano saiu do grupo"
		 * desativar o usuario logado e fazer logoff dele
		 */
	}


//	public static int totalMensagensUsuario() throws Exception{
//		/*
//		 * tem que esta logado
//		 * retorna total de mensagens criadas pelo usuario logado
//		 * 
//		 */
//	}

	public static void esvaziar() throws Exception{
		DAO.clear();
	}

}

