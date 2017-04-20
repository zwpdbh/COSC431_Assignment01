To compile, cd into the code directory, then:
javac -d ~/Downloads/tmp/  *.java
This will compile the source code into your ~/Downloads/tmp/ directory

Usage:
Part one, parse XML file:
java Parser <Your_XML_File>

This will generate 3 files in the current directory:
"tokens.txt" lists all the tokens from each documents, separated by blank line.

"indexed_terms_in_binary" stores the indexed term records which is a HashMap<Term, PostingsRecords>.
PostingsRecords stores the information about how to read postings record in "postings_records_in_binary".

"postings_records_in_binary" stores the postings records for each term.


Part two, search term:
java Dictionary <terms_records> <postings_records> <num_of_results_to_show>
<terms_records> specify the indexed term records
<postings_records> specify the postings records
<num_of_results_to_show> specify the number of related documents to show. Because it uses TF-IDF ranking,
the search results contains all the related documents which is a long list for some terms.
To show all the results, using -1.
Notice: Print out all the related search results will slow down the excution time.