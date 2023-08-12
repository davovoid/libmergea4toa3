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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import junit.framework.TestCase;

/**
 * Tests the A3 Merger.
 * @author David
 *
 */
public class TestA3Merger extends TestCase {

	private int posx1, posx2; // Used by fragment generator
	
	/**
	 * Tests the A3 Merger simulating a page scan, dividing it by
	 * three, and then using A3 Merger to merge all of them by itself.
	 * If resulting dividing positions and resulting size matches,
	 * the test is successful.
	 * @throws Exception
	 */
	public void testA3Merger() throws Exception {
		
		// Generate testing image
		
		BufferedImage fullImg = generateImageToTest();

		// Divide it in three fragments (simulates scanning)
		
		BufferedImage[] fragments = generateTestingFragments(fullImg);

		// Test the merging
		
		A3Merger merger = new A3Merger(fragments[0]);

		// Merge center
		
		System.out.println("Merging center...");
		
		merger.setScannerLeftCorrection(true);
		MergeStudyResults results = merger.studyMergingImageOnRight(fragments[1]);

		System.out.format("Resulting merging posx1=%d px. Original posx1=%d\r\n\r\n",
				results.getXposSmallestDev(), posx1);

		assertTrue(posx1==results.getXposSmallestDev());
		
		merger.setWorkingImg(merger.mergeImageOnRight(fragments[1], results));
		
		// Merge right
		
		System.out.println("Merging right...");

		merger.setScannerLeftCorrection(true);
		results = merger.studyMergingImageOnRight(fragments[2]);

		System.out.format("Resulting merging posx2=%d px. Original posx2=%d\r\n\r\n",
				results.getXposSmallestDev(), posx2);

		assertTrue(posx2==results.getXposSmallestDev());
		
		merger.setWorkingImg(merger.mergeImageOnRight(fragments[2], results));

		// Check resulting size
		
		System.out.format("Resulting size= %dx%d px. Original= %dx%d px.\r\n\r\n",
				merger.getWorkingImg().getWidth(), merger.getWorkingImg().getHeight(),
				fullImg.getWidth(), fullImg.getHeight());
		
		assertTrue(merger.getWorkingImg().getWidth() == fullImg.getWidth());
		assertTrue(merger.getWorkingImg().getHeight() == fullImg.getHeight());
		
		// Success
		
		System.out.println("Successfully tested. Congrats!");
		
	}

	/**
	 * Divides the image in three parts, left, center and right.
	 * It considers the full image received to be DIN-based (i.e. A3, A4, etc.),
	 * and divides it as like using a immediate lower-sized scanner
	 * (that is, A3 scanned using A4 scanner, A4 scanned using A5 scanner, etc.).
	 * This function writes the position to the current class as well for testing
	 * purposes.
	 * @param fullImg The full sized image.
	 * @return
	 */
	private BufferedImage[] generateTestingFragments(BufferedImage fullImg) {

		// Fragments width and height
		
		int fragmentwidth = (int) (fullImg.getHeight()/Math.sqrt(2));
		int fragmentheight = fullImg.getHeight();
		
		// Calculate positions to divide the image
		
		posx1 = (int) ( (fullImg.getWidth() - fragmentwidth) /2 );
		posx2 = (int) (fullImg.getWidth() - fragmentwidth);
		
		System.out.format("Original positions: 0, %d, %d px.\r\n", posx1, posx2);
		
		int[] positions = new int[] {0, posx1, posx2};
		
		// Render the fragments and save them
		BufferedImage[] fragments = new BufferedImage[3];
		
		for(int i=0; i<=2; i++) {
			
			fragments[i] = new BufferedImage(fragmentwidth, fragmentheight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = fragments[i].createGraphics();
			
			g2d.drawImage(fullImg, -positions[i], 0, null); // Negative to move to the opposite direction
			
		}
		
		return fragments;
		
	}
	
	/**
	 * Generates a sample testing image for the testing purposes.
	 * @return The testing image resulted.
	 */
	private static BufferedImage generateImageToTest() {
		
		BufferedImage img = new BufferedImage(2480,1754,BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2d = img.createGraphics();
		
		// Antialiasing to make it more difficult
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Background
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
		
		// Foreground, crossing lines at a non-integer stroke thickness
		
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2.5f));
		
		for(int i=0; i<img.getWidth(); i+=10) {
			
			g2d.drawLine(i, 0, i*2, img.getHeight());

			g2d.drawLine(img.getWidth()-i, 0, img.getWidth()-i*2, img.getHeight());
			
		}
		
		return img;
	}
	
}
