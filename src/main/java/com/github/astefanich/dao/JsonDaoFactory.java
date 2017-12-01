package com.github.astefanich.dao;

import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.dao.DaoFactoryException;

/**
 * Implementation class for the {@link DaoFactory} interface, for JSON files.
 * 
 * @author AndrewStefanich
 *
 */
public class JsonDaoFactory implements DaoFactory {

	/**
	 * Creates instances of JsonAccountDao.
	 * 
	 * @return a new JsonAccountDao instance.
	 * @throws DaoFactoryException
	 *             if instantiation fails.
	 */
	@Override
	public AccountDao getAccountDao() throws DaoFactoryException {
		try {
			return new JsonAccountDao();
		} catch (final AccountException e){
			throw new DaoFactoryException("Factory failed to instantiate a JsonAccountDao: " + e);
		}
	}

}
