package dao;



import java.util.List;

import com.db4o.query.Query;

import modelo.Usuario;

public class DAOUsuario extends DAO<Usuario> {

	public Usuario read(Object chave) {
	String nome = (String) chave;
	Query q = manager.query();
	q.constrain(Usuario.class);
	q.descend("nome").constrain(nome);
	List<Usuario> result = q.execute();
	if (result.size() > 0)
		return (Usuario) result.get(0);
	else
		return null;
	}
	public boolean verificaLogado(Usuario user) {
		return user.ativo()?true:false;
	}
	

}
