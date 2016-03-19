# multilingual-index-search
Indexing files and searching index
##Features
Project Indexer stores files' name, contents and high tf*idf content's terms in an index.

Text in German, Greek, English, Spanish, French and Italian language is being analyzed.

Project Searcher searches for a word or a phrase in file's metadata and contents'terms with

high tf*idf value, that have been indexed by project Indexer. You could schedule project Indexer

to run automatically and integrate project Searcher in your application to search the index.
## Usage
Indexer:

args[0]: directory to be indexed

args[1]: directory of index to be stored

in windows cmd:

1. cd in project indexer.

2. path\to\Indexer>java -cp bin;lib\jsonic-1.2.0.jar;lib\langdetect.jar;lib\lucene-analyzers-common-5.0.0.jar;lib\lucene-core-5.0.0.jar;lib\tika-app-1.7.jar indexer.Ind exer directory index_directory

Searcher:

args[0]: query

args[1]: query's language

args[2]: directory of stored index

in windows cmd:

1. cd in project Searcher.

2. path\to\Searcher>java -cp bin;lib\lucene-analy zers-common-5.0.0.jar;lib\lucene-core-5.0.0.jar;lib\lucene-queryparser-5.0.0.jar searcher.Searcher query en index_directory

In args[1] type:

"de" for German

"el" for Greek

"en" for English

"es" for Spanish

"fr" for French

"it" for Italian

###### note: indexer's args[1] and searcher's args[1] must be the same directory
