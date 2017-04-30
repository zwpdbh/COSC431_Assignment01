To compile, cd into the code directory, then:
javac -d ~/Downloads/tmp/  *.java
This will compile the source code into your ~/Downloads/tmp/ directory

cd into the compile source code directory.


Usage:
Assignment Part one:
Parse XML file (takes more than 600 seconds on my computer.):
java Parser <Your_XML_File>

This will generate a directory named "savedInvertedIndex" in the current execution fold
to save the generated inverted index data structure.
The parsed token will also be put into the file named: "tokens.txt" for examination.

Assignment Part two:
After compiling, cd into the generated class file directory, RUN:

For simply testing the searching with pre-generated data, just:
java Dictionary /home/cshome/w/wzhao/cosc431/tmp 20

java Dictionary <absolute_path_to_initializationData> <num_of_results_to_show>
java Dictionary indexed_terms_in_binary postings_records_for_DocIDs postings_records_for_TFs -1

<absolute_path_to_initializationData> specify absolute path to the initialization data.

<num_of_results_to_show> specify the number of related documents to show.
    Because it uses TF-IDF ranking,
    the search results contains all the related documents which is a long list for some terms.
    To show all the results, using -1.

There is also a backup data for saved inverted index at:
/home/cshome/w/wzhao/cosc431/backup_data