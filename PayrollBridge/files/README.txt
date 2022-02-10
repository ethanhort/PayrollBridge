This documentation file should serve as a brief documentation for quick changes to the program that require no coding experience. 
For issues not covered in this document, get someone with programming experience to fix it. The program is documented much more 
thoroughly by javadocs and inline comments than it is by this document. For questions regarding the Apache POI library or regarding 
interactions with Microsoft Documents (excel, word, etc.) refer to the Apache POI 4.1.x documentation found at this 
address: https://poi.apache.org/apidocs/4.1/ 

Notice: This document was written with the original user in mind. It might seem overexplanatory or even dismissive in its simplicity,
but I assure you that it was quite necessary. If the reader has any programming experience whatsoever, any further reading of this doc
will be almost entirely pointless. In this case, the reader would be much better served referring to documentation contained within the java
files themselves. This system is not particularly large or complex in any way, and as such, an extensive and dedicated documentation 
system did not seem worthwhile. 

IMPORTANT: Before changing anything, back up ALL files ending with file extension ".java", preferably with some form of version control, 
but at the very least copy the files and paste them in a completely separate directory in some secure location. Do not touch these 
backups until absolutely sure that the edited program works as intended (might not be a bad idea to perpetually keep a copy of the 
previous version of the program). I know you read warnings like this all the time and probably don't listen to them, and it probably
works out fine most of the time, but if you do not know Java and you edit the file, you will undoubtedly break something on your first
attempt. This is quite alright; so long as you have the backup file, you will be able to reattempt as many times as you need. Just delete
the file that you attempted to edit, copy your backup again, paste it in the folder that your original file was in, and reattempt your
edit.


Note that files ending with file extension ".class" are compiled versions of the ".java" files. 
Back these up as well if you would like, but know that you can always generate a ".class" file so long as you still have the 
corresponding ".java" file, and you must generate a new ".class" file after making changes to the corresponding ".java" file. If 
new class files are not generated, the program will still run, but the behavior of the program will not change.  


To begin editing (after appropriate backups), open the desired ".java" file by performing a single left click on the file name
such that the name of the file is highlighted in your file explorer. Then right click the file once and select the "open with" option.
Your computer will likely not know how to open ".java" files, and you should choose a program to open the file with. For our purposes
any basic text editor should do the trick like notepad (installed by defualt on windows machines), notepadd++, Sublime, or your 
editor of choice (note that these programs will probably be listed under an option like "choose a program" or "more apps" depending 
on your operating system). You almost certainly will have some text editor installed on your system already and will not need to 
download a new one.  

DIRECTIONS TO RUN PROGRAM:
I haven't finished the program yet so I don't know how we're going to run it. 


FIXING PROBLEMS: 
Most likely, the only problems that you're going to be able to fix without someone who knows Java are problems arising from formatting 
changes to the input files. If the input files change to some format other than an excel spreadsheet, the entire program will break
and it's probably not worth fixing. 

Changes to ledger file from ADP: Currently the program determines what is and isn't part of the ledger table by searching the Ledger Number column of 
the file for the first text that contains only numeric characters, and determines the end of the table by finding the next cell in this 
column that does not contain only numeric characters.

*If a cell is added to this column above the first ledger number that contains only numbers, but is not a part of the table, the program will 
 break. It will be much easier to get ADP to change the file than it will be to adjust the program to account for this. Similarly if a cell 
 is added directly below the last ledger number and that cell contains only numeric characters, the program will break, and once again it 
 should be much easier to get ADP to fix this. 
*If a cell in between the first ledger number and the last ledger number contains anything but a ledger number, the program will break, and if 
 a ledger number contains anything but a numeric digit (including a decimal point, letter, or probably even a space -- might still work with 
 a space, not sure), the program will break. Once again try to get ADP to fix this. 
*Currently Ledger Numbers are entered as text(type = 2) in the excel file, distribution codes are also text, while debit and credit totals
 are both entered as numbers (type = 1). If any of these change for some reason, the program will break (it will probably say something like 
 "not able to get a STRING value from a NUMERIC cell"). Try to find which column(s) caused the issue (excel has a type function) and get ADP to 
 change it back. 
*If a problem arises with a listed solution of "contact ADP", but it is impossible or impractical to do so for some reason, get someone with
 experience in Java or general programming experience. Any problem listed in this file should be easily fixed after a quick look at the code.  
*Finally, the program currently assumes that ledger numbers will be listed in the first column, distribution codes in the second column,
 debit totals in the fourth column, and credit totals in the fifth column. If this changes for some reason, it is easily fixable. Open the 
 "PayrollBridge.java" file as described above and scroll down until you see the following lines of code:
 	public static final int GL_COL_INDEX = 0; 
	public static final int GL_DIST_COL_INDEX = 1; 
	public static final int DEBIT_COL_INDEX = 3;
	public static final int CREDIT_COL_INDEX = 4; 
 They should be near the top of the file. If the positions of the columns in the excel file change, delete the number after the equal sign and 
 write the appropriate number instead. Note that computers are zero-indexed. This means that the first column is position 0, the second column 
 is position 1, and so forth. If, for example, the ledger number column was changed to be the third column instead of the first, you would 
 change GL_COL_INDEX to equal 2 NOT 3. AFTER EDITING ENSURE THAT EACH OF THESE LINES ENDS WITH A SINGLE SEMICOLON DIRECTLY AFTER THE NUMBER.
 Make sure to save the ".java" file before you close the editor. This can be done with the key combo CTRL+S or by selecting "file->save as" 
 just as you would do in a word document. Remember to update this document with the updated values after editing so that it makes sense to 
 future maintainers. Finally, we must recompile our java files. Refer to the RECOMPILING FILES section at the end of this document to do so.  


Distribution code file: Dont know havent done it

RECOMPILING FILES: 
After any edits to the java files, the files must be recompiled. Begin by deleting (or moving) all files that end with the extension ".class".
DO NOT DELETE FILES ENDING IN ".java" OR ANY BACKUP FILES THAT HAVE BEEN MADE. I will explain how to do this after I look up how to do it 
again.

Note: Information about library licensing is including in txt files in the "lib" directory.  