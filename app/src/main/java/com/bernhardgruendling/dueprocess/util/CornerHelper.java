package com.bernhardgruendling.dueprocess.util;

import android.graphics.Point;

public class CornerHelper {

    private static final int DISTANCE_FROM_EDGE = 10;

    public static int getCornerIdx(int width, int height, float x, float y) {
        if (x < width / 4) { //LEFT
            if (y < height / 4) { //TOP
                return 0; //TOP LEFT
            } else if (y > height / 4 * 3) { //BOTTOM
                return 2; //BOTTOM LEFT
            }
        } else if (x > width / 4 * 3) { //RIGHT
            if (y < width / 4) { //TOP
                return 1; //TOP RIGHT
            } else if (y > height / 4 * 3) { //BOTTOM
                return 3; //BOTTOM RIGHT
            }
        }
        return -1;
    }

    public static Point getPointFromCorner(int width, int height, int cornerIdx) {
        switch (cornerIdx) {
            case 0:
                return new Point(0+DISTANCE_FROM_EDGE, 0+DISTANCE_FROM_EDGE);
            case 1:
                return new Point(width-DISTANCE_FROM_EDGE, 0+DISTANCE_FROM_EDGE);
            case 2:
                return new Point(0+DISTANCE_FROM_EDGE, height-DISTANCE_FROM_EDGE);
            case 3:
                return new Point(width-DISTANCE_FROM_EDGE, height-DISTANCE_FROM_EDGE);
            default:
                throw new IllegalArgumentException("cornerIdx invalid");
        }

    }
}
