package search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import index.LuceneConsts;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;
    private QueryParser queryParserSourceID;
    private QueryParser queryParserYear;
    private QueryParser queryParserTitle;
    private QueryParser queryParserAbstract;
    private QueryParser queryParserFullText;
    private Query query;

    // Constructor ,initialize the searcher with the index directory
    public Searcher(String indexDirectoryPath) throws IOException {
        //Open index directory
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        //Create an IndexReader to read index
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        //Create an IndexSearcher to search the index
        indexSearcher = new IndexSearcher(indexReader);
        //Initialize QueryParsers for different fields
        queryParser = new QueryParser(LuceneConsts.CONTENTS, new StandardAnalyzer());
        queryParserSourceID = new QueryParser(LuceneConsts.source_id, new StandardAnalyzer());
        queryParserYear = new QueryParser(LuceneConsts.year, new StandardAnalyzer());
        queryParserTitle = new QueryParser(LuceneConsts.title, new StandardAnalyzer());
        queryParserAbstract = new QueryParser(LuceneConsts.abstract_text, new StandardAnalyzer());
        queryParserFullText = new QueryParser(LuceneConsts.full_text, new StandardAnalyzer());
    }

    //method to perform the search
    public List<Document> search(String searchQuery, String field) throws IOException, ParseException {
        // Parse the search query using the appropriate QueryParser based on the field
        if (field.equals("")) {
            query = queryParser.parse(searchQuery);
        } else if (field.equals("year")) {
            query = queryParserYear.parse(searchQuery);
        } else if (field.equals("source_id")) {
            query = queryParserSourceID.parse(searchQuery);
        } else if (field.equals("title")) {
            query = queryParserTitle.parse(searchQuery);
        } else if (field.equals("abstract")) {
            query = queryParserAbstract.parse(searchQuery);
        } else if (field.equals("full_text")) {
            query = queryParserFullText.parse(searchQuery);
        }

        //Create a  list to store the search results
        ArrayList<Document> resultDocs = new ArrayList<>();

        //Perform search and get the top hits
        ScoreDoc[] hits = indexSearcher.search(query, LuceneConsts.MAX_SEARCH).scoreDocs;
        // For each hit, retrieve the corresponding document and add it to the results list
        for (ScoreDoc hit : hits) {
            Document doc = indexSearcher.doc(hit.doc);
            resultDocs.add(doc);
        }
        // Return the list of matching documents
        return resultDocs;
    }

}
