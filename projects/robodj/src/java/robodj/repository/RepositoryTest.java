//
// $Id: RepositoryTest.java,v 1.2 2001/09/20 20:42:48 mdb Exp $

package robodj.repository;

import java.util.Properties;
import com.samskivert.jdbc.StaticConnectionProvider;

public class RepositoryTest
{
    public static void main (String[] args)
    {
	try {
	    Properties props = new Properties();
	    props.put(Repository.REPOSITORY_DB_IDENT + ".driver",
                      "org.gjt.mm.mysql.Driver");
	    props.put(Repository.REPOSITORY_DB_IDENT + ".url",
                      "jdbc:mysql://localhost:3306/robodj");
	    props.put(Repository.REPOSITORY_DB_IDENT + ".username",
                      "www");
	    props.put(Repository.REPOSITORY_DB_IDENT + ".password",
                      "Il0ve2PL@Y");

            StaticConnectionProvider scp =
                new StaticConnectionProvider(props);
	    Repository rep = new Repository(scp);

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

	    scp.shutdown();

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }
}
