/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.server.jaxrs;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.server.ServiceImplementation;
import org.drools.guvnor.server.jaxrs.jaxb.Package;
import org.drools.guvnor.server.util.DroolsHeader;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.jboss.resteasy.plugins.providers.atom.AbderaEntryProvider;
import org.jboss.resteasy.plugins.providers.atom.AbderaFeedProvider;
import org.junit.*;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import static org.junit.Assert.*;
import static org.jboss.resteasy.test.TestPortProvider.*;

public class BasicPackageResourceTest extends RestTestingBase {
    private Abdera abdera = new Abdera();
    String NS = "";
    QName METADATA = new QName(NS, "metadata");
    QName VALUE = new QName(NS, "value");    
    QName ARCHIVED = new QName(NS, "archived");

    @Before @Override
    public void setUpGuvnorTestBase() {
        super.setUpGuvnorTestBase();
        dispatcher.getRegistry().addPerRequestResource(PackageResource.class);
        dispatcher.getProviderFactory().registerProvider(AbderaEntryProvider.class);
        dispatcher.getProviderFactory().registerProvider(AbderaFeedProvider.class);
        
        ServiceImplementation impl = getServiceImplementation();
        //Package version 1(Initial version)
        PackageItem pkg = impl.getRulesRepository().createPackage( "restPackage1",
                                                                   "this is package restPackage1" );

        //Package version 2	
        DroolsHeader.updateDroolsHeader( "import com.billasurf.Board\n global com.billasurf.Person customer1",
                                         pkg );

        AssetItem func = pkg.addAsset( "func",
                                       "" );
        func.updateFormat( AssetFormats.FUNCTION );
        func.updateContent( "function void foo() { System.out.println(version 1); }" );
        func.checkin( "version 1" );

        AssetItem dsl = pkg.addAsset( "myDSL",
                                      "" );
        dsl.updateFormat( AssetFormats.DSL );
        dsl.updateContent( "[then]call a func=foo();\n[when]foo=FooBarBaz1()" );
        dsl.checkin( "version 1" );

        AssetItem rule = pkg.addAsset( "rule1",
                                       "" );
        rule.updateFormat( AssetFormats.DRL );
        rule.updateContent( "rule 'foo' when Goo1() then end" );
        rule.checkin( "version 1" );

        AssetItem rule2 = pkg.addAsset( "rule2",
                                        "" );
        rule2.updateFormat( AssetFormats.DSL_TEMPLATE_RULE );
        rule2.updateContent( "when \n foo \n then \n call a func" );
        rule2.checkin( "version 1" );

        AssetItem rule3 = pkg.addAsset( "model1",
                                        "" );
        rule3.updateFormat( AssetFormats.DRL_MODEL );
        rule3.updateContent( "declare Album1\n genre1: String \n end" );
        rule3.checkin( "version 1" );

        pkg.checkin( "version2" );

        //Package version 3
        DroolsHeader.updateDroolsHeader( "import com.billasurf.Board\n global com.billasurf.Person customer2",
                                         pkg );
        func.updateContent( "function void foo() { System.out.println(version 2); }" );
        func.checkin( "version 2" );
        dsl.updateContent( "[then]call a func=foo();\n[when]foo=FooBarBaz2()" );
        dsl.checkin( "version 2" );
        rule.updateContent( "rule 'foo' when Goo2() then end" );
        rule.checkin( "version 2" );
        rule2.updateContent( "when \n foo \n then \n call a func" );
        rule2.checkin( "version 2" );
        rule3.updateContent( "declare Album2\n genre2: String \n end" );
        rule3.checkin( "version 2" );
        //impl.buildPackage(pkg.getUUID(), true);
        pkg.checkin( "version3" );
    }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackagesForJSON() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();        
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
        connection.connect();
        assertEquals (200, connection.getResponseCode());        
        assertEquals(MediaType.APPLICATION_JSON, connection.getContentType());
        //System.out.println(GetContent(connection));
        //TODO: verify
     }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackagesForXML() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_XML, connection.getContentType());
        //System.out.println(GetContent(connection));
        //TODO: verify
    }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackagesForAtom() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_ATOM_XML, connection.getContentType());
        //System.out.println(GetContent(connection));
        
        InputStream in = connection.getInputStream();
        assertNotNull(in);
		Document<Feed> doc = abdera.getParser().parse(in);
		Feed feed = doc.getRoot();
		assertEquals("/packages", feed.getBaseUri().getPath());
		assertEquals("Packages", feed.getTitle());
		
		List<Entry> entries = feed.getEntries();
		assertEquals(2, entries.size());
		Iterator<Entry> it = entries.iterator();	
		boolean foundPackageEntry = false;
		while (it.hasNext()) {
			Entry entry = it.next();
			if("restPackage1".equals(entry.getTitle())) {
				foundPackageEntry = true;
				List<Link> links = entry.getLinks();
				assertEquals(1, links.size());
				assertEquals("/packages/restPackage1", links.get(0).getHref().getPath());
			}
		}
		assertTrue(foundPackageEntry);
    }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackageForJSON() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_JSON, connection.getContentType());
        //logger.log (LogLevel, GetContent(connection));
    }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackageForXML() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_XML, connection.getContentType());
        //logger.log(LogLevel, GetContent(connection));
    }

    /**
     * Test of getPackagesAsFeed method, of class PackageService.
     */
    @Test
    public void testGetPackageForAtom() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_ATOM_XML, connection.getContentType());
        System.out.println(GetContent(connection));
        
        InputStream in = connection.getInputStream();
        assertNotNull(in);
		Document<Entry> doc = abdera.getParser().parse(in);
		Entry entry = doc.getRoot();
		assertEquals("/packages/restPackage1", entry.getBaseUri().getPath());		
		assertEquals("restPackage1", entry.getTitle());
		assertTrue(entry.getPublished() != null);
		assertEquals("this is package restPackage1", entry.getSummary());
		//assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE.getType(), entry.getContentMimeType().getPrimaryType());
		assertEquals("/packages/restPackage1/binary", entry.getContentSrc().getPath());
		
		List<Link> links = entry.getLinks();
		assertEquals(6, links.size());
		Map<String, Link> linksMap = new HashMap<String, Link>();
		for(Link link : links){
			linksMap.put(link.getTitle(), link);
		}
		
		assertEquals("/packages/restPackage1/assets/drools", linksMap.get("drools").getHref().getPath());		
		assertEquals("/packages/restPackage1/assets/func", linksMap.get("func").getHref().getPath());		
		assertEquals("/packages/restPackage1/assets/myDSL", linksMap.get("myDSL").getHref().getPath());		
		assertEquals("/packages/restPackage1/assets/rule1", linksMap.get("rule1").getHref().getPath());		
		assertEquals("/packages/restPackage1/assets/rule2", linksMap.get("rule2").getHref().getPath());		
		assertEquals("/packages/restPackage1/assets/model1", linksMap.get("model1").getHref().getPath());
		
		ExtensibleElement metadataExtension  = entry.getExtension(METADATA); 
        ExtensibleElement archivedExtension = metadataExtension.getExtension(ARCHIVED);     
		//assertEquals("metadata_type_lifecycle", metadataExtension.getAttributeValue("type"));       
		assertEquals("false", archivedExtension.getSimpleExtension(VALUE)); 
    }

    /* Package Creation */
    @Test @Ignore
    public void testCreatePackageFromJAXB() throws Exception {
        Package p = createTestPackage("TestCreatePackageFromJAXB");
        JAXBContext context = JAXBContext.newInstance(p.getClass());
        Marshaller marshaller = context.createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(p, sw);
        String xml = sw.toString();
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_XML);
        connection.setRequestProperty("Content-Length", Integer.toString(xml.getBytes().length));
        connection.setUseCaches (false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream (
              connection.getOutputStream ());
        wr.writeBytes (xml);
        wr.flush ();
        wr.close ();

        assertEquals (204, connection.getResponseCode());
    }

    /* Package Creation */
    @Test
    public void testCreatePackageFromDRLAsEntry() throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.setDoOutput(true);

        //Send request
        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("simple_rules.drl")));
        DataOutputStream dos = new DataOutputStream (
              connection.getOutputStream ());
        while (br.ready())
            dos.writeBytes (br.readLine());
        dos.flush();
        dos.close();

        /* Retry with a -1 from the connection */
        if (connection.getResponseCode() == -1) {
            url = new URL(generateBaseUrl() + "/packages");
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
            connection.setDoOutput(true);

            //Send request
            br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("simple_rules.drl")));
            dos = new DataOutputStream (
                  connection.getOutputStream ());
            while (br.ready())
                dos.writeBytes (br.readLine());
            dos.flush();
            dos.close();
        }

        assertEquals (200, connection.getResponseCode());
        assertEquals (MediaType.APPLICATION_ATOM_XML, connection.getContentType());
        //logger.log(LogLevel, GetContent(connection));
    }

    @Test
    public void testCreatePackageFromDRLAsJson() throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
        connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
        connection.setDoOutput(true);

        //Send request
        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("simple_rules2.drl")));
        DataOutputStream dos = new DataOutputStream (
              connection.getOutputStream ());
        while (br.ready())
            dos.writeBytes (br.readLine());
        dos.flush();
        dos.close();

        assertEquals (200, connection.getResponseCode());
        assertEquals (MediaType.APPLICATION_JSON, connection.getContentType());
        //logger.log(LogLevel, GetContent(connection));
    }

    @Test
    public void testCreatePackageFromDRLAsJaxB() throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
        connection.setRequestProperty("Accept", MediaType.APPLICATION_XML);
        connection.setDoOutput(true);

        //Send request
        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("simple_rules3.drl")));
        DataOutputStream dos = new DataOutputStream (
              connection.getOutputStream ());
        while (br.ready())
            dos.writeBytes (br.readLine());
        dos.flush();
        dos.close();

        assertEquals (200, connection.getResponseCode());
        assertEquals (MediaType.APPLICATION_XML, connection.getContentType());
        //logger.log(LogLevel, GetContent(connection));
    }

    @Test @Ignore
    public void testCreatePackageFromAtom() throws Exception {
        Package p = createTestPackage("TestCreatePackageFromAtom");
        Entry e = toPackageEntry(p);
        e.setTitle("TestAtomPackageCreation");

        URL url = new URL(generateBaseUrl() + "/packages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", MediaType.APPLICATION_ATOM_XML);
        conn.setRequestProperty("Content-Length", Integer.toString(e.toString().getBytes().length));
        conn.setDoOutput(true);
        e.writeTo(conn.getOutputStream());
        assertEquals(204, conn.getResponseCode());
        conn.disconnect();
    }

    private Entry toPackageEntry (Package p) throws Exception {
        Abdera a = new Abdera();
        Entry e = a.newEntry();
        e.setTitle(p.getTitle());
        e.setUpdated(p.getMetadata().getLastModified());
        e.setPublished(p.getMetadata().getCreated());
        e.addLink("self", generateBaseUrl() + "/packages/" + p.getTitle());
        e.setSummary(p.getDescription());
        return e;
    }

    @Ignore @Test
    public void testCreatePackageFromJson() {
        //TODO: implement test
    }

    @Test
    public void testGetPackageSource() throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/source");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.WILDCARD);
        connection.connect();

        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.TEXT_PLAIN, connection.getContentType());
        String result = GetContent(connection);
        
        assertTrue( result.indexOf( "package restPackage1" ) >= 0 );
        assertTrue( result.indexOf( "import com.billasurf.Board" ) >= 0 );
        assertTrue( result.indexOf( "global com.billasurf.Person customer2" ) >= 0 );
        assertTrue( result.indexOf( "function void foo() { System.out.println(version 2); }" ) >= 0 );
        assertTrue( result.indexOf( "declare Album2" ) >= 0 );
    }

    @Test
    @Ignore
    public void testGetPackageBinary () throws Exception {
        /* Tests package compilation in addition to byte retrieval */
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/binary");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_OCTET_STREAM);
        connection.connect();

        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, connection.getContentType());
        System.out.println(GetContent(connection));
    }

    @Test @Ignore
    public void testUpdatePackageFromJAXB() throws Exception {
        Package p = createTestPackage("TestCreatePackageFromJAXB");
        p.setDescription("Updated description.");
        JAXBContext context = JAXBContext.newInstance(p.getClass());
        Marshaller marshaller = context.createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(p, sw);
        String xml = sw.toString();
        URL url = new URL(generateBaseUrl() + "/packages/TestCreatePackageFromJAXB");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_XML);
        connection.setRequestProperty("Content-Length", Integer.toString(xml.getBytes().length));
        connection.setUseCaches (false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream (
              connection.getOutputStream ());
        wr.writeBytes (xml);
        wr.flush ();
        wr.close ();

        assertEquals (204, connection.getResponseCode());

    }

    @Test @Ignore
    public void testUpdatePackageFromAtom() throws Exception {
        Package p = createTestPackage("org.drools.guvnor.server.jaxrs.test1");
        Entry e = toPackageEntry(p);
        e.addAuthor("Test McTesty");

        URL url = new URL(generateBaseUrl() + "/packages/org.drools.guvnor.server.jaxrs.test1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-type", MediaType.APPLICATION_ATOM_XML);
        conn.setRequestProperty("Content-Length", Integer.toString(e.toString().getBytes().length));
        conn.setDoOutput(true);
        e.writeTo(conn.getOutputStream());

        if (conn.getResponseCode() == -1) {
            conn.disconnect();
            url = new URL(generateBaseUrl() + "/packages/org.drools.guvnor.server.jaxrs.test1");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-type", MediaType.APPLICATION_ATOM_XML);
            conn.setRequestProperty("Content-Length", Integer.toString(e.toString().getBytes().length));
            conn.setDoOutput(true);
            e.writeTo(conn.getOutputStream());

        }

        assertEquals(204, conn.getResponseCode());
        conn.disconnect();
    }

    @Ignore @Test
    public void testUpdatePackageFromJson() {
        //TODO:  implement test
    }

    @Test
    public void testArchivePackage() throws Exception {
        //TODO: Not sure how to get package archiving working, currently breaking as a package is not an asset */
        URL url = new URL(generateBaseUrl() + "/packages/TestCreatePackageFromAtom");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.connect();

        /* Make sure the package is gone */
        url = new URL(generateBaseUrl() + "/packages/TestCreatePackageFromAtom");
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.connect();
        assertEquals (500, connection.getResponseCode());
    }
    
    @Test
    public void testGetPackageVersionsForAtom() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/versions");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_ATOM_XML, connection.getContentType());
        //System.out.println(GetContent(connection));
        
        InputStream in = connection.getInputStream();
        assertNotNull(in);
		Document<Feed> doc = abdera.getParser().parse(in);
		Feed feed = doc.getRoot();
		assertEquals("Version history of restPackage1", feed.getTitle());
		
		List<Entry> entries = feed.getEntries();
		assertEquals(3, entries.size());

		Map<String, Entry> entriesMap = new HashMap<String, Entry>();
		for(Entry entry : entries){
			entriesMap.put(entry.getTitle(), entry);
		}
		
		assertEquals("/packages/restPackage1/versions/1", entriesMap.get("1").getLinks().get(0).getHref().getPath());		
		assertTrue(entriesMap.get("1").getUpdated() != null);		
		assertEquals("/packages/restPackage1/versions/2", entriesMap.get("2").getLinks().get(0).getHref().getPath());		
		assertTrue(entriesMap.get("2").getUpdated() != null);		
		assertEquals("/packages/restPackage1/versions/3", entriesMap.get("3").getLinks().get(0).getHref().getPath());		
		assertTrue(entriesMap.get("3").getUpdated() != null);		
    }
    
    @Test
    public void testGetHistoricalPackageForAtom() throws MalformedURLException, IOException {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/versions/2");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_ATOM_XML);
        connection.connect();
        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_ATOM_XML, connection.getContentType());
        //System.out.println(GetContent(connection));
        
        InputStream in = connection.getInputStream();
        assertNotNull(in);
		Document<Entry> doc = abdera.getParser().parse(in);
		Entry entry = doc.getRoot();
		assertEquals("/packages/restPackage1/versions/2", entry.getBaseUri().getPath());
		assertEquals("restPackage1", entry.getTitle());
		assertEquals("this is package restPackage1", entry.getSummary());
		assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE.getType(), entry.getContentMimeType().getPrimaryType());
		assertEquals("/packages/restPackage1/versions/2/binary", entry.getContentSrc().getPath());
		
		List<Link> links = entry.getLinks();
		assertEquals(6, links.size());
		Map<String, Link> linksMap = new HashMap<String, Link>();
		for(Link link : links){
			linksMap.put(link.getTitle(), link);
		}
		
		assertEquals("/packages/restPackage1/versions/2/assets/drools", linksMap.get("drools").getHref().getPath());		
		assertEquals("/packages/restPackage1/versions/2/assets/func", linksMap.get("func").getHref().getPath());		
		assertEquals("/packages/restPackage1/versions/2/assets/myDSL", linksMap.get("myDSL").getHref().getPath());		
		assertEquals("/packages/restPackage1/versions/2/assets/rule1", linksMap.get("rule1").getHref().getPath());		
		assertEquals("/packages/restPackage1/versions/2/assets/rule2", linksMap.get("rule2").getHref().getPath());		
		assertEquals("/packages/restPackage1/versions/2/assets/model1", linksMap.get("model1").getHref().getPath());   }    

    @Test
    public void testGetHistoricalPackageSource() throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/versions/2/source");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.WILDCARD);
        connection.connect();

        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.TEXT_PLAIN, connection.getContentType());
        String result = GetContent(connection);
        System.out.println(result);
       
        assertTrue( result.indexOf( "package restPackage1" ) >= 0 );
        assertTrue( result.indexOf( "import com.billasurf.Board" ) >= 0 );
        assertTrue( result.indexOf( "global com.billasurf.Person customer1" ) >= 0 );
        assertTrue( result.indexOf( "function void foo() { System.out.println(version 1); }" ) >= 0 );
        assertTrue( result.indexOf( "declare Album1" ) >= 0 );
    }
    
    @Test @Ignore
    public void testGetHistoricalPackageBinary () throws Exception {
        URL url = new URL(generateBaseUrl() + "/packages/restPackage1/versions/1/binary");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.APPLICATION_OCTET_STREAM);
        connection.connect();

        assertEquals (200, connection.getResponseCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, connection.getContentType());
        System.out.println(GetContent(connection));
    }
}
