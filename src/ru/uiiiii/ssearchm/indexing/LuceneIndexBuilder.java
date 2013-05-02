package ru.uiiiii.ssearchm.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class LuceneIndexBuilder {
	
	private static final String[] JAVA_STOP_WORDS = { "public", "private",
			"protected", "interface", "abstract", "implements", "extends",
			"null", "new", "switch", "case", "default", "synchronized", "do",
			"if", "else", "break", "continue", "this", "assert", "for",
			"instanceof", "transient", "final", "static", "void", "catch",
			"try", "throws", "throw", "class", "finally", "return", "const",
			"native", "super", "while", "import", "package", "true", "false",
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
			"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
	
	public static void buildIndex(String docsPath, String indexPath) throws IOException, CorruptIndexException, LockObtainFailedException {
		final File docDir = new File(docsPath);

      	Directory dir = FSDirectory.open(new File(indexPath));
      	Analyzer analyzer = new StandardAnalyzerJava(Version.LUCENE_31, getStopWords());
      	IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

        iwc.setOpenMode(OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(dir, iwc);
        luceneIndexDocsRecursive(writer, docDir);
        writer.close();
	}

	private static Set<?> getStopWords() {
		TreeSet<String> stopWords = new TreeSet<String>();
		
		for (String word : JAVA_STOP_WORDS) {
			stopWords.add(word);
		}
		
		for (Object word : StandardAnalyzerJava.STOP_WORDS_SET) {
			stopWords.add(String.valueOf((char[])word));
		}
		
		return stopWords;
	}
	
	private static void luceneIndexDocsRecursive(IndexWriter writer, File file) throws CorruptIndexException, IOException {
		if (file.getName().equals(".git")) {
			return;
		}
		if (file.isDirectory()) {
	        String[] files = file.list();
	        for (String child : files) {
	        	luceneIndexDocsRecursive(writer, new File(file, child));
	        }
		}
		else {
			String extension = "";
			String fileName = file.getName();
			int dotPos = fileName.lastIndexOf('.');
			if (dotPos > 0) {
			    extension = fileName.substring(dotPos + 1);
			}
			
			if (extension.equals("java")) {
				luceneIndexDoc(writer, file);
			}
		}
	}
	
	private static void luceneIndexDoc(IndexWriter writer, File file) throws CorruptIndexException, IOException {
		FileInputStream fis = new FileInputStream(file);
		
        Document doc = new Document();
        Field pathField = new Field("path", file.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        pathField.setIndexOptions(IndexOptions.DOCS_ONLY);
        doc.add(pathField);
        
        doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

        System.out.println("adding " + file);
        writer.addDocument(doc);
        
        fis.close();
	}
}
