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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * The A3 merger management class. This class allows to merge scans from a
 * bigger image. Scans shall be aligned the best possible, and the scans shall
 * be from left to right, starting on one side, and finishing on the other side.
 * <br />
 * For instance, A3 can be reproduced by three A4 scans, one from the left side,
 * other from the center, and the last from the right side. <br />
 * An example on how to work with A3 merger:
 * <ol>
 * <li>Use the constructor to add the first (left side) image. <br />
 * {@code A3Merger a3Merger = new A3Merger(leftImage);}
 * <li>Merge the following scan from the right using:<br />
 * {@code a3Merger.mergeImageOnRight(nextImageOnRight, true);}<br />
 * The {@code true} argument allows the resulting image to be used as the
 * working image in A3Merger, so you can add subsequent images more on the right
 * by calling that function again.
 * <li>{@code a3Merger.getWorkingImg()} shall return the resulting image with
 * all merges.
 * </ol>
 * 
 * A3 Merger algorithm allows small angle corrections and considers y-axis
 * offsets.
 * 
 * @author David
 *
 */
public class A3Merger {

	private BufferedImage workingImg = null;
	private boolean scannerLeftCorrection = false;
	private A3MergerStudyEvent mergerStudyEvent = null;
	
	private static final double maxignoresection=100; // px width removing the merged image
	private static final double maxdegsection=50; // px width going from removing to completely adding the merged img
	
	
	/**
	 * Constructs the A3 merger, using the first (left) image from the merging
	 * image set.
	 * @param workingImg The image to which the other images will be merged.
	 */
	public A3Merger(BufferedImage workingImg) {
		setWorkingImg(workingImg);
	}

	/**
	 * Gets the current A3 merger working (or in some cases resulting) image.
	 * @return
	 */
	public BufferedImage getWorkingImg() {
		return workingImg;
	}

	/**
	 * Sets the working image to a new desired one.
	 * @param workingImg The new image to be used as a working image.
	 */
	public void setWorkingImg(BufferedImage workingImg) {
		this.workingImg = workingImg;
	}
	
	/**
	 * Gets the current status of scanner left correction.
	 * @return
	 */
	public boolean isScannerLeftCorrection() {
		return scannerLeftCorrection;
	}

	/**
	 * Sets the scanner left correction. Useful in cases where the merged image was
	 * scanned in such a way its merged border is not in fully quality. This mode
	 * would discard part of that border (using the working image) and then it will
	 * transition gradually from the working image to the merged image, making the
	 * transition as seamless as possible.
	 * 
	 * @param scannerLeftCorrection the parameter.
	 */
	public void setScannerLeftCorrection(boolean scannerLeftCorrection) {
		this.scannerLeftCorrection = scannerLeftCorrection;
	}

	public A3MergerStudyEvent getMergerStudyEvent() {
		return mergerStudyEvent;
	}

	/**
	 * Useful in GUI environments for showing progress. The A3 Merger functions
	 * would call the interfaced method from the {@code A3MergerStudyEvent}:
	 * <ul>
	 * <li>During the processing, each second.
	 * <li>After finishing each scaling stage.
	 * <li>After finishing.
	 * </ul>
	 * 
	 * @param mergerStudyEvent The merger study event to register.
	 */
	public void setMergerStudyEvent(A3MergerStudyEvent mergerStudyEvent) {
		this.mergerStudyEvent = mergerStudyEvent;
	}

	/**
	 * Merges an image on the right of the working image.
	 * 
	 * @param imageToMerge          The image to merge on the right. Y-axis offset
	 *                              and low angle correction is automatically
	 *                              performed.
	 * @param useResultAsWorkingImg If set to true, the resulting image will become
	 *                              the working image after processing.
	 * @return The resulting image.
	 */
	public BufferedImage mergeImageOnRight(BufferedImage imageToMerge, boolean useResultAsWorkingImg) {

		// Merge study, getting the x, y, and angle positioning
		
		MergeStudyResults results = studyMergingImageOnRight(imageToMerge);
		
		// Merge result
		
		BufferedImage resultImg = mergeImageOnRight(imageToMerge, results);

		// Set as working image
		
		if(useResultAsWorkingImg) setWorkingImg(resultImg);
		
		return resultImg;
		
	}

