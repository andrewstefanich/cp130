package edu.uw.astef1.dao;

import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.dao.DaoFactoryException;

/**
 * Implementation class for the {@link DaoFactory} interface. Creates instances of FileAccountDao.
 * 
 * @author AndrewStefanich
 *
 */
public class FileDaoFactory implements DaoFactory {
	
	/**
	 * No-argument constructor, for JavaBean
	 */
	public FileDaoFactory(){
		
	}

	/**
	 * Instantiates a AccountDao object.
	 * 
	 * @return a newly instantiated AccountDao object
	 * @throws DaoFactoryException
	 *             if unable to instantiate the AccountDao object
	 */
	@Override
	public AccountDao getAccountDao() throws DaoFactoryException {
		AccountDao fileAccountDao = new FileAccountDao();
		return fileAccountDao;
	}

}
