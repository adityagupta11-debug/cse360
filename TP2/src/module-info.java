module FoundationsF26 {
	requires javafx.controls;
	requires java.sql;
	requires com.h2database;
	
	opens applicationMain to javafx.graphics;
}
