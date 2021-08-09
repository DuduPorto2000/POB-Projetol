package dao;
import java.util.List;

import com.db4o.query.Query;

import modelo.Mensagem;

public class DAOMensagem  extends DAO<Mensagem> {
	
	public Mensagem read(Object chave) {
		int id = (Integer) chave;
		Query q = manager.query();
		q.constrain(Mensagem.class);
		q.descend("id").constrain(id);
		List<Mensagem> result = q.execute();
		if (result.size() > 0)
			return result.get(0);
		else
			return null;
	}
	public static List<Mensagem> queryMSGs(String termo) {
		Query q = manager.query();
		q.constrain(Mensagem.class);
		q.descend("texto").constrain(termo).like();
		List<Mensagem> result = q.execute();
		if (result.size() > 0)
			return result;
		else
			return null;
	}
}
