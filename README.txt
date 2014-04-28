Galago 1.04 

Project is Forked from Lemur Project honted on sourceforge.net "http://sourceforge.net/projects/lemur/"

This project is partially complete and still under Development...

It uses the same set of commands as the origional Galago 1.04 does...

Implemented Temporal Weighted Searching of documents over large text corpus.

Please Install maven2+ and follow the same instructions for setting up the galago on your system 

http://sourceforge.net/p/lemur/wiki/Galago%20Installation/

Then
1. import the galagosearch-core, galagosearch-tupleflow, galagosearch-tupleflow-typebuilder as normal java projects

2. Add ClassPath variable in eclipse M2_REPO which points to maven local repository.

3. The program starts in  App.java in org.galagosearch.core.tools package

NOTE: Its is specially designed to work with New York Times Annotated Corpus, please don't try to run any other corpus. You can modify to work with other corpus as well

Files Modified/Added

Time.java, TimeTuple.java, TimeWrap.java, 

TagTokenizer.java, UniversalParser.java, 

App.java, Search.java, StructuredRetrieval.java

GalagoTimeSearch.jar contains the binary to run the search system its an executable jar

in Terminal/cmd		java -jar GalagoTimeSearch.jar <option> <option> <option>

