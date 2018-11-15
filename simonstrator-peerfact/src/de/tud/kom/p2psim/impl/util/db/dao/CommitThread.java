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

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import de.tud.kom.p2psim.impl.util.db.metric.CustomMeasurement;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetric;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetricBound;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/** Thread to commit the persistence queue without blocking the simulation. */
public class CommitThread implements Runnable {

	private static int numThreads = 0;
	private static int numSessions = 0;

	private List<Object> persistQueue;

	public CommitThread(List<Object> persistQueue) {
		this.persistQueue = persistQueue;
	}

	@Override
	public void run() {
		Date start = new Date();
		int threads = incNumThreads();
		Monitor.log(CommitThread.class, Level.INFO,
				"started new commit thread (concurrent commit threads: "
						+ threads + ")");

		while (true) {
			if (commitQueue())
				break;

			// connection problems -> retry in 10 seconds
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// don't bother resuming sleep
			}
		}

		long duration = new Date().getTime() - start.getTime();
		Monitor.log(CommitThread.class, Level.INFO, "finished commiting "
				+ persistQueue.size() + " objects in " + duration + " ms");
		decNumThreads();
		DAO.addToCommitTime(duration);

		// allow the garbage collector to free the committed data
		persistQueue = null;
	}
	

	//private static Session makeSession() {
	//	SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
	//	return sessionFactory.openSession();
	//}

	public boolean commitQueue() {
		int sessions = incNumSessions();
		Session s = null;
		boolean ok = true;
		try {
			s = DAO.getSession();
			s.getTransaction().begin();

			for (Object persist : persistQueue) {
                if (persist instanceof HostMetricBound) {
                    HostMetric metric = ((HostMetricBound)persist).getHostMetric();
                    if (metric != null) {
                    	HostMetric foundMetric = (HostMetric) s.get(HostMetric.class, metric.getId());
                        if (foundMetric != null) {
                            ((HostMetricBound) persist).setHostMetric(foundMetric);
                        }
                    }
                }
				// log.info("Persisting " + persist);
				s.persist(persist);
				if (persist instanceof CustomMeasurement) {
					((CustomMeasurement)persist).afterPersist();
				}
			}
			s.getTransaction().commit();
			s.close();
			ok = true;
		} catch (org.hibernate.HibernateException e) {
            throw e;
            // FIXME: In which cases can the below described scenario occur?
            // We can't just swallow the exception here. In case of an real error
            // this would be critical... loosing measurement data and stuff...

            // OLD CATCH BLOCK:
                // at this point we already had a working connection during the setup
                // (saving the ExperimentDAO and so on), so this is likely an intermittent failure

                //log.warn("could not connect to database (concurrent sessions: "+sessions+")");
                //ok = false;
		} finally {
			decNumSessions();
			//if (em != null && em.isOpen()) {
			//	em.close();
			//}
		}
		return ok;
	}

	private static synchronized int incNumThreads() {
		return ++numThreads;
	}

	private static synchronized int decNumThreads() {
		return --numThreads;
	}

	private static synchronized int incNumSessions() {
		return ++numSessions;
	}

	private static synchronized int decNumSessions() {
		return --numSessions;
	}
}
