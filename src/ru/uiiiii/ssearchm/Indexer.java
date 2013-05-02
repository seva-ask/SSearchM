package ru.uiiiii.ssearchm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class Indexer {
	
	private final static String DOCS_PATH = "C:\\Users\\Всеволод\\Desktop\\lucene-3.6.2-src\\lucene-3.6.2";

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
	    String docsPath = DOCS_PATH;
	    String indexPath = "index";
	    
	    final File docDir = new File(docsPath);
	    
	    Date start = new Date();
	    System.out.println("Indexing to directory '" + indexPath + "'...");

      	Directory dir = FSDirectory.open(new File(indexPath));
      	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
      	IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

        iwc.setOpenMode(OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(dir, iwc);
        
        indexDocsRecursive(writer, docDir);        

        writer.close();

	    Date end = new Date();
	    System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	}
	
	private static void indexDocsRecursive(IndexWriter writer, File file) throws CorruptIndexException, IOException {
		if (file.isDirectory()) {
	        String[] files = file.list();
	        for (String child : files) {
	        	indexDocsRecursive(writer, new File(file, child));
	        }
		}
		else {
			indexDoc(writer, file);
		}
	}
	
	private static void indexDoc(IndexWriter writer, File file) throws CorruptIndexException, IOException {
		FileInputStream fis = new FileInputStream(file);
		
        Document doc = new Document();
        Field pathField = new Field("path", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        pathField.setIndexOptions(IndexOptions.DOCS_ONLY);
        doc.add(pathField);
        
        NumericField modifiedField = new NumericField("modified");
        modifiedField.setLongValue(file.lastModified());
        doc.add(modifiedField);
        
        doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

        System.out.println("adding " + file);
        writer.addDocument(doc);
        
        fis.close();
	}
}
