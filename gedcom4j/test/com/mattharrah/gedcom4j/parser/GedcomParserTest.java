package com.mattharrah.gedcom4j.parser;

import java.io.IOException;

import junit.framework.TestCase;

import com.mattharrah.gedcom4j.Family;
import com.mattharrah.gedcom4j.Source;

public class GedcomParserTest extends TestCase {

	public void testLoad1() throws IOException, GedcomParserException {
		GedcomParser gp = new GedcomParser();
		gp.load("sample\\TGC551.ged");
		assertTrue(gp.errors.isEmpty());
		assertFalse(gp.warnings.isEmpty());
		for (String s : gp.warnings) {
			System.out.println(s);
			System.out.flush();
		}
		for (String s : gp.errors) {
			System.err.println(s);
			System.err.flush();
		}
		assertEquals("H. Eichmann", gp.gedcom.submitter.name);

		assertEquals(15, gp.gedcom.individuals.size());
		assertEquals(7, gp.gedcom.families.size());
		assertEquals(2, gp.gedcom.sources.size());
		assertEquals(1, gp.gedcom.multimedia.size());
	}

	public void testLoad2() throws IOException, GedcomParserException {
		GedcomParser gp = new GedcomParser();
		gp.load("sample\\allged.ged");
		assertTrue(gp.errors.isEmpty());
		assertFalse(gp.warnings.isEmpty());
		for (String s : gp.warnings) {
			System.out.println(s);
			System.out.flush();
		}
		for (String s : gp.errors) {
			System.err.println(s);
			System.err.flush();
		}
		assertNull(gp.gedcom.submitter.name);
	}

	public void testLoad3() throws IOException, GedcomParserException {
		GedcomParser gp = new GedcomParser();
		gp.load("sample\\a31486.ged");
		assertTrue(gp.errors.isEmpty());
		assertTrue(gp.warnings.isEmpty());
		for (String s : gp.warnings) {
			System.out.println(s);
			System.out.flush();
		}
		for (String s : gp.errors) {
			System.err.println(s);
			System.err.flush();
		}

		// Check submitter
		assertNull(gp.gedcom.submitter.name);

		// Check header
		assertEquals("6.00", gp.gedcom.header.sourceSystem.versionNum);
		assertEquals("(510) 794-6850",
				gp.gedcom.header.sourceSystem.corporation.phoneNumbers.get(0));

		// There are two sources in this file, and their names should be as
		// shown
		assertEquals(2, gp.gedcom.sources.size());
		for (Source s : gp.gedcom.sources.values()) {
			assertTrue(s.title.get(0).equals("William Barnett Family.FTW")
					|| s.title.get(0).equals("Warrick County, IN WPA Indexes"));
		}

		assertEquals(17, gp.gedcom.families.size());
		assertEquals(64, gp.gedcom.individuals.size());

		// Check a specific family
		Family family = gp.gedcom.families.get("@F1428@");
		assertNotNull(family);
		assertEquals(3, family.children.size());
		assertEquals("Lawrence Henry /Barnett/",
				family.husband.names.get(0).basic);
		assertEquals("Velma //", family.wife.names.get(0).basic);

	}

	public void testLoad4() throws IOException, GedcomParserException {
		GedcomParser gp = new GedcomParser();
		// Different line end char seq than the other file
		gp.load("sample\\TGC551LF.ged");
		assertTrue(gp.errors.isEmpty());
		assertFalse(gp.warnings.isEmpty());
		for (String s : gp.warnings) {
			System.out.println(s);
			System.out.flush();
		}
		for (String s : gp.errors) {
			System.err.println(s);
			System.err.flush();
		}
		assertEquals("H. Eichmann", gp.gedcom.submitter.name);

		assertEquals(44, gp.gedcom.individuals.size());
		assertEquals(1, gp.gedcom.families.size());
		assertEquals(1, gp.gedcom.sources.size());
		assertEquals(1, gp.gedcom.multimedia.size());
	}
}
