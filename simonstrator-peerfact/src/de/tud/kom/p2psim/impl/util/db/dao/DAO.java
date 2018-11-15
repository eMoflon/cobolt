/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 *
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.util.db.dao;

import java.util.ArrayList;
import java.util.LinkedList;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import de.tud.kom.p2psim.impl.util.db.metric.CustomMeasurement;
import de.tud.kom.p2psim.impl.util.db.metric.Experiment;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetric;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetricBound;
import de.tud.kom.p2psim.impl.util.db.metric.Measurement;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementPair;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementPairList;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementSingle;
import de.tud.kom.p2psim.impl.util.db.metric.Metric;
import de.tud.kom.p2psim.impl.util.db.metric.MetricDescription;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This class represents a simple access to persist objects with Hibernate. It
 * provides simple methods to store Objects. Additionally it exists a queue,
 * which has the task to increase the performance. The queue has the benefit,
 * that at once can persist a huge amount of data.<br>
 * <br>
 * For the easy store of POJOs, the methods persistImmediately,
 * addToPersistQueue and commitQueue are of interest.
 *
 * @author Christoph Münker
 * @author Andreas Hemel
 */
public class DAO {

//	/**
//	 * The actually session for all DAOs
//	 */
//	private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();
//
//	/**
//	 * The session factory from Hibernate
//	 */
//	private static EntityManagerFactory emf;

	private static final int QUEUE_SIZE = 20000;

	/**
	 * The queue for the big amount of data
	 */
	private static ArrayList<Object> persistQueue = new ArrayList<Object>(
			QUEUE_SIZE);

	private static final LinkedList<Thread> threads = new LinkedList<Thread>();

	private static int maxConnections = 1;

	private static long objectCount = 0;

	private static long commitCount = 0;

	private static long commitTime = 0;

//	public static Map<String, Object> configOverwrites = new HashMap<String, Object>();

	private static ArrayList<Class<?>> daoClasses = new ArrayList<Class<?>>();
	
	public static String database;
	
	public static String username;
	
	public static String password;

	private static SessionFactory sessionFactory;

	private static final ThreadLocal<Session> session = new ThreadLocal<Session>();
	
	private static ServiceRegistry serviceRegistry;

	/**
	 * Hibernate-only implementation to support annotated classes without active
	 * binding
	 * 
	 * @return
	 */
	public static Session getSession() {
		Session currSession = DAO.session.get();
		if (sessionFactory == null) {
			try {
				Configuration cfg = new Configuration();
				/*
				 * TODO config
				 */
				cfg.setProperty("hibernate.show_sql", "false");
				cfg.setProperty("hibernate.format_sql", "false");
				cfg.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
				// Default user/pass/database, overwrite using MetricOutputDAO
				cfg.setProperty("hibernate.connection.url", "jdbc:mysql://localhost/"+database);
				cfg.setProperty("hibernate.connection.username", username);
				cfg.setProperty("hibernate.connection.password", password);
	
				// mySQL 5
				cfg.setProperty("hibernate.dialect",
						"org.hibernate.dialect.MySQL5InnoDBDialect");
	
				cfg.setProperty("hibernate.transaction.factory_class",
						"org.hibernate.transaction.JDBCTransactionFactory");
				cfg.setProperty("hibernate.pool_size", "1");
				cfg.setProperty("hibernate.hbm2ddl.auto", "update");
				cfg.setProperty("hibernate.current_session_context_class", "thread");
				
				// Add core classes
				cfg.addAnnotatedClass(CustomMeasurement.class);
				cfg.addAnnotatedClass(Experiment.class);
				cfg.addAnnotatedClass(HostMetric.class);
				cfg.addAnnotatedClass(HostMetricBound.class);
				cfg.addAnnotatedClass(Measurement.class);
				cfg.addAnnotatedClass(MeasurementPair.class);
				cfg.addAnnotatedClass(MeasurementPairList.class);
				cfg.addAnnotatedClass(MeasurementSingle.class);
				cfg.addAnnotatedClass(Metric.class);
				cfg.addAnnotatedClass(MetricDescription.class);
				
				for (Class<?> clazz : daoClasses) {
					cfg.addAnnotatedClass(clazz);
				}
				serviceRegistry = new StandardServiceRegistryBuilder()
						.applySettings(cfg.getProperties()).build();
				sessionFactory = cfg.buildSessionFactory(serviceRegistry);
			} catch (Throwable ex) {
				throw new ExceptionInInitializerError(ex);
			}
		}
		if (currSession == null) {
			currSession = sessionFactory.openSession();
			Monitor.log(DAO.class, Level.DEBUG, "Opening hibernate session.");
			DAO.session.set(currSession);
		}
		return currSession;
	}

	/**
	 * Gets the session
	 *
	 * @return
	 */
//	public static EntityManager getEntityManager() {
//		EntityManager em = DAO.entityManager.get();
//		if (emf == null) {
//			/*
//			 * Setting hibernate properties here, to remove dependency on
//			 * persistence.xml. Please note: set your database, user, and
//			 * password in your XML-Config, using the MetricOutputDAO.
//			 */
//			Properties properties = new Properties();
//
//			// Disable output
//			properties.put("hibernate.show_sql", "false");
//			properties.put("hibernate.format_sql", "false");
//			// Connection-driver for mySQL
//			properties.put("hibernate.connection.driver_class",
//					"com.mysql.jdbc.Driver");
//			// Default user/pass/database, overwrite using MetricOutputDAO
//			properties.put("hibernate.connection.url", "");
//			properties.put("hibernate.connection.username", "");
//			properties.put("hibernate.connection.password", "");
//
//			// mySQL 5
//			properties.put("hibernate.dialect",
//					"org.hibernate.dialect.MySQL5InnoDBDialect");
//
//			properties.put("hibernate.transaction.factory_class",
//					"org.hibernate.transaction.JDBCTransactionFactory");
//			properties.put("hibernate.pool_size", "1");
//			properties.put("hibernate.hbm2ddl.auto", "update");
//			properties.put("hibernate.current_session_context_class", "thread");
//
//			properties.put("hibernate.ejb.loaded.classes", daoClasses);
//
//			// overwrite user, table, and pass
//			properties.putAll(configOverwrites);
//
//			emf = Persistence.createEntityManagerFactory("PeerfactSIM-EM",
//					properties);
//		}
//		if (em == null) {
//			em = emf.createEntityManager();
//			DAO.entityManager.set(em);
//		}
//		return em;
//	}

