/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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



package de.tud.kom.p2psim.impl.util.stat.distributions;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;


public class PoissonDistribution implements Distribution {
    private double lambda;
    private PoissonDistributionImpl poisson;
    
    @XMLConfigurableConstructor({"lambda"})
    public PoissonDistribution(double lambda){
    	this.lambda = lambda;
    	this.poisson = new PoissonDistributionImpl(lambda);
    }
    
    // returns the x-value for a random value in the cdf
    public double returnValue() {
		double random = Randoms.getRandom(this)
				.nextDouble();
       int result;
       
       try {
           result = poisson.inverseCumulativeProbability(random);
       } catch (MathException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           result = 0;
       }
           
       return result;
    }
    
    /**
     * returns a random value Poisson distributed with lamda = _lamda.
     * @param _lamda
     * @return  as double
     */
    public static double returnValue(double _lamda) {
    	try {
    		PoissonDistributionImpl d = new PoissonDistributionImpl(_lamda);
			return d.inverseCumulativeProbability(Randoms.getRandom(
					PoissonDistribution.class).nextDouble());
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }

	@Override
	public String toString() {
		return "PoissonDistribution [lambda=" + lambda + "]";
	}

}
