//
// $Id: RepositoryTest.java,v 1.1 2000/11/08 06:42:57 mdb Exp $

package robodj.repository;

import java.util.Properties;

public class RepositoryTest
{
    public static void main (String[] args)
    {
	try {
	    Properties props = new Properties();
	    props.put("driver", "org.gjt.mm.mysql.Driver");
	    props.put("url", "jdbc:mysql://localhost:3306/robodj");
	    props.put("username", "www");
	    props.put("password", "Il0ve2PL@Y");

	    Repository rep = new Repository(props);

	    Entry ient = new Entry();
	    ient.title = "Test entry";
	    ient.artist = "Test artist";
	    ient.source = "Test source";
	    ient.songs = new Song[1];
	    ient.songs[0] = new Song();
	    ient.songs[0].position = 1;
	    ient.songs[0].title = "Test song one";
	    ient.songs[0].location = "test_song_one.mp3";
	    ient.songs[0].duration = 99;
	    rep.insertEntry(ient);
	    System.out.println("--> " + ient);

	    Entry ent = rep.getEntry(ient.entryid);
	    System.out.println("<-- " + ent);

	    rep.shutdown();

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }
}
