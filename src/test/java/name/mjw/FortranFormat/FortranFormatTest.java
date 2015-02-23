package name.mjw.FortranFormat;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;

import name.mjw.FortranFormat.FortranFormat.SpecificationStringInterpreter;

public class FortranFormatTest {

	@Test
	public void testNullInputThrowsError() throws Exception {
		try {
			new SpecificationStringInterpreter(null);
			fail();
		} catch (final Exception e) {
			assertEquals("The format specification string may not be null.",
					e.getMessage());
		}
	}

	@Test
	public void testCommaInsertionsWithBasicRepeatableDescriptors()
			throws Exception {
		assertEquals("I4,I4,I4",
				new SpecificationStringInterpreter("()").checkCommas("I4I4I4"));
		assertEquals("I4,I4,I4",
				new SpecificationStringInterpreter("()")
						.checkCommas("I4,I4,I4"));
		assertEquals("I4,I4,I4",
				new SpecificationStringInterpreter("()").checkCommas("I4,I4I4"));
		assertEquals("I4,I4,I4",
				new SpecificationStringInterpreter("()").checkCommas("I4I4,I4"));
		assertEquals("2I4",
				new SpecificationStringInterpreter("()").checkCommas("2I4"));
		assertEquals("(I4)",
				new SpecificationStringInterpreter("()").checkCommas("(I4)"));
	}