	/**
	 * Generates the image merging study used by A3 Merger. That is, it implements
	 * the algorithm and returns the calculation results, such as x, y, and angle
	 * for merging the new added image.
	 * 
	 * @param imageToMerge The image to be studied as a merge.
	 * @return The merge study results.
	 */
	public MergeStudyResults studyMergingImageOnRight(BufferedImage imageToMerge) {
		
		long currentTime = System.currentTimeMillis();
		
		// Parameters updated for each merging cycle
		
		int firstScaleRed = (int) (Math.pow(2,Math.round(Math.log(workingImg.getHeight()/300)/Math.log(2))));
		int scaleRed=firstScaleRed;
		
		int xMinFindRange = 0;
		int xMaxFindRange = workingImg.getWidth()/scaleRed;
		int yMinFindRange = Math.min(-workingImg.getHeight()/20/scaleRed, -100);
		int yMaxFindRange = Math.max(workingImg.getHeight()/20/scaleRed, 100);

		// Smallest dev positioning
		
		int xposSmallestDev = 0;
		int yposSmallestDev = 0;
		double angleSmallestDev = 0;

		double progress=0;
		
		do {
			
			System.out.format("Current scale reduction: %d\n", scaleRed);
			
			// Resize images

			BufferedImage workingImgScl = new BufferedImage(workingImg.getWidth()/scaleRed, workingImg.getHeight()/scaleRed, BufferedImage.TYPE_INT_ARGB);
			workingImgScl.createGraphics().drawImage(workingImg, 0, 0, workingImg.getWidth()/scaleRed, workingImg.getHeight()/scaleRed,null);

			BufferedImage imageToMergeScl = new BufferedImage(imageToMerge.getWidth()/scaleRed, imageToMerge.getHeight()/scaleRed, BufferedImage.TYPE_INT_ARGB);
			imageToMergeScl.createGraphics().drawImage(imageToMerge, 0, 0, imageToMerge.getWidth()/scaleRed, imageToMerge.getHeight()/scaleRed,null);

			System.out.println("Images scaled. Processing...");
			
			// Smallest deviation initialize
			
			double smallestDeviation = Double.MAX_VALUE;

			int maxxposinc = Math.max(workingImgScl.getWidth()/10, 200);
			int maxyposinc = Math.max(workingImgScl.getHeight(), 200);
			
			for (int xpos = xMinFindRange; xpos<xMaxFindRange; xpos++) {
				
				for (int ypos = yMinFindRange; ypos<yMaxFindRange; ypos++) {
					
					for(int xdesv = -6; xdesv <=6; xdesv+=3) {

						// Only translation when scaling 1
						
						if(scaleRed>2 && xdesv!=0) continue;
						
						double angle = Math.atan2(xdesv, imageToMergeScl.getHeight());

						if(System.currentTimeMillis()-currentTime>1000) {

							// Statistics every second
							
							double scalesteps=Math.log(firstScaleRed)/Math.log(2d)+1;
							double scaleprogress = (Math.log(firstScaleRed)/Math.log(2d)-Math.log(scaleRed)/Math.log(2d))/scalesteps;
							
							double xpossteps = (double)(xMaxFindRange-xMinFindRange);
							double xposprogress = (double)(xpos - xMinFindRange) / xpossteps;
							
							double ypossteps = (double)(yMaxFindRange-yMinFindRange);
							double yposprogress = (double)(ypos - yMinFindRange) / ypossteps;
							
							progress = scaleprogress + xposprogress / scalesteps + yposprogress / xpossteps / scalesteps;
							
							System.out.format("CURRENT PROGRESS: %.1f%%\n\n", progress*100d);
							
							System.out.format("Current position: x=%d, y=%d, angle=%.5f rad.\n",
									xpos, ypos, angle);
							
							System.out.format("Current processing result: x=%d, y=%d, angle=%.5f rad, dev=%.8f.\n\n",
									xposSmallestDev, yposSmallestDev, angleSmallestDev, smallestDeviation);
							
							// Merger study event for GUI info retrieval
							if(getMergerStudyEvent()!=null) {
								
								getMergerStudyEvent().updateStudyProgress(
										progress, firstScaleRed, scaleRed,
										xMinFindRange, xMaxFindRange,
										yMinFindRange, yMaxFindRange,
										xpos, ypos, angle,
										xposSmallestDev, yposSmallestDev, angleSmallestDev,
										smallestDeviation);
								
							}
							
							currentTime = System.currentTimeMillis();
						}
						
						// xpos and ypos move the imageToMerge to find merging conditions
						
						double controlledArea = 0;
						
						double currentDeviation = 0d;

						for(int xcheck = xpos; xcheck < xpos+maxxposinc; xcheck++) {
		
							for(int ycheck = ypos; ycheck < ypos+maxyposinc; ycheck++) {
	
								// Check the controlled area
								
								int workingX = xcheck;
								int workingY = ycheck;
								
								if(workingY<0) continue;
								
								if(workingX>=workingImgScl.getWidth()) continue;
								if(workingY>=workingImgScl.getHeight()) continue;
								
								if(xcheck<maxignoresection) continue;
								
								// Merging position no translation applied
								int mergingXnoTransl = xcheck - xpos;
								int mergingYnoTransl = ycheck - ypos;
								
								// Calculate translation
								
								int mergingX, mergingY;
								
								if(xdesv!=0) {

									mergingX = (int) (mergingXnoTransl * Math.cos(angle)
											+ mergingYnoTransl * Math.sin(angle));
									mergingY = (int) (-mergingXnoTransl * Math.sin(angle)
											+ mergingYnoTransl * Math.cos(angle));
									
								} else {
									
									mergingX = mergingXnoTransl;
									mergingY = mergingYnoTransl;
									
								}
								
								// Check width and height limits
								if(mergingX<0) continue;
								if(mergingY<0) continue;
								
								if(mergingX>=imageToMergeScl.getWidth()) continue;
								if(mergingY>=imageToMergeScl.getHeight()) continue;
								
								int rgbWorking = workingImgScl.getRGB(workingX, workingY);
								int rgbMerging = imageToMergeScl.getRGB(mergingX, mergingY);
								
								// Compare absolute of respectively red, green and blue:
								currentDeviation += Math.abs(((rgbWorking >> 16) & 255) - ((rgbMerging >> 16) & 255));
								currentDeviation += Math.abs(((rgbWorking >> 8) & 255) - ((rgbMerging >> 8) & 255));
								currentDeviation += Math.abs((rgbWorking & 255) - (rgbMerging & 255));
								
								controlledArea++;
								
							}
						}
						
						if(controlledArea<0.5d) continue;
						
						currentDeviation /= controlledArea; // index per area
						
						// Check if it is the best result
						
						if(currentDeviation<smallestDeviation) {
							
							// Save result
							
							smallestDeviation = currentDeviation;
							
							xposSmallestDev = xpos;
							yposSmallestDev = ypos;
							angleSmallestDev = angle;

						}
						
					}
				}
			}
				
			// Show result
			
			System.out.format("Resulting position: x=%d, y=%d, angle=%.5f rad, dev=%.8f, scale=%d.\n",
					xposSmallestDev, yposSmallestDev, angleSmallestDev, smallestDeviation, scaleRed);

			// Merger study event for GUI info retrieval
			if(getMergerStudyEvent()!=null) {
				
				getMergerStudyEvent().updateStudyProgress(
						progress, firstScaleRed, scaleRed,
						xMinFindRange, xMaxFindRange,
						yMinFindRange, yMaxFindRange,
						xposSmallestDev, yposSmallestDev, angleSmallestDev,
						xposSmallestDev, yposSmallestDev, angleSmallestDev,
						smallestDeviation);
				
			}
			
			// New limits and rescale
			
			xMinFindRange = xposSmallestDev*2-4;
			xMaxFindRange = xposSmallestDev*2+4;
			yMinFindRange = yposSmallestDev*2-4;
			yMaxFindRange = yposSmallestDev*2+4;
			
			if(scaleRed>1) {

				xposSmallestDev *=2;
				yposSmallestDev *=2;

			}
			
			scaleRed/=2;
			
			/*if(scaleRed>0) {

				yMinFindRange = Math.min(-workingImg.getHeight()/20/scaleRed, -100);
				yMaxFindRange = Math.max(workingImg.getHeight()/20/scaleRed, 100);

			}*/
			
		} while(scaleRed>=1);
	
		MergeStudyResults results = new MergeStudyResults(xposSmallestDev, yposSmallestDev, angleSmallestDev);
		return results;
	}

