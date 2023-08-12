/*
 * libmergea4toa3 - A library for merging A4 scanned fragments to obtain
 * a single A3 image. Copyright (C) 2023  David R. Araújo Piñeiro (Davovoid)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package davovoid.libmergea4toa3;

/**
 * A3 Merger study results container.
 * 
 * @author David
 *
 */
public class MergeStudyResults {
	
	private int xposSmallestDev;
	private int yposSmallestDev;
	private double angleSmallestDev;
	
	/**
	 * Creates the study result.
	 * @param xposSmallestDev x value result of the study.
	 * @param yposSmallestDev y value result of the study.
	 * @param angleSmallestDev angle value result of the study (rad).
	 */
	public MergeStudyResults(int xposSmallestDev, int yposSmallestDev, double angleSmallestDev) {
		this.xposSmallestDev = xposSmallestDev;
		this.yposSmallestDev = yposSmallestDev;
		this.angleSmallestDev = angleSmallestDev;
	}

	/**
	 * Gets the x position from the study.
	 * @return The better x position for merging the merged image onto the original.
	 */
	public int getXposSmallestDev() {
		return xposSmallestDev;
	}

	public void setXposSmallestDev(int xposSmallestDev) {
		this.xposSmallestDev = xposSmallestDev;
	}
	
	/**
	 * Gets the y position from the study.
	 * @return The better y position for merging the merged image onto the original.
	 */
	public int getYposSmallestDev() {
		return yposSmallestDev;
	}

	public void setYposSmallestDev(int yposSmallestDev) {
		this.yposSmallestDev = yposSmallestDev;
	}

	/**
	 * Gets the angle (rad) from the study. Positive means moving onto positive x axis
	 * over positive y axis.
	 * @return The better angle in rad for merging the merged image onto the original.
	 */
	public double getAngleSmallestDev() {
		return angleSmallestDev;
	}

	public void setAngleSmallestDev(double angleSmallestDev) {
		this.angleSmallestDev = angleSmallestDev;
	}
	
	
}
