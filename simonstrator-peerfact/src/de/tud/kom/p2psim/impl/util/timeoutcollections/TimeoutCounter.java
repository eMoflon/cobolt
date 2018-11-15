/*
 * Copyright (c) 2005-2013 KOM - Multimedia Communications Lab
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
 */

package de.tud.kom.p2psim.impl.util.timeoutcollections;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Holds a counter that is increased for a given
 * time period. After the specified timeout the
 * counter will be reduced by the amount it has
 * been increased.
 *
 * @author Fabio Zöllner
 * @version 1.0, 19.01.13
 */
public class TimeoutCounter {
    private long defaultTimeout;
    private int count = 0;
    private List<Long> timeouts = Lists.newArrayList();

    public TimeoutCounter(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void increase() {
        count++;
        timeouts.add(Time.getCurrentTime() + defaultTimeout);
    }

    public void increase(long timeout) {
        count++;
        timeouts.add(Time.getCurrentTime() + timeout);
    }

    public int get() {
        cleanup();
        return count;
    }

    private void cleanup() {
        Collections.sort(timeouts);
        long currentTime = Time.getCurrentTime();
        for (Iterator<Long> iterator = timeouts.iterator(); iterator.hasNext(); ) {
            Long next = iterator.next();
            if (next <= currentTime) {
                iterator.remove();
                count--;
            }
        }
    }
}
