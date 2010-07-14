package com.yellowbkpk.geo.glom;

import static org.junit.Assert.*;

import org.junit.Test;

import osm.OSMFile;
import osm.primitive.way.Way;

public class GlommerTest {

    @Test
    public void testGlom1() {
        Glommer g = new Glommer("foo");
        OSMFile data = new OSMFile();
        
        Way w1 = new Way();
        data.addWay(w1);

        Way w2 = new Way();
        data.addWay(w2);
        
        g.glom(data);
    }

}
