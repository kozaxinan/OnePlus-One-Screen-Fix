/*
 * Copyright (C) 2014 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.kozaxinan.fixoposcreen.iab.utils;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Artem on 21.01.14.
 */
public class ViewUtils {

    private static final String TAG = "ViewUtils";

    public static int getTop( View view) {
        final int[] coordinates = new int[3];
        view.getLocationInWindow(coordinates);
        return coordinates[1];
    }

    // //////////////////////////////////////////
    // //////////// -- VISIBILITY -- ////////////
    // //////////////////////////////////////////

    public static void setVisible( View view, boolean visible) {
        setVisible(view, visible, View.GONE);
    }

    public static void setVisible( View view, boolean visible, int invisibleFlag) {
        int visibility = view.getVisibility();
        int visibilityNew = visible ? View.VISIBLE : invisibleFlag;

        if (visibility != visibilityNew) {
            view.setVisibility(visibilityNew);
        }
    }

    public static void safelySetText( TextView textView, CharSequence text) {
        final boolean visible = text != null;
        if (visible) textView.setText(text);
        ViewUtils.setVisible(textView, visible);
    }
}