	public static void addEntityClass(Class<?> entity) {
		daoClasses.add(entity);
	}

	/**
	 * begin the transaction
	 */
	protected static void begin() {
		getSession().getTransaction().begin();
		// getEntityManager().getTransaction().begin();
	}

	/**
	 * commit the transaction
	 */
	protected static void commit() {
		getSession().getTransaction().commit();
		// getEntityManager().getTransaction().commit();
	}

	/**
	 * Does a rollback to the last commit, rather then to the state before the
	 * transaction is started.
	 */
	protected static void rollback() {
		try {
			// getEntityManager().getTransaction().rollback();
			getSession().getTransaction().rollback();
		} catch (HibernateException e) {
			Monitor.log(DAO.class, Level.WARN,
					"The Rollback could not be executed! %s", e);
		}

		try {
			// getEntityManager().close();
			getSession().close();
		} catch (HibernateException e) {
			Monitor.log(DAO.class, Level.WARN,
					"The session could not be closed! %s", e);
		}
		// DAO.entityManager.set(null);
		DAO.session.set(null);
	}

	/**
	 * Close the session
	 */
	public static void close() {
		// getEntityManager().close();
		// DAO.entityManager.set(null);
		Session s = getSession();
		s.close();
		Monitor.log(DAO.class, Level.DEBUG, "Closing hibernate session.");
		DAO.session.remove();
	}

	/**
	 * Persist the given POJO directly to the DB. Should be make, if it exists
	 * relationships between tables.
	 *
	 * @param o
	 *            A Object, which is a POJO and can persist.
	 */
	public static void persistImmediately(Object o) {
		begin();
		// getEntityManager().persist(o);
		getSession().persist(o);
		commit();
		close();
	}

	/**
	 * Add the given POJO to a queue, which can later be flushed with the method
	 * commitQueue. This is faster than using persistImmediately.
	 *
	 * @param o
	 *            A POJO that can be persisted.
	 */
	protected static void addToPersistQueue(Object o) {
		persistQueue.add(o);
		if (persistQueue.size() >= QUEUE_SIZE) {
			commitQueue();
		}
	}

	/**
	 * Commit the queue in a background thread so the simulation does not have
	 * to wait for the database.
	 *
	 * To avoid any synchronization problems the entire queue is handed over to
	 * the committing thread and a new one created in its place.
	 */
	public static void commitQueue() {
		if (persistQueue.isEmpty())
			return;

		commitCount++;
		objectCount += persistQueue.size();

		if (threads.size() >= maxConnections) {
			Monitor.log(DAO.class, Level.INFO,
					"Waiting for commitThreads to finish, to not exceeed the maximum of "
							+ maxConnections + "Connections to the Database.");
			while (!threads.isEmpty()) {
				Thread thread = threads.peek();
				try {
					Monitor.log(DAO.class, Level.INFO, "Waiting for thread "
							+ thread.getName() + " to finish");
					thread.join();
					Monitor.log(DAO.class, Level.INFO,
							"Thread " + thread.getName() + " finished");
				} catch (InterruptedException e) {
					Monitor.log(DAO.class, Level.WARN,
							"got interrupted while waiting for commit threads");
					continue;
				}
				threads.poll();
			}
		}

		Thread thread = new Thread(new CommitThread(persistQueue));
		thread.setName("CommitThread #" + commitCount);
		thread.start();
		threads.add(thread);
		persistQueue = new ArrayList<Object>(QUEUE_SIZE);
	}

	/**
	 * Wait for all queue committing threads to finish.
	 */
	public static void finishCommits() {
		while (!threads.isEmpty()) {
			Thread thread = threads.peek();
			try {
				Monitor.log(DAO.class, Level.INFO, "Waiting for thread "
						+ thread.getName() + " to finish");
				thread.join();
				Monitor.log(DAO.class, Level.INFO, "Thread " + thread.getName()
						+ " finished");
			} catch (InterruptedException e) {
				Monitor.log(DAO.class, Level.WARN,
						"got interrupted while waiting for commit threads");
				continue;
			}
			threads.poll();
		}
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		StandardServiceRegistryBuilder.destroy(serviceRegistry);
		Monitor.log(DAO.class, Level.INFO,
				"commit threads finished. \n stored " + objectCount
						+ " objects in " + commitCount
						+ " threaded transactions taking " + commitTime + " ms");
	}

	/**
	 * Updated a given POJO, which is already persist. It must the same Object,
	 * which is already used to persist, otherwise the object will be only added
	 * as a new entry.
	 *
	 * @param o
	 *            The same object, which is already used to persist.
	 * 
	 * @return The object attached to the persistence context
	 */
	public static <T> T update(T o) {
		commitQueue();
		begin();
		@SuppressWarnings("unchecked")
		T objRef = (T) getSession().merge(o);
		commit();
		close();

		return objRef;
	}

	public static synchronized void addToCommitTime(long time) {
		commitTime += time;
	}
}
