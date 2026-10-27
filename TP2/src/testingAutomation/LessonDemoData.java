package testingAutomation;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;
import entityClasses.User;
import java.nio.file.Files;
import java.nio.file.Path;

/*******
 * <p>Title: LessonDemoData Class.</p>
 * <p>Description: Explicitly prepares a separate project-local TP2Demo database for manual
 * Contributor checks. Every write uses registered/authenticated accounts and the real guarded
 * Database API. This utility never opens the ordinary Team53 database or the legacy home-folder
 * Foundations database, never drops data, and never claims a failed operation succeeded.</p>
 * <p>Run from TP2 with {@code python3 dev.py demo}; it seeds then opens the same isolated database.
 * Demo-only accounts jsmith, akim, newcontributor and tp2DemoCurator use DemoPass1!.</p>
 * @author Joshua Luther - original lesson examples
 * @author Vishwam - authenticated isolated demo integration
 */
public final class LessonDemoData {
    private static final String PASSWORD="DemoPass1!";
    private static final String CURATOR="tp2DemoCurator";

    private LessonDemoData() {}

    /** Seed only the dedicated demo database; no URL or production-account override is accepted. */
    public static void main(String[] args) throws Exception {
        if(args.length!=0)throw new IllegalArgumentException("Run python3 dev.py demo from TP2; this fixture accepts no production database arguments.");
        Path directory=Path.of("data").toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String url="jdbc:h2:"+directory.resolve("TP2Demo").toString().replace('\\','/');
        Database db=new Database(url);
        db.connectToDatabase();
        try {
            for(String name:new String[]{"jsmith","akim","newcontributor",CURATOR}) {
                if(!db.doesUserExist(name))db.register(new User(name,PASSWORD,"Demo","","User","",
                    name+"@example.test",false,!name.equals(CURATOR),false,name.equals(CURATOR)));
                login(db,name);
            }
            add(db,"jsmith","JavaFX app fails to launch in Eclipse","Tools and Setup",
                "After importing the project, running it gave a module error saying the JavaFX runtime components were missing, even though the JARs were on the build path.",
                "Add the JavaFX module path and --add-modules to the Run Configuration VM arguments. Putting the JARs on the build path alone is not enough.",false,false,false);
            add(db,"jsmith","Merge conflicts right before the deadline","Teamwork",
                "Two teammates edited the same Database class on separate branches and merged an hour before the TP1 deadline, which took most of that hour to fix.",
                "Pull and merge shared files at least once a day, and agree on who owns which methods in shared classes before starting work.",true,false,true);
            add(db,"jsmith","Record the screencast after the tests pass","Testing",
                "We found a failing test while recording a screencast and had to record it again after fixing the code.",
                "Run the full test driver and fix every failure before recording any video.",false,true,false);
            add(db,"akim","JavaFX scene does not update after a change","Coding",
                "A label kept showing old text because the code changed the value on a background thread instead of the JavaFX thread.",
                "Update JavaFX widgets only on the JavaFX Application Thread, using Platform.runLater when the work happens elsewhere.",false,false,false);
            for(String owner:new String[]{"jsmith","akim","newcontributor"}) {
                login(db,owner);
                System.out.println(owner+": "+db.getLessonsByOwner(owner).size()+" lesson(s)");
            }
            System.out.println("Demo ready at "+directory.resolve("TP2Demo")+"; Contributor passwords: "+PASSWORD);
            System.out.println("Start with python3 dev.py demo. This fixture does not modify normal application databases.");
        } finally {db.closeConnection();}
    }

    private static void login(Database db,String name) {
        if(!db.authenticateSession(name,PASSWORD))
            throw new IllegalStateException("The dedicated demo account "+name+" has different credentials. No existing account was overwritten.");
    }

    private static void require(LessonResult result) {
        if(result!=LessonResult.SUCCESS)throw new IllegalStateException("Demo setup failed: "+result+". "+result.getMessage());
    }

    private static void add(Database db,String owner,String title,String category,String situation,
            String text,boolean fieldLocks,boolean fullLock,boolean edited) {
        login(db,owner);
        int id=0;
        for(LessonsLearned existing:db.getLessonsByOwner(owner).getLessons())
            if(existing.getTitle().equals(title)){id=existing.getLessonId();break;}
        if(id==0) {
            LessonsLearned lesson=new LessonsLearned(owner,title,category,situation,text);
            require(db.addLesson(lesson));id=lesson.getLessonId();
            if(id<=0)throw new IllegalStateException("No identity was returned for a seeded lesson.");
            System.out.println("Added demo lesson for "+owner+": "+title);
        } else System.out.println("Reusing demo lesson for "+owner+": "+title);
        login(db,CURATOR);
        require(db.setFieldLock(id,"Title",fieldLocks));
        require(db.setFieldLock(id,"Lesson",fieldLocks));
        require(db.setLessonLock(id,fullLock));
        require(db.setCuratorEdited(id,edited));
    }
}
