package dao;

import java.util.List;

import com.db4o.query.Query;

import modelo.Log;

public class DAOLog extends DAO<Log>{

	public Log read(Object chave) {
	String nome = (String) chave;
	Query q = manager.query();
	q.constrain(Log.class);
	q.descend("nome").constrain(nome);
	List<Log> result = q.execute();
	if (result.size() > 0)
		return (Log) result.get(0);
	else
		return null;
	}

}
