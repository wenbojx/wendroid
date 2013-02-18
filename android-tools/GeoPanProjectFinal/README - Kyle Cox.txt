README

GeoPan
by 
Kyle Cox
Yoav Peeri

////////////////////
//  INTRODUCTION  //
////////////////////
*GeoPan is a panoramic photo stitching program, which geolocation from the cameras gps to save the location of where each picture was taken.

*The pictures can then be viewed on an interactive map, with pins displaying the location of where each picture was taken.

*Tapping on a pin will display the image that was taken at that location, and gives options to zoom, pan, get information about, and delete the picture

//////////////
//  SQLITE  //
//////////////
The way the pictures are stored is by saving related metadata about the picture, stored in four columns:  The time and day which it was taken (which is saved as the filename), the latitude and longitude of where the picture was taken, and the directory path of where the picture is stored on the SD card.  

When the program starts, it checks to see if the needed table exists or not, and if it does not exist, then create it.  The SQL table receives inserts from the camera intent every time the user begins taking a panoramic picture.  The map intent calls a select statement, which grabs all rows of information, and uses them to plot pins on the map.  Finally, a delete statement is used in the picture viewer intent when the user wishes to delete a specific picture, from which the project name is passed.

Additionally, a small SQL table is included when the user installs the application.  This demo database contains three entries which include the Grand Canyon, Rocky Mountain National Park, and the top of the Empire State Building.  These pictures, however, are not included by default, as they have to come from the SD card.  They are available on the website to download and store on the device to test.  

To load the demo table, from the main menu, simply press the menu button.  Two options will appear, which allow you to load in the demo database, or remove it.

/////////////
//  CAMERA //
/////////////
The camera starts by first obtaining the users coordinates, being as accurate as possible.  Additionally, when the camera starts, a new project_name is created based on the current system time.  A file path of where the picture will be stored is also created under the directory "/sdcard/geopan/*project_name*/mosaic.jpg. When the user takes the picture, a line is inserted into the SQLite table, and the picture is stored to the camera.

///////////
//  MAP  //
///////////
When the map starts, a rough location is obtained, and the map is zoomed far out, in order for the user to have a relatively centered, yet wide view of all the panoramic pictures they have taken.  

There are two measures taken from preventing the user from viewing the map when it contains no pictures.  The first is from the main menu: when the user presses the "View Pictures" button, a simple check of the database is done to see if any pictures exist or not.  If no pictures exist, the user is alerted and not permitted to view the map, as it would be useless.  The second action is to check for pictures when the user comes back from the picture viewer intent.  If the user has deleted the last picture and switches to the map, the map first does a check to see if there are any pictures, and if there are not, the user is sent back to the main menu.

As the map loads, it does a select statement from the SQL database, and pulls all of the rows available.  Each row is parsed into four parts: The second and third columns are saved as the pins latitude and longitude.  The fourth pin is the file location, and is saved as a property of the pin overlay item.  Once a pin overlay item is created, it is stored into an array, and the loop continues until each row has been added to the array.  Once completed, all of the pin overlay items are added onto the map.

To display a picture, the onTap function is used, and the file path property is stored.  Next, the picture viewer intent is created, to which gets passed the file path variable.

//////////////////////
//  PICTURE VIEWER  //
//////////////////////
The picture viewer uses a quite complicated custom activity.  It allows for simple scrolling and panning on devices that do not have the multitouch ability, and instead a slide up or down of a finger to zoom in and out.

Pressing the menu button reveals several more options.  The pan option switches the user input of the image from zooming to panning the picture.  The panning also allows the user to move the picture pas the edge of the screen, allowing more freedom when viewing a picture.  

The reset button simply resets the view back to the default size, which is determined by the first axis to reach the edge of the screen.

The delete option will delete the picture from the database.  When the user switches back to the map view, all pins are read from the database, and thus the one chosen for deletion will no longer appear.

Finally the info button is used to display the date and time the picture was taken.  Because the filename of the picture was chosen to be the system time in milliseconds, it can easily be read and formatted to serve not only as a unique name, but useful information as well.