	@Test
	public void testCommaInsertionsWithDecimal() throws Exception {
		assertEquals("(I4,I4,I4,F4.2)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(I4I4I4F4.2)"));
		assertEquals("(I4,I4,F4.2,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(I4I4F4.2I4)"));
		assertEquals("(I4,F4.2,I4,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(I4F4.2I4I4)"));
		assertEquals("(F4.2,I4,I4,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(F4.2I4I4I4)"));
	}

	@Test
	public void testCommaInsertionsWithHorizontalPositioning() throws Exception {
		assertEquals("1X,1X,1X",
				new SpecificationStringInterpreter("()").checkCommas("1X1X1X"));
		assertEquals("(1X,1X,1X)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(1X1X1X)"));
		assertEquals("(1X,1X)1X",
				new SpecificationStringInterpreter("()")
						.checkCommas("(1X1X)1X"));
		assertEquals("(1X,1X)1X,I4",
				new SpecificationStringInterpreter("()")
						.checkCommas("(1X1X)1XI4"));
		assertEquals("I4(1X,1X)1X,I4",
				new SpecificationStringInterpreter("()")
						.checkCommas("I4(1X1X)1XI4"));
		assertEquals("I4,I4(1X,1X)1X,I4", new SpecificationStringInterpreter(
				"()").checkCommas("I4I4(1X1X)1XI4"));
		assertEquals("F4.2,I4,I4(1X,1X)1X,I4",
				new SpecificationStringInterpreter("()")
						.checkCommas("F4.2I4I4(1X1X)1XI4"));
	}

	@Test
	public void testCommaInsertionsWithMultipliers() throws Exception {
		assertEquals("2I4,I4,I4",
				new SpecificationStringInterpreter("()").checkCommas("2I4I4I4"));
		assertEquals("(2I4,I4,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(2I4I4I4)"));
		assertEquals("(2I4,2(I4),I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(2I4,2(I4)I4)"));
		assertEquals("(2I4,2I4,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(2I4,2I4I4)"));
		assertEquals("(2F4.2,I4,I4)",
				new SpecificationStringInterpreter("()")
						.checkCommas("(2F4.2I4I4)"));
	}

	@Test
	public void testCommasNotInsertedIfEverythingIsCorrect() throws Exception {
		assertEquals("2I4,5X,4I4,2F4.2", new SpecificationStringInterpreter(
				"()").checkCommas("2I4,5X,4I4,2F4.2"));
		assertEquals(
				"F4.2,A5,F4.2,A5,2F4.2,A5,E4.2E2,4ES5.3E2,4ES5.3E2,E4.2E2",
				new SpecificationStringInterpreter("()")
						.checkCommas("F4.2,A5,F4.2,A5,2F4.2,A5,E4.2E2,4ES5.3E2,4ES5.3E2,E4.2E2"));
	}

	@Test
	public void testDescriptorsAreMultipliedOut() throws Exception {
		assertEquals("I4,I4,I4,I4",
				new SpecificationStringInterpreter("()").multiplyOut("4I4"));
		assertEquals("(I4,I4,I4,I4)",
				new SpecificationStringInterpreter("()").multiplyOut("(4I4)"));
		assertEquals("I4,I4,5X,I4,I4,I4,I4,F4.2,F4.2",
				new SpecificationStringInterpreter("()")
						.multiplyOut("2I4,5X,4I4,2F4.2"));
		assertEquals("(I4,I4,5X,I4,I4,I4,I4,F4.2,F4.2)",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(2I4,5X,4I4,2F4.2)"));
	}

	@Test
	public void testParenthesisAreMultipliedOut() throws Exception {
		assertEquals("(I4)(I4)(I4)(I4)", new SpecificationStringInterpreter(
				"()").multiplyOut("4(I4)"));
		assertEquals("((I4)(I4)(I4)(I4))", new SpecificationStringInterpreter(
				"()").multiplyOut("(4(I4))"));
		assertEquals("((I4,I4)(I4,I4)(I4,I4)(I4,I4))",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(4(I4,I4))"));
		assertEquals("((I4,I4,I4,I4)(I4,I4,I4,I4)(I4,I4,I4,I4)(I4,I4,I4,I4))",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(4(I4,3I4))"));
		assertEquals("(I4,I4,(5X)(5X)(5X)(5X)(5X),I4,I4,I4,I4,F4.2,F4.2)",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(2I4,5(5X),4I4,2F4.2)"));
		assertEquals("(I4,I4,5X,I4,I4,I4,I4,(F4.2,F4.2)(F4.2,F4.2))",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(2I4,5X,4I4,2(2F4.2))"));
		assertEquals("((I4I4I4)(I4I4I4)(A5)(A5))",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(2(I4I4I4)2(A5))"));
		assertEquals("((I4I4I4)(I4I4I4)A2(A5))",
				new SpecificationStringInterpreter("()")
						.multiplyOut("(2(I4I4I4)A2(A5))"));
	}

	@Test
	public void testParenthesisRemoved() throws Exception {
		// must contain root parenthesis
		assertEquals("I4,I4,I4,I4",
				new SpecificationStringInterpreter("()")
						.removeParenthesis("((I4)(I4)(I4)(I4))"));
		assertEquals("I4,I4,I4,I4,I4,I4,I4,I4",
				new SpecificationStringInterpreter("()")
						.removeParenthesis("((I4,I4)(I4,I4)(I4,I4)(I4,I4))"));
		assertEquals(
				"I4,I4,5X,5X,5X,5X,5X,I4,I4,I4,I4,F4.2,F4.2",
				new SpecificationStringInterpreter("()")
						.removeParenthesis("(I4,I4,(5X)(5X)(5X)(5X)(5X),I4,I4,I4,I4,F4.2,F4.2)"));
		assertEquals(
				"I4,I4,5X,I4,I4,I4,I4,F4.2,F4.2,F4.2,F4.2",
				new SpecificationStringInterpreter("()")
						.removeParenthesis("(I4,I4,5X,I4,I4,I4,I4,(F4.2,F4.2)(F4.2,F4.2))"));
	}

	@Test
	public void testParenthesisErrors() throws Exception {
		try {
			new SpecificationStringInterpreter("I4I4I4");
			fail();
		} catch (final Exception e) {
			assertEquals(
					"Fortran format specification strings must begin with an open parenthesis '(' and end with a close parenthesis ')'. Blank spaces are tolerated before an open parenthesis and any characters are tolerated after a close parenthesis. No characters outside of the root parenthesis affect the format specification.",
					e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("I4I4I4)");
			fail();
		} catch (final Exception e) {
			assertEquals(
					"Fortran format specification strings must begin with an open parenthesis '(' and end with a close parenthesis ')'. Blank spaces are tolerated before an open parenthesis and any characters are tolerated after a close parenthesis. No characters outside of the root parenthesis affect the format specification.",
					e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("(I4I4I4");
			fail();
		} catch (final Exception e) {
			assertEquals("Missing a close parenthesis.", e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("2(I4I4I4)");
			fail();
		} catch (final Exception e) {
			assertEquals("Only spaces may precede the root parenthesis.",
					e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("(I4I4I4(I4I4I4)");
			fail();
		} catch (final Exception e) {
			assertEquals("Missing a close parenthesis.", e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("randomtext(I4I4I4)randomtext");
			fail();
		} catch (final Exception e) {
			assertEquals("Only spaces may precede the root parenthesis.",
					e.getMessage());
		}
		try {
			new SpecificationStringInterpreter("(5(6(A5))");
			fail();
		} catch (final Exception e) {
			assertEquals("Missing a close parenthesis.", e.getMessage());
		}
	}

	@Test
	public void testInterpreterWorks() throws Exception {
		assertEquals("I4,I4,I4",
				new SpecificationStringInterpreter("(I4I4I4)")
						.getCompletedInterpretation());
		assertEquals("I4,I4,I4", new SpecificationStringInterpreter(
				"(I4I4I4)(I4I4I4)").getCompletedInterpretation());
		assertEquals("I4,I4,I4", new SpecificationStringInterpreter(
				"(I4I4I4)I4I4I4)").getCompletedInterpretation());
		assertEquals("I4,I4,I4,I4,I4,I4,A5,A5",
				new SpecificationStringInterpreter("(2(I4I4I4)2(A5))")
						.getCompletedInterpretation());
		assertEquals("I4,I4,I4,I4,I4,I4,A2,A5",
				new SpecificationStringInterpreter("(2(I4I4I4)A2(A5))")
						.getCompletedInterpretation());
		assertEquals("I4,I4,I4,I4,I4,I4,A2,A5",
				new SpecificationStringInterpreter(
						"( 2 ( I 4 I 4 I 4 ) A 2 ( A 5 ) )")
						.getCompletedInterpretation());
		assertEquals("I4,I4,I4", new SpecificationStringInterpreter(
				"     (I4I4I4)randomtext").getCompletedInterpretation());
		assertEquals(
				"A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5,A5",
				new SpecificationStringInterpreter("(5(6(A5)))")
						.getCompletedInterpretation());
		assertEquals(
				"I2,I2,I2,I2,I2,I10,I10,I2,I2,I2,I2,I2,I10,I10,I2,I2,I2,I2,I2,I10,I10",
				new SpecificationStringInterpreter("(3(5(I2)2(I10)))")
						.getCompletedInterpretation());
		assertEquals(
				"I2,I2,I2,I2,I2,I10,I10,I2,I2,I2,I2,I2,I10,I10,I2,I2,I2,I2,I2,I10,I10",
				new SpecificationStringInterpreter("(3(5(I2)2I10)))")
						.getCompletedInterpretation());
		assertEquals("I4,A2,A4,5X", new SpecificationStringInterpreter(
				"(I4,A2,A4,5X)").getCompletedInterpretation());
		assertEquals("I4,A4,A4,A4,I4,I4,A4,A4,A4,I4,A5,A5",
				new SpecificationStringInterpreter("(2(I4,3A4,I4),2(A5))")
						.getCompletedInterpretation());
	}

	@Test
	public void testUnitGeneration() throws Exception {
		assertEquals("[I4 , I4 , I4 ]", new SpecificationStringInterpreter(
				"(I4I4I4)").getUnits().toString());
		assertEquals("[I4 , I4 , I4 ]", new SpecificationStringInterpreter(
				"(I4I4I4)(I4I4I4)").getUnits().toString());
		assertEquals("[I4 , I4 , I4 ]", new SpecificationStringInterpreter(
				"(I4I4I4)I4I4I4)").getUnits().toString());
		assertEquals("[I4 , I4 , I4 , I4 , I4 , I4 , A5 , A5 ]",
				new SpecificationStringInterpreter("(2(I4I4I4)2(A5))")
						.getUnits().toString());
		assertEquals("[I4 , I4 , I4 , I4 , I4 , I4 , A2 , A5 ]",
				new SpecificationStringInterpreter("(2(I4I4I4)A2(A5))")
						.getUnits().toString());
		assertEquals("[I4 , I4 , I4 , I4 , I4 , I4 , A2 , A5 ]",
				new SpecificationStringInterpreter(
						"( 2 ( I 4 I 4 I 4 ) A 2 ( A 5 ) )").getUnits()
						.toString());
		assertEquals("[I4 , I4 , I4 ]", new SpecificationStringInterpreter(
				"     (I4I4I4)randomtext").getUnits().toString());
		assertEquals(
				"[A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 , A5 ]",
				new SpecificationStringInterpreter("(5(6(A5)))").getUnits()
						.toString());
		assertEquals(
				"[I2 , I2 , I2 , I2 , I2 , I10 , I10 , I2 , I2 , I2 , I2 , I2 , I10 , I10 , I2 , I2 , I2 , I2 , I2 , I10 , I10 ]",
				new SpecificationStringInterpreter("(3(5(I2)2(I10)))")
						.getUnits().toString());
		assertEquals(
				"[I2 , I2 , I2 , I2 , I2 , I10 , I10 , I2 , I2 , I2 , I2 , I2 , I10 , I10 , I2 , I2 , I2 , I2 , I2 , I10 , I10 ]",
				new SpecificationStringInterpreter("(3(5(I2)2I10)))")
						.getUnits().toString());
		assertEquals("[I4 , A2 , A4 , X5 ]",
				new SpecificationStringInterpreter("(I4,A2,A4,5X)").getUnits()
						.toString());
		assertEquals(
				"[I4 , A4 , A4 , A4 , I4 , I4 , A4 , A4 , A4 , I4 , A5 , A5 ]",
				new SpecificationStringInterpreter("(2(I4,3A4,I4),2(A5))")
						.getUnits().toString());
		assertEquals("[F4.2 , F4.2 ]", new SpecificationStringInterpreter(
				"(2F4.2)").getUnits().toString());
		assertEquals("[F4.2 , F4.2 , ES4.2E2 , ES4.2E2 , ES4.2E2 ]",
				new SpecificationStringInterpreter("(2F4.2,3ES4.2E2)")
						.getUnits().toString());
		assertEquals("[F4.2 , F4.2 , X5 , ES4.2E2 , ES4.2E2 , ES4.2E2 ]",
				new SpecificationStringInterpreter("(2F4.2,5X,3ES4.2E2)")
						.getUnits().toString());
		assertEquals(
				"[F4.2 , F4.2 , A7 , E2.1E1 , X5 , ES4.2E2 , ES4.2E2 , ES4.2E2 ]",
				new SpecificationStringInterpreter(
						"(2F4.2,A7,E2.1E1,5X,3ES4.2E2)").getUnits().toString());
	}

	@Test
	public void testUnsupportedEditDescriptorException() throws Exception {
		try {
			new SpecificationStringInterpreter("(2Y2)").getUnits();
			fail();
		} catch (final Exception e) {
			assertEquals("Unsupported Edit Descriptor: Y", e.getMessage());
		}
	}

	@Test
	public void testIntegers() throws Exception {
		// write
		final ArrayList<Object> ints = new ArrayList<Object>();
		ints.add(123);
		ints.add(-123);
		ints.add(123456);
		assertEquals("  123 -123*****", FortranFormat.write(ints, "(3I5)"));
		assertEquals("  123 -123*****", FortranFormat.write(ints, "(3I5.2)"));
		assertEquals(" 0123-0123*****", FortranFormat.write(ints, "(3I5.4)"));
		assertEquals("00123**********", FortranFormat.write(ints, "(3I5.5)"));
		// read
		assertEquals("[135, 135, 135, 135]",
				FortranFormat.read("1 3 5 135 135    135", "(4I5)").toString());
		assertEquals("[1, 2, 34, 56]",
				FortranFormat.read("12 34  56  78  90", "(I1, I2, I3, I4)")
						.toString());
	}

	@Test
	public void testRealsF() throws Exception {
		// write
		final ArrayList<Object> floats = new ArrayList<Object>();
		floats.add(123.345f);
		floats.add(-123.345f);
		assertEquals("      123.     -123.",
				FortranFormat.write(floats, "(2F10.0)"));
		assertEquals("     123.3    -123.3",
				FortranFormat.write(floats, "(2F10.1)"));
		assertEquals("    123.35   -123.35",
				FortranFormat.write(floats, "(2F10.2)"));
		assertEquals("   123.345  -123.345",
				FortranFormat.write(floats, "(2F10.3)"));
		assertEquals("  123.3450 -123.3450",
				FortranFormat.write(floats, "(2F10.4)"));
		assertEquals(" 123.34500-123.34500",
				FortranFormat.write(floats, "(2F10.5)"));
		assertEquals("123.345001**********",
				FortranFormat.write(floats, "(2F10.6)"));
		assertEquals("********************",
				FortranFormat.write(floats, "(2F10.7)"));
		// read
		assertEquals("[1.23, 4.5, 19.4]",
				FortranFormat.read("1 2 3 4.5 1 9.4", "(F5.2,F5.2,F5.2)")
						.toString());
		assertEquals("[1.2345E20]", FortranFormat.read("12345E20", "(F10.4)")
				.toString());
		assertEquals(
				"[12.0, 3.4, 5600000.000000001, 8.9]",
				FortranFormat.read("12 3.4  56E 78.  90",
						"(F3.0, F4.1, F6.2, F7.3)").toString());
		assertEquals("[123.4, 0.05, 6.0000000000000005E78, 0.9]", FortranFormat
				.read("12 3.4  56E 78.  90", "(F6.1, F3.2, F5.0, F6.1)")
				.toString());
	}

	@Test
	public void testRealsE() throws Exception {
		// write
		final ArrayList<Object> doubles = new ArrayList<Object>();
		doubles.add(Math.PI);
		assertEquals(" 0.31416E+01", FortranFormat.write(doubles, "(E12.5)"));
		assertEquals(" 0.314E+0001", FortranFormat.write(doubles, "(E12.3E4)"));
		assertEquals("0.3141593E+1", FortranFormat.write(doubles, "(E12.7E1)"));
	}

	@Test
	public void testScientific() throws Exception {
		// write
		final ArrayList<Object> doubles = new ArrayList<Object>();
		doubles.add(34.5678);
		assertEquals("  3.457E+001", FortranFormat.write(doubles, "(ES12.3E3)"));
	}

	@Test
	public void testEngineering() throws Exception {
		// write
		final ArrayList<Object> doubles = new ArrayList<Object>();
		doubles.add(1234.567);
		doubles.add(0.00001234567);
		assertEquals("  1.235E+003 12.346E-006",
				FortranFormat.write(doubles, "(2EN12.3E3)"));
	}

	@Test
	public void testLogicals() throws Exception {
		// write
		final ArrayList<Object> booleans = new ArrayList<Object>();
		booleans.add(true);
		booleans.add(false);
		assertEquals("T F", FortranFormat.write(booleans, "(L1,L2)"));
		assertEquals("  T   F", FortranFormat.write(booleans, "(L3,L4)"));
		// read
		assertEquals("[false, true, true]",
				FortranFormat.read("Fax  Trust   Thursday", "(L3, L8, L10)")
						.toString());
	}

	@Test
	public void testCharacters() throws Exception {
		// write
		final ArrayList<Object> characters = new ArrayList<Object>();
		characters.add("12345");
		characters.add("*");
		assertEquals("1*", FortranFormat.write(characters, "(A1,A)"));
		assertEquals("12*", FortranFormat.write(characters, "(A2,A)"));
		assertEquals("123*", FortranFormat.write(characters, "(A3,A)"));
		assertEquals("1234*", FortranFormat.write(characters, "(A4,A)"));
		assertEquals("12345*", FortranFormat.write(characters, "(A5,A)"));
		assertEquals(" 12345*", FortranFormat.write(characters, "(A6,A)"));
		assertEquals("  12345*", FortranFormat.write(characters, "(A7,A)"));
		assertEquals("12345*", FortranFormat.write(characters, "(A,A)"));
		// read
		assertEquals("[ABCD, EFGHI, JKLMNOP, ]",
				FortranFormat.read("ABCDEFGHIJKLMNOPQRST", "(A4, A5, A7, A)")
						.toString());
		assertEquals("[ABCDE, FGHIJ, KLMNO, PQRST]",
				FortranFormat.read("ABCDEFGHIJKLMNOPQRST", "(4A5)").toString());
	}

	@Test
	public void testHorizontalSpaces() throws Exception {
		// write
		final ArrayList<Object> os = new ArrayList<Object>();
		os.add(12);
		os.add(768);
		os.add(3.715);
		assertEquals("  12     768   3.71",
				FortranFormat.write(os, "(1X,I3,3X,I5,2X,F5.2)"));
	}

	@Test
	public void testTabs() throws Exception {
		// write
		final ArrayList<Object> os = new ArrayList<Object>();
		os.add(123);
		os.add(456);
		assertEquals("  456 123", FortranFormat.write(os, "(T6,I4,T2,I4)"));
	}

	@Test
	public void testTabsAll() throws Exception {
		// write
		final ArrayList<Object> os = new ArrayList<Object>();
		os.add(123);
		os.add(456);
		os.add(789);
		assertEquals("   456   12789",
				FortranFormat.write(os, "(T10,I3,TL9,I3,TR5,I3)"));
	}

	@Test
	public void testVerticalPositioning() throws Exception {
		// write
		final ArrayList<Object> os = new ArrayList<Object>();
		os.add(123);
		os.add(456);
		os.add("+-*/");
		assertEquals("  123\n\n   456\n +-*/",
				FortranFormat.write(os, "(I5//I6/1X,A)"));
		// read
		assertEquals(
				"[123, 789, 345]",
				FortranFormat.read("  123  456\n  789  012\n  345  678",
						"(I5/I5/I5)").toString());
	}

	@Test
	public void testShortStringDoesntCauseError() throws Exception {
		assertEquals("[null, , null]", FortranFormat.read("", "(I5A5I5)")
				.toString());
	}

}
