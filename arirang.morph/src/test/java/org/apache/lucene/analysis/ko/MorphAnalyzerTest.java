package org.apache.lucene.analysis.ko;

import java.util.List;

import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;

import junit.framework.TestCase;

public class MorphAnalyzerTest extends TestCase {

	public void testAnalyzer() throws Exception {
		
		String text = "용수환경건물1층바닥slab거푸집설치";
		
		MorphAnalyzer analyzer = new MorphAnalyzer();
		
		List<AnalysisOutput> outputs = analyzer.analyze(text);
		
		for(AnalysisOutput o : outputs) {
			System.out.println(o);
		}
	}
}