	/**
	 * Merges an image to the working image, using the merge study
	 * results from a {@code MergeStudyResults} container.
	 * This function does not validate the correctness of the merging params,
	 * for that please consider {@code mergeImageOnRight(BufferedImage imageToMerge, boolean useResultAsWorkingImg)}.
	 * @param imageToMerge The image to be merged.
	 * @param mergeStudy The merging parameters container.
	 * @return The merging result.
	 */
	public BufferedImage mergeImageOnRight(BufferedImage imageToMerge, MergeStudyResults mergeStudy) {
		
		int xposSmallestDev = mergeStudy.getXposSmallestDev();
		int yposSmallestDev = mergeStudy.getYposSmallestDev();
		double angleSmallestDev = mergeStudy.getAngleSmallestDev();
		
		// Resulting image container
		BufferedImage resultImg = new BufferedImage(
				xposSmallestDev+imageToMerge.getWidth(),
				yposSmallestDev+imageToMerge.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		// The graphics2D object to manipulate it
		Graphics2D resultImgG2D = resultImg.createGraphics();
		
		// White, then original image
		resultImgG2D.setColor(Color.WHITE);
		resultImgG2D.fillRect(0, 0, resultImg.getWidth(), resultImg.getHeight());
		resultImgG2D.drawImage(workingImg, 0, 0, workingImg.getWidth(), workingImg.getHeight(), null);
		
		// and then the merged image corrected as parameters
		AffineTransform tx = AffineTransform.getRotateInstance(angleSmallestDev, 0, 0); // Clock-wise is positive
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		
		BufferedImage rotatedMergedImg = op.filter(imageToMerge, null);

		resultImgG2D.drawImage(rotatedMergedImg, xposSmallestDev, yposSmallestDev,
				rotatedMergedImg.getWidth(), rotatedMergedImg.getHeight(), null);
		
		// Removing left part of merged image, if allowed by parameter
		
		if(isScannerLeftCorrection()) {
			
			for(int x=0;x<maxignoresection+maxdegsection; x++) {
				
				for(int y=0; y<resultImg.getHeight();y++) {

					// Check width and height limits
					if(y+yposSmallestDev>=workingImg.getHeight()) continue;
					if(y+yposSmallestDev>=resultImg.getHeight()) continue;

					if(x+xposSmallestDev>=workingImg.getWidth()) continue;
					if(x+xposSmallestDev>=resultImg.getWidth()) continue;
					
					if(y+yposSmallestDev<0) continue;
					if(x+xposSmallestDev<0) continue;
					
					// Alpha factor calculated
					double factor = x<maxignoresection? 0d : (double)(x-maxignoresection)/maxdegsection;
					
					// Calculate colors
					
					int rgbWorking=workingImg.getRGB(x+xposSmallestDev, y+yposSmallestDev);
					int rgbMerge=resultImg.getRGB(x+xposSmallestDev, y+yposSmallestDev);
					
					Color colorWorking = new Color(rgbWorking);
					Color colorMerge = new Color(rgbMerge);
					
					/*
					if(y<3) { // Debugging purposes
						
						colorWorking = Color.red;
						colorMerge = Color.green;
					
					}
					*/
					// Set resulting color onto resulting img
					resultImg.setRGB(x+xposSmallestDev, y+yposSmallestDev,
							new Color(
									(int)(colorWorking.getRed()*(1d-factor)+colorMerge.getRed()*factor),
									(int)(colorWorking.getGreen()*(1d-factor)+colorMerge.getGreen()*factor),
									(int)(colorWorking.getBlue()*(1d-factor)+colorMerge.getBlue()*factor),
									255).getRGB()
							);
					
				}
				
			}
			
			
		}
		
		return resultImg;
	}
	
}
