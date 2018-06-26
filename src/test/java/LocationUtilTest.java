/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.andavin.util.LocationUtil;
import org.bukkit.block.BlockFace;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since June 26, 2018
 * @author Andavin
 */
public class LocationUtilTest {

    @Test
    public void testDifferenceAndRotating() {

        BlockFace rotating = BlockFace.NORTH;
        while (true) {

            assertEquals(0, LocationUtil.getDifference(rotating, rotating), 0);
            assertEquals(22.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 1)), 0);
            assertEquals(45, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 2)), 0);
            assertEquals(67.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 3)), 0);
            assertEquals(90, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 4)), 0);
            assertEquals(112.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 5)), 0);
            assertEquals(135, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 6)), 0);
            assertEquals(157.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 7)), 0);
            assertEquals(180, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 8)), 0);
            assertEquals(202.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 9)), 0);
            assertEquals(225, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 10)), 0);
            assertEquals(247.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 11)), 0);
            assertEquals(270, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 12)), 0);
            assertEquals(292.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 13)), 0);
            assertEquals(315, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 14)), 0);
            assertEquals(337.5, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 15)), 0);
            assertEquals(0, LocationUtil.getDifference(rotating, LocationUtil.rotateRight(rotating, 16)), 0);

            assertEquals(45, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 14)), 0);
            assertEquals(90, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 12)), 0);
            assertEquals(135, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 10)), 0);
            assertEquals(180, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 8)), 0);
            assertEquals(225, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 6)), 0);
            assertEquals(270, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 4)), 0);
            assertEquals(315, LocationUtil.getDifference(rotating, LocationUtil.rotateLeft(rotating, 2)), 0);

            assertEquals(45, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 45, false, false)), 0);
            assertEquals(90, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 90, false, false)), 0);
            assertEquals(135, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 135, false, false)), 0);
            assertEquals(180, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 180, false, false)), 0);
            assertEquals(225, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 225, false, false)), 0);
            assertEquals(270, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 270, false, false)), 0);
            assertEquals(315, LocationUtil.getDifference(rotating, LocationUtil.rotate(rotating, 315, false, false)), 0);

            rotating = LocationUtil.rotateRight(rotating);
            if (rotating == BlockFace.NORTH) {
                break;
            }
        }
    }
}
