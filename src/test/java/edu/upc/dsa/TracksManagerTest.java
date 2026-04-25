package edu.upc.dsa;

import edu.upc.dsa.exceptions.TrackNotFoundException;
import edu.upc.dsa.models.Track;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TracksManagerTest {
    TracksManager tm;

    @Before
    public void setUp() {
        this.tm = TracksManagerImpl.getInstance();
        this.tm.addTrack("T1", "La Barbacoa", "Georgie Dann");
        this.tm.addTrack("T2", "Despacito", "Luis Fonsi");
        this.tm.addTrack("T3", "Ent3r S4ndm4n", "Metallica");
    }

    @After
    public void tearDown() {
        // És un Singleton
        this.tm.clear();
    }

    @Test
    public void addTrackTest() {
        Assert.assertEquals(3, tm.size());

        this.tm.addTrack("La Vereda De La Puerta De Atrás", "Extremoduro");

        Assert.assertEquals(4, tm.size());

    }

    @Test
    public void getTrackTest() throws Exception {
        Assert.assertEquals(3, tm.size());

        Track t = this.tm.getTrack("T2");
        Assert.assertEquals("Despacito", t.getTitle());
        Assert.assertEquals("Luis Fonsi", t.getSinger());

        t = this.tm.getTrack2("T2");
        Assert.assertEquals("Despacito", t.getTitle());
        Assert.assertEquals("Luis Fonsi", t.getSinger());

        Assert.assertThrows(TrackNotFoundException.class, () ->
                this.tm.getTrack2("XXXXXXX"));

    }

    @Test
    public void getTracksTest() {
        Assert.assertEquals(3, tm.size());
        List<Track> tracks  = tm.findAll();

        Track t = tracks.get(0);
        Assert.assertEquals("La Barbacoa", t.getTitle());
        Assert.assertEquals("Georgie Dann", t.getSinger());

        t = tracks.get(1);
        Assert.assertEquals("Despacito", t.getTitle());
        Assert.assertEquals("Luis Fonsi", t.getSinger());

        t = tracks.get(2);
        Assert.assertEquals("Ent3r S4ndm4n", t.getTitle());
        Assert.assertEquals("Metallica", t.getSinger());

        Assert.assertEquals(3, tm.size());

    }

    @Test
    public void updateTrackTest() {
        Assert.assertEquals(3, tm.size());
        Track t = this.tm.getTrack("T3");
        Assert.assertEquals("Ent3r S4ndm4n", t.getTitle());
        Assert.assertEquals("Metallica", t.getSinger());

        t.setTitle("Enter Sandman");
        this.tm.updateTrack(t);

        t = this.tm.getTrack("T3");
        Assert.assertEquals("Enter Sandman", t.getTitle());
        Assert.assertEquals("Metallica", t.getSinger());
    }


    @Test
    public void deleteTrackTest() {
        Assert.assertEquals(3, tm.size());
        this.tm.deleteTrack("T3");
        Assert.assertEquals(2, tm.size());

        Assert.assertThrows(TrackNotFoundException.class, () ->
                this.tm.getTrack2("T3"));

    }
}
