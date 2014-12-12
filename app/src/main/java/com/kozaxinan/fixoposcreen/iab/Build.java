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
package com.kozaxinan.fixoposcreen.iab;

import com.kozaxinan.fixoposcreen.BuildConfig;

/**
 * Created by Artem on 30.12.13.
 */
public final class Build {

    public static final boolean DEBUG = BuildConfig.MY_DEBUG;
    public static final String TIME_STAMP = BuildConfig.MY_TIME_STAMP;

	public static final String GOOGLE_PLAY_PUBLIC_KEY = BuildConfig.MY_GOOGLE_PLAY_PUBLIC_KEY;

    /**
     * It contains most of links of AcDisplay.
     */
    public static class Links {

        public static final String DONATE = "http://goo.gl/";
    }

}
