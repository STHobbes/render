/*
 * LightInfo.java
 *
 * Created on October 3, 2002, 10:50 AM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * The GNU General Public License is available at:
 *      http://www.opensource.org/licenses/gpl-license.php
 */
package cip.render.raytrace;

import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import cip.render.utilColour.RGBf;

/**
 * This is the description of the light illuminating a ray intersection with a surface.  Because illumination descriptions
 * are frequently used temporary objects, this object can be used as both a typical object instantiated using the
 * <tt>new</tt> and reclaimed by the garbage collector when the are no longer references to the object, and/or a
 * cached object which is borrowed from a dynamically growing cache of <tt>LightInfo</tt> objects.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class LightInfo {
    //    // global ObjCache object with full cache tracking
//    private static final ObjCache s_objCache = PackageConstants.NO_CACHE ?
//            null : (PackageConstants.LOCAL_CACHE ? null : (new ObjCache("LightInfo          ")));
//    // local cache - no tracking and/or error checking associated with the cache
//    private static final byte[] s_cacheLock = PackageConstants.NO_CACHE ?
//            null : (PackageConstants.LOCAL_CACHE ? (new byte[0]) : null);
//    private static LightInfo s_lclObjCache = null;
    public LightInfo m_next = null;

    /**
     *
     */
    public static final int AMBIENT = 0;
    /**
     *
     */
    public static final int DIRECTIONAL = 1;
    /**
     *
     */
    public static final int LOCAL = 2;

    public int m_nType;                         // light type (from the types described above)
    public RGBf m_rgb = new RGBf();             // the light colour (all types)
    public float m_fDist;                       // the distance to the light (LOCAL only)
    public Vector3f m_vDir = new Vector3f();    // the direction of the light - from the light source (DIRECTIONAL and LOCAL)
    public Point3f m_ptFrom = new Point3f();    // the location of the light - (LOCAL only)

    // this is temporary for debugging .....
    public float m_fPerpDist;

    public LightInfo() {
    }
//
//    // Borrow the object from the object cache (create one if there are none in the cache
//    public static LightInfo borrowObj() {
//        LightInfo lightInfo = null;
//        if (PackageConstants.LOCAL_CACHE) {
//            synchronized (s_cacheLock) {
//                if (null != (lightInfo = s_lclObjCache)) {
//                    s_lclObjCache = lightInfo.m_next;
//                    lightInfo.m_next = null;
//                }
//            }
//        } else {
//            lightInfo = (LightInfo) s_objCache.borrowObj(true);
//        }
//        if (null == lightInfo) {
//            lightInfo = new LightInfo();
//        }
//        return lightInfo;
//    }
//
//    // return an object to the cache
//    public void returnObj() {
//        if (PackageConstants.LOCAL_CACHE) {
//            synchronized (s_cacheLock) {
//                m_next = s_lclObjCache;
//                s_lclObjCache = this;
//            }
//        } else {
//            s_objCache.returnObj(this);
//        }
//    }
}
