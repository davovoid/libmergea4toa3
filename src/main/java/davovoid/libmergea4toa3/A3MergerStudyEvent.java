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
 * The A3 Merger Study Event interface used by the A3 Merger class to inform
 * other programs about the current calculation state while it is being
 * calculated.
 * 
 * @author David
 *
 */
public interface A3MergerStudyEvent {

	/**
	 * The current A3 Merger study state. Contains all information the consumer
	 * application might want to check.
	 * 
	 * @param progress          The current overall progress (0 to 1).
	 * @param firstScaleRed     The first scale reduction used (by how many times
	 *                          the width/height has been reduced).
	 * @param scaleRed          The current state scale reduction.
	 * @param xMinFindRange     Minimum x position where this current stage is being
	 *                          checked.
	 * @param xMaxFindRange     Maximum x position where this current stage is being
	 *                          checked.
	 * @param yMinFindRange     Minimum y position where this current stage is being
	 *                          checked.
	 * @param yMaxFindRange     Maximum y position where this current stage is being
	 *                          checked.
	 * @param currentxpos       Currently checked x position.
	 * @param currentypos       Currently checked y position.
	 * @param currentangle      Currently checked angle conversion (rad) over the
	 *                          image. If positive, this angle rotates positive x
	 *                          axis over positive y axis.
	 * @param bestxpos          Best found x position in the current checked scale
	 *                          reduction.
	 * @param bestypos          Best found y position in the current checked scale
	 *                          reduction.
	 * @param bestangle         Best angle (rad) value in the current checked scale
	 *                          reduction.
	 * @param smallestdeviation Smallest red + green + blue value (each one in the 0
	 *                          to 255 scale) deviation between original and merged
	 *                          images, per square pixel checked. Less is better.
	 *                          Zero means no deviation. Less than 100 is a good
	 *                          value, and less than 60 is a very good value.
	 */
	public void updateStudyProgress(double progress, int firstScaleRed, int scaleRed,
			int xMinFindRange, int xMaxFindRange,
			int yMinFindRange, int yMaxFindRange,
			int currentxpos, int currentypos, double currentangle,
			int bestxpos, int bestypos, double bestangle,
			double smallestdeviation
			);
	
}
