package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Indexer {

    private IndexWriter writer;
    private Analyzer standardAnalyzer;

    private Path indexDirectory;
    private File dpathDirectory;

    // Constructor to initialize paths and analyzer
    public Indexer(String indexDirectoryPath, String dpathDirectoryPath) {
        this.indexDirectory = Paths.get(indexDirectoryPath);
        this.dpathDirectory = new File(dpathDirectoryPath);
        this.standardAnalyzer = new StandardAnalyzer();
    }

    // Method to create the indexer
    public int createIndexer() throws IOException {
        // Initialize Directory and IndexWriter
        Directory dir = FSDirectory.open(indexDirectory);
        IndexWriterConfig indexConfig = new IndexWriterConfig(standardAnalyzer);
        writer = new IndexWriter(dir, indexConfig);
        writer.deleteAll();

        // Get list of .txt files from the directory
        File[] txtFiles = dpathDirectory.listFiles();
        if (txtFiles == null) {
            throw new IOException("No files found in the directory: " + dpathDirectory.getAbsolutePath());
        }
        ArrayList<File> files = new ArrayList<>();
        for (File f : txtFiles) {
            if (f.getName().toLowerCase().endsWith(".txt")) {
                files.add(f);
            }
        }

        // Index each file
        for (File file : files) {
            System.out.println("Indexing: " + file.getCanonicalPath());
            Document doc = new Document();

            // Read file contents
            Field filePathField = new StoredField(LuceneConsts.FILE_PATH, file.getCanonicalPath());
            List<String> lines = Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.ISO_8859_1);
            String content = String.join(System.lineSeparator(), lines);

            Field contents = new TextField(LuceneConsts.CONTENTS, content, Field.Store.YES);
            Scanner scanner = new Scanner(file, StandardCharsets.ISO_8859_1);
            Field source_id = new TextField(LuceneConsts.source_id, scanner.nextLine(), Field.Store.YES);
            Field year = new TextField(LuceneConsts.year, scanner.nextLine(), Field.Store.YES);
            Field title = new TextField(LuceneConsts.title, scanner.nextLine(), Field.Store.YES);
            Field abstract_text = new TextField(LuceneConsts.abstract_text, scanner.nextLine(), Field.Store.YES);
            Field full_text = new TextField(LuceneConsts.full_text, scanner.nextLine(), Field.Store.YES);

            // Add fields to document
            doc.add(filePathField);
            doc.add(source_id);
            doc.add(year);
            doc.add(title);
            doc.add(abstract_text);
            doc.add(full_text);
            doc.add(contents);
            scanner.close();

            writer.addDocument(doc);
        }

        // Commit and close the writer
        writer.commit();
        int numDocs = writer.getDocStats().numDocs;
        writer.close();
        dir.close();

        return numDocs;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(args.length + " arguments required");
            System.out.println("Usage: java Indexer <index dir> <data dir>");
            System.exit(1);
        }
        String indexDir = args[0];
        String dataDir = args[1];

        try {
            Indexer indexer = new Indexer(indexDir, dataDir);
            int numIndexed = indexer.createIndexer();
            System.out.println("Indexing completed. Number of documents indexed: " + numIndexed);
        } catch (IOException e) {
            System.err.println("Indexing failed: " + e.getMessage());
        }
    }
}
