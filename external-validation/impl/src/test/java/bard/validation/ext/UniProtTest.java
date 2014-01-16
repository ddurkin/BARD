package bard.validation.ext;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class UniProtTest {

	private static ExternalOntologyAPI eo;
	
	public UniProtTest() {
		BasicConfigurator.configure();
		for(String name: new String[]{"uk.ac.ebi","org.springframework", "httpclient.wire","org.apache.commons.httpclient"}) {
			Logger logger = Logger.getRootLogger().getLoggerRepository().getLogger(name);
			logger.setLevel(Level.INFO);
		}
	}
	
	@BeforeClass
	public static void initialize() {
		eo = new ExternalOntologyUniprot();
	}

	@Test
	public void testGetById() throws ExternalOntologyException {
		long start = System.currentTimeMillis();
		ExternalItem item = eo.findById("Q07869");
		assertEquals(String.format("%s\t%s", item.getId(), item.getDisplay()), item.getId().equals("Q07869"), true);
		System.out.println("testGetById took (ms): " + (System.currentTimeMillis() - start));
	}

	@Test
	public void testFindMatching() throws ExternalOntologyException {
		long start = System.currentTimeMillis();
		String query = "H9FRU7";
		List<ExternalItem> items = eo.findMatching(query);
        TestUtils.printItemsInfo(items);
		assertEquals(query + " in the gene database should return items", items.size() > 0, true);
		System.out.println(String.format("%s items returned for query " + query, items.size()));
		System.out.println("testFindMatching took (ms): " + (System.currentTimeMillis() - start) );
	}
	
	@Test
	public void testFindMatchingLimitThree() throws ExternalOntologyException {
		long start = System.currentTimeMillis();
		String query = "PPAR";
		List<ExternalItem> items = eo.findMatching(query, 3);
		TestUtils.printItemsInfo(items);
		assertEquals(query + " in the gene database should return items", items.size() == 3, true);
		System.out.println(String.format("%s items returned for query " + query, items.size()));
		System.out.println("testFindMatching took (ms): " + (System.currentTimeMillis() - start) );
	}

    @Test
    public void testFindMatchingGoodId() throws ExternalOntologyException {
        long start = System.currentTimeMillis();
        String query = "Q07869";
        List<ExternalItem> items = eo.findMatching(query);
        TestUtils.printItemsInfo(items);
        assertEquals("should find match by ID", 1, items.size());
        assertEquals("should find match by ID", "Q07869", items.get(0).getId());
        System.out.println(String.format("%s items returned for query " + query, items.size()));
        System.out.println("testFindMatching took (ms): " + (System.currentTimeMillis() - start) );
    }

    @Test
    public void testFindMatchingBadId() throws ExternalOntologyException {
        long start = System.currentTimeMillis();
        String query = "Q00000";
        List<ExternalItem> items = eo.findMatching(query);
        TestUtils.printItemsInfo(items);
        assertEquals("should not find match by ID", 0, items.size());
        System.out.println(String.format("%s items returned for query " + query, items.size()));
        System.out.println("testFindMatching took (ms): " + (System.currentTimeMillis() - start) );
    }

    @Test
	public void testGetByName() throws ExternalOntologyException {
		long start = System.currentTimeMillis();
		String query = "PPARA_HUMAN";
		ExternalItem item = eo.findByName(query);
		assertEquals(String.format("%s\t%s", item.getId(), item.getDisplay()), item != null, true);
		System.out.println("testGetByName took (ms): " + (System.currentTimeMillis() - start) );
	}
	
	public static void main(String[] args) throws Exception {
		UniProtTest t = new UniProtTest();
		t.initialize();
		for(int i: new Integer[]{0,1,2,3,4,5,6,7,8,9}) {
			t.testGetById();
			Thread.sleep(2000);
		}
		for(int i: new Integer[]{0,1,2,3,4,5,6,7,8,9}) {
			t.testGetByName();
			Thread.sleep(2000);
		}
		for(int i: new Integer[]{0,1,2,3,4,5,6,7,8,9}) {
			t.testFindMatching();
			Thread.sleep(2000);
		}
	}
}