Compilation instructions:

1. Make sure you have Maven installed on your computer:
	- download from here: https://maven.apache.org/download.cgi.
	- make sure to set up an Environment variable (windows users).
	- a short guide in here: https://maven.apache.org/guides/getting-started/windows-prerequisites.html
2. Import as a Maven project
3. Left-click on the project -> Maven -> update project
4. To build, navigate the project root directory via the terminal and use this command:  
	`mvn clean compile assembly:single`
5. A LettersGame.jar file will appear in target/

- The mini app consists of 3 screens. The goal is to correctly identify the letter in the middle of the string. Input is the left and right mouse buttons where the left represent a 'V' and the right represent a 'U'. The input is being read during the time when a string is showing

- note that the data will be saved in a .csv file _in the directory the jar file was launched from_.
If no such file exists - the program will create one for you, however this can lead to multiple files being created and your data scattered across them if you'll launch the game from different directories 


