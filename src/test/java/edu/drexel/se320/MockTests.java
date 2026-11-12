// The approach that I took to refactor the code so the mock dependency 
// object could be properly injected is "Passing the Collaborator" method.
// I chose this method because it gives us a very straightforward way to use Mockito with the Client. 
// The mock object is guaranteed to be initialized when the client is created, preventing 
// half-initialized states. It's a simple and clean approach that accomplishes 
// the main task of the assignment, unlike the other factory methods discussed in class 
// would be overly complex for the task. A complexity not necessary for the assignment.
package edu.drexel.se320;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InOrder;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MockTests {

    public MockTests() {}

    /**
     * Demonstrate a working mock from the Mockito documentation.
     * https://static.javadoc.io/org.mockito/mockito-core/latest/org/mockito/Mockito.html#1
     */
    @Test
    public void testMockDemo() {
         List<String> mockedList = (List<String>)mock(List.class);

         mockedList.add("one");
         mockedList.clear();

         verify(mockedList).add("one");
         verify(mockedList).clear();
    }

    private ServerConnection mockConnection;
    private Client client;

    @BeforeEach
    public void setUp() {
        mockConnection = mock(ServerConnection.class);
        client = new Client(mockConnection);
    }

    @Test
    public void testServerConnectionFailureGivesNull() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(false);

        // If you change the code to pass the mock above to the client (based on your choice of
        // refactoring), this test should pass.  Until then, it will fail.
        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    // Test 1: Tests if when call to the server fails, no further methods are called.
    @Test
    public void test1() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(false);
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
        verify(mockConnection).connectTo("testServer");
        verifyNoMoreInteractions(mockConnection);
    }

    // Test 2: connection succeeds, file search fails, no other methods called after. 
    // Fails because original client code because code doesn't return null for invalid file cases.
    @Test
    public void test2() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(false);
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
        verify(mockConnection).connectTo("testServer");
        verify(mockConnection).requestFileContents("testFile.txt");
        verify(mockConnection, times(1)).closeConnection();
        verifyNoMoreInteractions(mockConnection);
    }

    // Test 3: tests if connection succeeds and file is valid and not empty, some file content is read.
    @Test
    public void test3() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true, false);
        when(mockConnection.read()).thenReturn("content");
        client.requestFile("testServer", "testFile.txt");
        verify(mockConnection, atLeastOnce()).read();
    }

    // Test 4: tests that if connection succeeds and file is valid but empty, client returns empty string
    @Test
    public void test4() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(false);

        String result = client.requestFile("testServer", "testFile.txt");
        assertEquals("", result);
    }

    // Test 5: tests that if client reads part of a file and then IOException occurs, client returns null and not a partial result.
    @Test
    public void test5() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true);
        when(mockConnection.read()).thenReturn("part1").thenThrow(new IOException("read error"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }

    // Test 6: tests that if initial connection succeeds, but an exception occurs when retrieving a file, the client closes the connection.
    // Fails because closeConnection(), is only called after file is read completely.
    @Test
    public void test6() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenThrow(new IOException("request error"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
        verify(mockConnection).closeConnection();
    }

    // Test 7: tests that if connection succeeds and file is valid and non-empty, the client calls moreBytes() before it calls read().
    @Test
    public void test7() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true, false);
        when(mockConnection.read()).thenReturn("data");
        client.requestFile("testServer", "testFile.txt");
        InOrder inOrder = inOrder(mockConnection);
        inOrder.verify(mockConnection).moreBytes();
        inOrder.verify(mockConnection).read();
    }

    // Test 8: If server returns file in four pieces, the client concatenates the text in the correct order
    @Test
    public void test8() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true, true, true, true, false);
        when(mockConnection.read()).thenReturn("Part1", "Part2", "Part3", "Part4");
        String result = client.requestFile("testServer", "testFile.txt");
        assertEquals("Part1Part2Part3Part4", result);
    }

    // Test 9: tests that if read() returns null, the client treats this as the empty string.
    @Test
    public void test9() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn( true, false);
        when(mockConnection.read()).thenReturn(null);
        String result = client.requestFile("testServer", "testFile.txt");
        assertEquals("", result);
    }

    // Test 10a: tests that connectTo failing with IOException returns null.
    @Test
    public void test10a() throws IOException {
        when(mockConnection.connectTo("testServer")).thenThrow(new IOException("Connection failure"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }

    // Test 10b: tests that if requestFileContents fails with IOException, the client returns null
    @Test
    public void test10b() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenThrow(new IOException("Request failure"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }

    // Test10c: tests that if moreBytes fails with IOException, the client returns null
    @Test
    public void test10c() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenThrow(new IOException("request bytes failure"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }

    // Test10d: tests that if read fails with IOException, the client returns null.
    @Test
    public void test10d() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true);
        when(mockConnection.read()).thenThrow(new IOException("Read failure"));
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }

    // Test10e. Tests that if closeConnection fails with IOException, the client returns null
    @Test
    public void test10e() throws IOException {
        when(mockConnection.connectTo("testServer")).thenReturn(true);
        when(mockConnection.requestFileContents("testFile.txt")).thenReturn(true);
        when(mockConnection.moreBytes()).thenReturn(true, false);
        when(mockConnection.read()).thenReturn("content");
        doThrow(new IOException("Close connection failure")).when(mockConnection).closeConnection();
        String result = client.requestFile("testServer", "testFile.txt");
        assertNull(result);
    }
}
