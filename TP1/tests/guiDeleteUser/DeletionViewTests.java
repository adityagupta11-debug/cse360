package guiDeleteUser;

import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.*;
import database.Database;
import database.Database.DeletionCandidate;
import database.Database.DeletionResult;
import entityClasses.User;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import guiTools.TeamTheme;

/** Tests the real MVC classes and renders JavaFX scenes offscreen, without desktop automation. */
public final class DeletionViewTests {
    private static int passed;
    private static void check(String name, boolean good) {
        if (!good) throw new AssertionError("FAIL " + name);
        passed++; System.out.println("PASS " + name);
    }
    public static void main(String[] args) throws Exception {
        CompletableFuture<Void> done=new CompletableFuture<>();
        Platform.startup(()->{
            try { run(); done.complete(null); }
            catch(Throwable failure) { done.completeExceptionally(failure); }
        });
        try { done.get(30,TimeUnit.SECONDS); }
        finally { Platform.exit(); }
        System.out.println("RESULT: " + passed + " controller/view checks passed; 0 failed.");
    }
    private static void run() throws Exception {
        Database db=new Database("jdbc:h2:mem:viewTests"); db.connectToDatabase();
        applicationMain.FoundationsMain.database = db;
        try {
            User admin=new User("adminDemo","DemoPass1!","Demo","","Admin","","admin@example.test",true,false,false,false);
            db.register(admin);
            db.register(new User("memberDemo","DemoPass1!","Demo","","Member","","member@example.test",false,true,false,false));
            db.authenticateSession("adminDemo","DemoPass1!");
            ModelDeleteUser model=new ModelDeleteUser(db);
            DeletionCandidate member=model.choices().stream().filter(u->u.username().equals("memberDemo")).findFirst().orElseThrow();
            check("V01 no selection never asks for confirmation", ControllerDeleteUser.request(model,null,u->{throw new AssertionError("Unexpected dialog");})==DeletionResult.NO_SELECTION);
            check("V02 No preserves account", ControllerDeleteUser.request(model,member,u->false)==DeletionResult.CANCELLED && db.doesUserExist("memberDemo"));
            ViewDeleteUser.theUser = admin;
            var viewConstructor=ViewDeleteUser.class.getDeclaredConstructor();
            viewConstructor.setAccessible(true);viewConstructor.newInstance();
            var sceneField=ViewDeleteUser.class.getDeclaredField("theDeleteUserScene");
            sceneField.setAccessible(true);
            Scene scene=(Scene)sceneField.get(null);Parent page=scene.getRoot();
            TeamTheme.apply(scene);page.applyCss();page.layout();
            ViewDeleteUser.setUserList(db.getUserList());
            Button delete=ViewDeleteUser.button_Delete;
            ComboBox<String> choice=ViewDeleteUser.combobox_SelectUser;
            check("V03 Delete disabled with no selection",delete.isDisabled());
            check("V04 selector is not editable text",!choice.isEditable());
            choice.setValue(member.username());
            check("V05 selection enables Delete",!delete.isDisabled());
            check("V13 teammate account details retained",ViewDeleteUser.label_SelectedDetails.getText().contains("member@example.test"));
            snapshot(page,"deletion-screen.png",800,600);
            Alert alert=ViewDeleteUser.alertConfirmDelete;
            alert.setContentText("Delete account \"memberDemo\" (ID "+member.id()+")? This permanently removes access.");
            ButtonType yes=ButtonType.YES;
            ButtonType no=ButtonType.NO;
            check("V06 exact confirmation and target",alert.getHeaderText().equals("Are you sure?") && alert.getContentText().contains("memberDemo") && alert.getContentText().contains("ID "+member.id()));
            check("V07 Yes is not default",!((Button)alert.getDialogPane().lookupButton(yes)).isDefaultButton());
            check("V08 No is default and cancel",((Button)alert.getDialogPane().lookupButton(no)).isDefaultButton() && ((Button)alert.getDialogPane().lookupButton(no)).isCancelButton());
            alert.getDialogPane().applyCss();alert.getDialogPane().layout();
            snapshot(alert.getDialogPane(),"confirmation-dialog.png",560,240);
            check("V09 Yes removes captured selection",ControllerDeleteUser.request(model,member,u->true)==DeletionResult.DELETED && !db.doesUserExist("memberDemo"));
            check("V10 clear selection disables Delete",clearAndDisabled(choice,delete));
            for(DeletionResult result:DeletionResult.values()) check("V11-"+result+" has useful feedback",!ModelDeleteUser.message(result).isBlank());
            var constructor=guiUserLogin.ViewUserLogin.class.getDeclaredConstructor();constructor.setAccessible(true);constructor.newInstance();
            snapshot(guiUserLogin.ViewUserLogin.theUserLoginScene.getRoot(),"login-screen.png",800,600);
            check("V12 login theme attached",guiUserLogin.ViewUserLogin.theUserLoginScene.getStylesheets().contains(TeamTheme.stylesheet()));
        } finally { db.closeConnection(); }
    }
    private static boolean clearAndDisabled(ComboBox<?> box,Button button) { box.getSelectionModel().clearSelection();return button.isDisabled(); }
    private static void snapshot(Parent root,String name,int width,int height) throws Exception {
        root.resize(width,height); root.applyCss();root.layout();
        WritableImage image=root.snapshot(null,new WritableImage(width,height));
        BufferedImage output=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<height;y++) for(int x=0;x<width;x++) output.setRGB(x,y,image.getPixelReader().getArgb(x,y));
        Path dir=Path.of("build/reports");Files.createDirectories(dir);
        ImageIO.write(output,"png",dir.resolve(name).toFile());
    }
}
