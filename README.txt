Galago 1.04 

<<<<<<< HEAD
Project is Forked from Lemur Project hosted on sourceforge.net "http://sourceforge.net/projects/lemur/"
=======
Project is Forked from Lemur Project honted on sourceforge.net "http://sourceforge.net/projects/lemur/"

This project is partially complete and still under Development...
>>>>>>> 35ff2559ea96a6c9caa2c7c6f2f937ed1ab11f45

It uses the same set of commands as the origional Galago 1.04 does...

Implemented Temporal Weighted Searching of documents over large text corpus.

Please Install maven2+ and follow the same instructions for setting up the galago on your system 

http://sourceforge.net/p/lemur/wiki/Galago%20Installation/

Then
1. import the galagosearch-core, galagosearch-tupleflow, galagosearch-tupleflow-typebuilder as normal java projects

2. Add ClassPath variable in eclipse M2_REPO which points to maven local repository.

3. The program starts in  App.java in org.galagosearch.core.tools package

<<<<<<< HEAD
NOTE: It is specially designed to work with New York Times Annotated Corpus, please don't try to run any other corpus. You can modify to work with other corpus as well
=======
NOTE: Its is specially designed to work with New York Times Annotated Corpus, please don't try to run any other corpus. You can modify to work with other corpus as well
>>>>>>> 35ff2559ea96a6c9caa2c7c6f2f937ed1ab11f45

Files Modified/Added

Time.java, TimeTuple.java, TimeWrap.java, 

TagTokenizer.java, UniversalParser.java, 

App.java, Search.java, StructuredRetrieval.java

GalagoTimeSearch.jar contains the binary to run the search system its an executable jar

<<<<<<< HEAD
in Terminal/cmd		java -jar GalagoTimeSearch.jar <option> <option> <option>
=======
in Terminal/cmd		java -jar GalagoTimeSearch.jar <option> <option> <option>

>>>>>>> 35ff2559ea96a6c9caa2c7c6f2f937ed1ab11f45
